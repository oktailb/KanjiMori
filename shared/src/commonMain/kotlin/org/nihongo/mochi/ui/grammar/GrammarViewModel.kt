package org.nihongo.mochi.ui.grammar

import org.nihongo.mochi.domain.grammar.GrammarRepository
import org.nihongo.mochi.domain.grammar.GrammarRule
import org.nihongo.mochi.presentation.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GrammarNode(
    val rule: GrammarRule,
    val x: Float, // Relative X position (0.0 - 1.0)
    val y: Float, // Relative Y position (0.0 - 1.0)
    val children: List<GrammarNode> = emptyList()
)

data class GrammarLevelSeparator(
    val levelId: String,
    val y: Float
)

class GrammarViewModel(
    private val grammarRepository: GrammarRepository
) : ViewModel() {

    private val _nodes = MutableStateFlow<List<GrammarNode>>(emptyList())
    val nodes: StateFlow<List<GrammarNode>> = _nodes.asStateFlow()

    private val _separators = MutableStateFlow<List<GrammarLevelSeparator>>(emptyList())
    val separators: StateFlow<List<GrammarLevelSeparator>> = _separators.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    private var currentMaxLevelId: String = "N5" // Default fallback

    fun loadGraph(maxLevelId: String) {
        currentMaxLevelId = maxLevelId
        viewModelScope.launch {
            _isLoading.value = true
            
            // Load available categories if not loaded
            if (_availableCategories.value.isEmpty()) {
                val categories = grammarRepository.getCategories()
                _availableCategories.value = categories
                // By default, select all categories? Or none means all? 
                // Let's say empty set means "All" for easier logic, or we initialize with all.
                // User asked for "filter to select categories". Usually starts with all.
                // Let's keep it empty initially and treat empty as "Show All" or initialize with all.
                // Let's treat empty as "Show All" to avoid issues if categories change.
            }

            refreshGraph()
            _isLoading.value = false
        }
    }

    fun toggleCategory(category: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
        viewModelScope.launch {
            refreshGraph()
        }
    }
    
    fun setCategories(categories: Set<String>) {
        _selectedCategories.value = categories
        viewModelScope.launch {
            refreshGraph()
        }
    }

    private suspend fun refreshGraph() {
        val def = grammarRepository.loadGrammarDefinition()
        
        // Determine level order
        val allLevels = def.metadata.levels
        val targetLevelIndex = allLevels.indexOf(currentMaxLevelId).takeIf { it != -1 } ?: allLevels.size - 1
        val levelsToShow = allLevels.take(targetLevelIndex + 1)
        
        var rules = grammarRepository.getRulesUntilLevel(currentMaxLevelId)
        
        // Apply Category Filter
        val selected = _selectedCategories.value
        if (selected.isNotEmpty()) {
            rules = rules.filter { rule ->
                // If rule has no category, do we show it? Usually yes, core grammar often has no specific category or "General".
                // But if the user selects specific categories, they might want only those.
                // Let's assume: if rule.category is null, keep it? Or strict filtering?
                // Looking at repository, category is nullable.
                // Let's match if category is in selected set. If category is null, maybe include it only if a special "Uncategorized" option is there?
                // For now: strict match. If category is null, it's hidden if filters are active.
                // OR: If selected categories contains "General" and rule category is null.
                rule.category != null && selected.contains(rule.category)
            }
        }
        
        // Build the graph layout
        val (nodes, separators) = buildGraphLayout(rules, levelsToShow)
        
        _nodes.value = nodes
        _separators.value = separators
    }

    private fun buildGraphLayout(rules: List<GrammarRule>, levels: List<String>): Pair<List<GrammarNode>, List<GrammarLevelSeparator>> {
        val nodes = mutableListOf<GrammarNode>()
        val separators = mutableListOf<GrammarLevelSeparator>()
        
        // Map rules for easy lookup
        val rulesMap = rules.associateBy { it.id }
        
        // Cache for depth calculation
        val depthCache = mutableMapOf<String, Int>()

        // Recursive function to calculate depth (Longest Path from Root)
        // Returns the "layer" index (0 = Root)
        fun getDepth(ruleId: String): Int {
            if (depthCache.containsKey(ruleId)) {
                val cached = depthCache[ruleId]!!
                if (cached == -1) return 0 // Cycle detected, break it, assume root
                return cached
            }
            
            val rule = rulesMap[ruleId]
            
            // If dependency is missing from the current set (rulesMap), 
            // we treat this node as a root relative to the current view.
            if (rule == null) return 0 
            
            depthCache[ruleId] = -1 // Mark as visiting to detect cycles
            
            var maxDepDepth = -1 // Start at -1 so base rules (0 deps) get depth 0
            
            if (rule.dependencies.isNotEmpty()) {
                for (depId in rule.dependencies) {
                    val d = getDepth(depId)
                    if (d > maxDepDepth) maxDepDepth = d
                }
            } else {
                 maxDepDepth = -1
            }
            
            val depth = maxDepDepth + 1
            depthCache[ruleId] = depth
            return depth
        }

        // Calculate depths for all rules
        rules.forEach { getDepth(it.id) }

        // Determine total slots needed.
        val rulesByLevel = rules.groupBy { it.level }
        val paddingSlotsPerLevel = 32
        var totalSlots = 0
        
        levels.forEach { levelId ->
            val count = rulesByLevel[levelId]?.size ?: 0
            val effectiveCount = if (count == 0) 1 else count 
            totalSlots += effectiveCount + paddingSlotsPerLevel
        }

        if (totalSlots == 0) totalSlots = 1

        var currentSlotIndex = 0
        
        levels.forEachIndexed { levelIndex, levelId ->
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            
            // Top padding for level
            currentSlotIndex++
            
            if (levelRules.isNotEmpty()) {
                // Determine placement (X axis) strategy
                // Nodes should try to be on the same side as their intra-level parent
                
                // Map node ID to assigned X side (0.25 for Left, 0.75 for Right)
                val assignedSides = mutableMapOf<String, Float>()
                
                // Keep track of counts to balance sides for root nodes
                var leftCount = 0
                var rightCount = 0
                
                // First pass: Sort by depth to process parents first
                val sortedRules = levelRules.sortedWith(
                    compareBy<GrammarRule> { depthCache[it.id] ?: 0 }
                        .thenBy { it.id }
                )
                
                // Helper to assign side
                fun assignSide(ruleId: String, preferredSide: Float?): Float {
                    val side = if (preferredSide != null) {
                        preferredSide
                    } else {
                        // Balance: pick the side with fewer items so far
                        if (leftCount <= rightCount) 0.3f else 0.7f
                    }
                    
                    assignedSides[ruleId] = side
                    if (side < 0.5f) leftCount++ else rightCount++
                    return side
                }

                sortedRules.forEach { rule ->
                    // Check for intra-level parent
                    val intraLevelParentId = rule.dependencies.firstOrNull { depId ->
                        rulesMap[depId]?.level == levelId && assignedSides.containsKey(depId)
                    }
                    
                    val preferredSide = if (intraLevelParentId != null) {
                        assignedSides[intraLevelParentId]
                    } else {
                        null
                    }
                    
                    assignSide(rule.id, preferredSide)
                }
                
                // Now create nodes with computed Y and assigned X
                sortedRules.forEach { rule ->
                    val y = currentSlotIndex.toFloat() / totalSlots
                    val x = assignedSides[rule.id] ?: 0.25f
                    
                    nodes.add(GrammarNode(rule, x, y))
                    currentSlotIndex++
                }
            } else {
                currentSlotIndex++
            }
            
            val separatorY = (currentSlotIndex.toFloat() + 0.66f) / totalSlots
            
            // Add separator for ALL levels, including the last one
            separators.add(GrammarLevelSeparator(levelId, separatorY))
            
            // Bottom padding for level
            currentSlotIndex++
        }
        
        return Pair(nodes, separators)
    }
}
