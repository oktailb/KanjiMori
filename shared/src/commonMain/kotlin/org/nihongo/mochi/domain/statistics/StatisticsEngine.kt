package org.nihongo.mochi.domain.statistics

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.shared.generated.resources.Res

class StatisticsEngine(
    private val levelContentProvider: LevelContentProvider
) {

    @Serializable
    private data class LevelDefinition(
        val name: String,
        val xmlName: String,
        val type: StatisticsType,
        val category: String,
        val sortOrder: Int,
        val globalStep: Int = 0 // Default for backward compatibility if field missing
    )

    @Serializable
    private data class LevelDefinitions(
        val definitions: List<LevelDefinition>
    )

    private var cachedLevels: List<LevelDefinition> = emptyList()

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLevelDefinitions() {
        if (cachedLevels.isNotEmpty()) return
        try {
            val jsonString = Res.readBytes("files/levels.json").decodeToString()
            val data = Json.decodeFromString<LevelDefinitions>(jsonString)
            cachedLevels = data.definitions
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback empty or hardcoded if critical
        }
    }

    // Blocking getter for legacy calls (if needed, but prefer suspend init)
    // Note: In KMP/Compose, resources are loaded asynchronously.
    // If you need synchronous access immediately, you might need to preload this in ViewModel init or similar.
    private fun getLevelDefinitions(): List<LevelDefinition> {
        return cachedLevels
    }

    fun getAllStatistics(): List<LevelProgress> {
        val levels = getLevelDefinitions()
        return levels.map { level ->
            val percentage = calculatePercentage(level)
            LevelProgress(level.name, level.xmlName, percentage.toInt(), level.type, level.category, level.sortOrder)
        }
    }
    
    fun getSagaMapSteps(tab: SagaTab): List<SagaStep> {
        val allLevels = getLevelDefinitions()
        
        val categoryFilter = when(tab) {
            SagaTab.JLPT -> listOf("Kanas", "JLPT", "Frequency") 
            SagaTab.SCHOOL -> listOf("Kanas", "School", "Frequency")
            SagaTab.CHALLENGES -> listOf("Challenges", "Frequency") 
        }
        
        val relevantLevels = allLevels.filter { it.category in categoryFilter }
        
        // Group by globalStep now, instead of calculating stepIndex from sortOrder
        val levelsWithStep = relevantLevels.map { level ->
             level.globalStep to level
        }
        
        val stepsGrouped = levelsWithStep.groupBy { it.first }
            .toSortedMap()
            
        return stepsGrouped.map { (stepIndex, pairs) ->
            val levelsInStep = pairs.map { it.second }
            
            // Strategy: Group by "Base Name" or something similar to merge Recog/Read/Write.
            // For Kanas: "Hiragana" and "Katakana" are distinct Base Names.
            // For JLPT: "N5" is the base name.
            
            val nodeGroups = levelsInStep.groupBy { level ->
                // Normalize Key
                when {
                    level.xmlName.startsWith("reading_") -> level.xmlName.removePrefix("reading_").uppercase() // reading_n5 -> N5
                    level.category == "Kanas" -> level.xmlName // Hiragana != Katakana
                    else -> level.xmlName.uppercase() // N5 -> N5
                }
            }
            
            val nodes = nodeGroups.map { (key, levels) ->
                 val recogLevel = levels.find { it.type == StatisticsType.RECOGNITION }
                 val readLevel = levels.find { it.type == StatisticsType.READING }
                 val writeLevel = levels.find { it.type == StatisticsType.WRITING }
                 
                 val mainType = when {
                    recogLevel != null -> StatisticsType.RECOGNITION
                    readLevel != null -> StatisticsType.READING
                    writeLevel != null -> StatisticsType.WRITING
                    else -> StatisticsType.RECOGNITION
                }
                
                val title = when(mainType) {
                    StatisticsType.RECOGNITION -> recogLevel!!.name
                    StatisticsType.READING -> readLevel!!.name
                    StatisticsType.WRITING -> writeLevel!!.name
                }
                
                val nodeId = when(mainType) {
                    StatisticsType.RECOGNITION -> recogLevel!!.xmlName
                    StatisticsType.READING -> readLevel!!.xmlName
                    StatisticsType.WRITING -> writeLevel!!.xmlName
                }

                SagaNode(
                    id = nodeId,
                    title = title,
                    recognitionId = recogLevel?.xmlName,
                    readingId = readLevel?.xmlName,
                    writingId = writeLevel?.xmlName,
                    mainType = mainType
                )
            }
            
            SagaStep(
                id = "step_$stepIndex",
                nodes = nodes
            )
        }
    }
    
    fun getSagaProgress(node: SagaNode): UserSagaProgress {
        val recogProgress = node.recognitionId?.let { calculatePercentage(it, StatisticsType.RECOGNITION) } ?: 0
        val readProgress = node.readingId?.let { calculatePercentage(it, StatisticsType.READING) } ?: 0
        val writeProgress = node.writingId?.let { calculatePercentage(it, StatisticsType.WRITING) } ?: 0
        
        val nodeMap = mutableMapOf<String, Int>()
        if (node.recognitionId != null) nodeMap[node.recognitionId] = recogProgress
        if (node.readingId != null) nodeMap[node.readingId] = readProgress
        if (node.writingId != null) nodeMap[node.writingId] = writeProgress
        
        return UserSagaProgress(
            recognitionIndex = recogProgress,
            readingIndex = readProgress,
            writingIndex = writeProgress,
            nodeProgress = nodeMap
        )
    }

    private fun calculatePercentage(xmlName: String, type: StatisticsType): Int {
        val scoreType = when(type) {
            StatisticsType.READING -> ScoreManager.ScoreType.READING
            StatisticsType.WRITING -> ScoreManager.ScoreType.WRITING
            StatisticsType.RECOGNITION -> ScoreManager.ScoreType.RECOGNITION
        }
        
        return if (xmlName == "user_list") {
             calculateUserListPercentage(scoreType).toInt()
        } else {
             val characters = levelContentProvider.getCharactersForLevel(xmlName)
             calculateMasteryPercentage(characters, scoreType).toInt()
        }
    }
    
    private fun calculatePercentage(level: LevelDefinition): Double {
        return calculatePercentage(level.xmlName, level.type).toDouble()
    }

    private fun calculateUserListPercentage(scoreType: ScoreManager.ScoreType): Double {
        val scores = ScoreManager.getAllScores(scoreType)
        if (scores.isEmpty()) return 0.0

        val totalEncountered = scores.size
        val mastered = scores.count { (_, score) -> (score.successes - score.failures) >= 10 }

        return if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private fun calculateMasteryPercentage(characterList: List<String>, scoreType: ScoreManager.ScoreType): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = ScoreManager.getScore(character, scoreType)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100.0
    }
}
