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
        
        // Basic layout strategy:
        // Y-axis depends on the Level (N5 at top, N1 at bottom)
        // X-axis distributed to minimize overlap
        
        val levelHeight = 1.0f / levels.size
        val rulesByLevel = rules.groupBy { it.level }
        
        levels.forEachIndexed { index, levelId ->
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            if (levelRules.isNotEmpty()) {
                val yBase = index * levelHeight + (levelHeight / 2)
                val count = levelRules.size
                
                // Simple distribution for now. 
                // A better algorithm would check dependencies to place children below parents.
                levelRules.forEachIndexed { ruleIndex, rule ->
                    // Add some jitter to Y to avoid perfect alignment looking too grid-like
                    val jitterY = if (ruleIndex % 2 == 0) -levelHeight * 0.1f else levelHeight * 0.1f
                    
                    val x = (ruleIndex + 1).toFloat() / (count + 1)
                    val y = yBase + jitterY
                    
                    nodes.add(GrammarNode(rule, x, y))
                }
            }
            
            // Add separator at the end of the level (except the last one)
            if (index < levels.size - 1) {
                separators.add(GrammarLevelSeparator(levelId, (index + 1) * levelHeight))
            }
        }
        
        return Pair(nodes, separators)
    }
}
