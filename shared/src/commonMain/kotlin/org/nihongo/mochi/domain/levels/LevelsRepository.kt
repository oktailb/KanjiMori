package org.nihongo.mochi.domain.levels

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.nihongo.mochi.shared.generated.resources.Res

class LevelsRepository {

    private var cachedDefinitions: LevelDefinitions? = null
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLevelDefinitions(): LevelDefinitions {
        if (cachedDefinitions != null) return cachedDefinitions!!
        
        try {
            val jsonString = Res.readBytes("files/levels.json").decodeToString()
            cachedDefinitions = jsonParser.decodeFromString<LevelDefinitions>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            // Return empty definitions in case of error
            cachedDefinitions = LevelDefinitions()
        }
        return cachedDefinitions!!
    }

    fun getDefinitions(): LevelDefinitions? = cachedDefinitions
    
    // Helper to get all data files for a specific activity type (e.g. READING)
    suspend fun getAllDataFilesForActivity(activityKey: String): List<String> {
        val defs = loadLevelDefinitions()
        return defs.sections.values.flatMap { section ->
            section.levels.flatMap { level ->
                level.activities
                    .filter { it.key.name == activityKey || it.key.name == activityKey.uppercase() } 
                    // Note: ActivityConfig map uses StatisticsType enum keys usually, 
                    // but depending on serialization it might be string. 
                    // In LevelModels.kt it is: Map<StatisticsType, ActivityConfig>
                    // So we should filter by the Enum name if we pass a string.
                    .map { it.value.dataFile }
            }
        }.distinct()
    }
}
