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

    fun loadGraph(maxLevelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val def = grammarRepository.loadGrammarDefinition()
            
            // Determine level order
            val allLevels = def.metadata.levels
            val targetLevelIndex = allLevels.indexOf(maxLevelId).takeIf { it != -1 } ?: allLevels.size - 1
            val levelsToShow = allLevels.take(targetLevelIndex + 1)
            
            val rules = grammarRepository.getRulesUntilLevel(maxLevelId)
            
            // Build the graph layout
            val (nodes, separators) = buildGraphLayout(rules, levelsToShow)
            
            _nodes.value = nodes
            _separators.value = separators
            _isLoading.value = false
        }
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
        val paddingSlotsPerLevel = 2
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
                        if (leftCount <= rightCount) 0.25f else 0.75f
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
            
            val separatorY = (currentSlotIndex.toFloat() + 0.5f) / totalSlots
            
            // Add separator for ALL levels, including the last one
            separators.add(GrammarLevelSeparator(levelId, separatorY))
            
            // Bottom padding for level
            currentSlotIndex++
        }
        
        return Pair(nodes, separators)
    }
}
