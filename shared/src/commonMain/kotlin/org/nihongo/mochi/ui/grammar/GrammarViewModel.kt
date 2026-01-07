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

    // New: Expose the total height in "slots" to the UI for consistent scaling
    private val _totalLayoutSlots = MutableStateFlow(1f)
    val totalLayoutSlots: StateFlow<Float> = _totalLayoutSlots.asStateFlow()

    private val _currentLevelId = MutableStateFlow("N5")
    val currentLevelId: StateFlow<String> = _currentLevelId.asStateFlow()

    fun loadGraph(maxLevelId: String) {
        _currentLevelId.value = maxLevelId
        viewModelScope.launch {
            _isLoading.value = true
            
            if (_availableCategories.value.isEmpty()) {
                val categories = grammarRepository.getCategories()
                _availableCategories.value = categories
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
        val currentMaxLevelId = _currentLevelId.value
        val def = grammarRepository.loadGrammarDefinition()
        val allLevels = def.metadata.levels
        val targetLevelIndex = allLevels.indexOf(currentMaxLevelId).takeIf { it != -1 } ?: allLevels.size - 1
        val levelsToShow = allLevels.take(targetLevelIndex + 1)
        
        var rules = grammarRepository.getRulesUntilLevel(currentMaxLevelId)
        
        val selected = _selectedCategories.value
        if (selected.isNotEmpty()) {
            rules = rules.filter { rule ->
                rule.category != null && selected.contains(rule.category)
            }
        }
        
        val (nodes, separators, totalSlots) = buildGraphLayout(rules, levelsToShow)
        
        _nodes.value = nodes
        _separators.value = separators
        _totalLayoutSlots.value = totalSlots
    }

    private fun buildGraphLayout(rules: List<GrammarRule>, levels: List<String>): Triple<List<GrammarNode>, List<GrammarLevelSeparator>, Float> {
        // Defines the height of one node relative to the padding
        val slotHeightPerNode = 1.0f 
        // Defines the spacing around the Toori gate (gap between levels)
        val paddingSlotsPerLevel = 3.0f

        val rawNodes = mutableListOf<Pair<GrammarRule, Float>>() // Rule + Raw Y Slot
        val rawSeparators = mutableListOf<Pair<String, Float>>() // Level + Raw Y Slot
        
        val rulesMap = rules.associateBy { it.id }
        val depthCache = mutableMapOf<String, Int>()

        fun getDepth(ruleId: String): Int {
            if (depthCache.containsKey(ruleId)) return depthCache[ruleId] ?: 0
            val rule = rulesMap[ruleId]
            if (rule == null) return 0 
            depthCache[ruleId] = -1 
            var maxDepDepth = -1
            if (rule.dependencies.isNotEmpty()) {
                for (depId in rule.dependencies) {
                    val d = getDepth(depId)
                    if (d > maxDepDepth) maxDepDepth = d
                }
            }
            val depth = maxDepDepth + 1
            depthCache[ruleId] = depth
            return depth
        }
        rules.forEach { getDepth(it.id) }

        val rulesByLevel = rules.groupBy { it.level }
        
        var currentSlot = 0f
        
        levels.forEach { levelId ->
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            
            // Add top padding/separator space for this level
            // This centers the content between the previous separator and the next
            currentSlot += 0.5f 
            
            if (levelRules.isNotEmpty()) {
                val assignedSides = mutableMapOf<String, Float>()
                var leftCount = 0
                var rightCount = 0
                
                val sortedRules = levelRules.sortedWith(
                    compareBy<GrammarRule> { depthCache[it.id] ?: 0 }.thenBy { it.id }
                )
                
                fun assignSide(ruleId: String, preferredSide: Float?): Float {
                    val side = if (preferredSide != null) preferredSide else if (leftCount <= rightCount) 0.3f else 0.7f
                    assignedSides[ruleId] = side
                    if (side < 0.5f) leftCount++ else rightCount++
                    return side
                }

                sortedRules.forEach { rule ->
                    val intraLevelParentId = rule.dependencies.firstOrNull { depId ->
                        rulesMap[depId]?.level == levelId && assignedSides.containsKey(depId)
                    }
                    val preferredSide = if (intraLevelParentId != null) assignedSides[intraLevelParentId] else null
                    assignSide(rule.id, preferredSide)
                }
                
                sortedRules.forEach { rule ->
                    rawNodes.add(rule to currentSlot)
                    currentSlot += slotHeightPerNode
                }
            } else {
                // Empty level placeholder
                currentSlot += slotHeightPerNode
            }
            
            // Space for Toori Gate
            currentSlot += (paddingSlotsPerLevel / 2f)
            rawSeparators.add(levelId to currentSlot)
            currentSlot += (paddingSlotsPerLevel / 2f)
        }
        
        // Final total slots
        val totalSlots = if (currentSlot == 0f) 1f else currentSlot
        
        // Fix: We need the X values.
        val finalNodes = mutableListOf<GrammarNode>()
        
        // RESET for second pass with correct structure
        currentSlot = 0f
        
         levels.forEach { levelId ->
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            currentSlot += 0.5f
            
            if (levelRules.isNotEmpty()) {
                val assignedSides = mutableMapOf<String, Float>()
                var leftCount = 0
                var rightCount = 0
                
                val sortedRules = levelRules.sortedWith(
                    compareBy<GrammarRule> { depthCache[it.id] ?: 0 }.thenBy { it.id }
                )
                
                fun assignSide(ruleId: String, preferredSide: Float?): Float {
                    val side = if (preferredSide != null) preferredSide else if (leftCount <= rightCount) 0.3f else 0.7f
                    assignedSides[ruleId] = side
                    if (side < 0.5f) leftCount++ else rightCount++
                    return side
                }

                sortedRules.forEach { rule ->
                    val intraLevelParentId = rule.dependencies.firstOrNull { depId ->
                        rulesMap[depId]?.level == levelId && assignedSides.containsKey(depId)
                    }
                    val preferredSide = if (intraLevelParentId != null) assignedSides[intraLevelParentId] else null
                    assignSide(rule.id, preferredSide)
                }
                
                sortedRules.forEach { rule ->
                    val x = assignedSides[rule.id] ?: 0.5f
                    finalNodes.add(GrammarNode(rule, x, currentSlot)) // Y is raw here
                    currentSlot += slotHeightPerNode
                }
            } else {
                currentSlot += slotHeightPerNode
            }
            
            currentSlot += (paddingSlotsPerLevel / 2f)
            // Separator Y raw
            currentSlot += (paddingSlotsPerLevel / 2f)
        }
        
        // Normalize
        val normalizedNodes = finalNodes.map { it.copy(y = it.y / totalSlots) }
        val normalizedSeparators = rawSeparators.map { GrammarLevelSeparator(it.first, it.second / totalSlots) }
        
        return Triple(normalizedNodes, normalizedSeparators, totalSlots)
    }
}
