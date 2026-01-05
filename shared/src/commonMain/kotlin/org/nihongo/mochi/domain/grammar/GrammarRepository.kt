package org.nihongo.mochi.domain.grammar

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.nihongo.mochi.domain.kana.ResourceLoader

@Serializable
data class GrammarDefinition(
    val version: String,
    val metadata: GrammarMetadata,
    val dependencies_basics: List<GrammarRule>,
    val rules: List<GrammarRule>
)

@Serializable
data class GrammarMetadata(
    val levels: List<String>,
    val categories: List<String>
)

@Serializable
data class GrammarRule(
    val id: String,
    val description: String,
    val level: String,
    val dependencies: List<String>,
    val category: String? = null,
    val tags: List<String> = emptyList()
)

class GrammarRepository(
    private val resourceLoader: ResourceLoader
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var grammarDefinition: GrammarDefinition? = null

    suspend fun loadGrammarDefinition(): GrammarDefinition {
        if (grammarDefinition != null) return grammarDefinition!!

        val jsonString = resourceLoader.loadJson("grammar/grammar.json")
        grammarDefinition = json.decodeFromString<GrammarDefinition>(jsonString)
        return grammarDefinition!!
    }

    suspend fun getRulesUntilLevel(maxLevelId: String): List<GrammarRule> {
        val def = loadGrammarDefinition()
        val allRules = def.dependencies_basics + def.rules
        
        // This is a simplified filter. A real implementation might need to understand the order of JLPT levels (N5 < N4 < N3...)
        // or rely on the "levels" list in metadata to know the order.
        
        val levelsOrder = def.metadata.levels
        val maxLevelIndex = levelsOrder.indexOf(maxLevelId)
        
        if (maxLevelIndex == -1) return emptyList()

        return allRules.filter { rule ->
            val ruleLevelIndex = levelsOrder.indexOf(rule.level)
            ruleLevelIndex != -1 && ruleLevelIndex <= maxLevelIndex
        }
    }
}
