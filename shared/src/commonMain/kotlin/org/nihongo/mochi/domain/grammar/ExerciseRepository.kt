package org.nihongo.mochi.domain.grammar

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.nihongo.mochi.domain.kana.ResourceLoader

@Serializable
data class ExerciseRoot(
    val exercises: List<Exercise>
)

class ExerciseRepository(
    private val resourceLoader: ResourceLoader
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        encodeDefaults = true
    }
    
    private var cachedExercises: List<Exercise>? = null

    private suspend fun getAllExercises(): List<Exercise> {
        if (cachedExercises != null) return cachedExercises!!
        
        return try {
            val jsonString = resourceLoader.loadJson("grammar/exercices.json")
            val root = json.decodeFromString<ExerciseRoot>(jsonString)
            cachedExercises = root.exercises
            root.exercises
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getExercisesForTag(tag: String, limit: Int = 10): List<Exercise> {
        return getAllExercises()
            .filter { it.tags.contains(tag) }
            .shuffled()
            .take(limit)
    }

    fun parsePayload(exercise: Exercise): ExercisePayload? {
        return try {
            when (exercise.type) {
                ExerciseType.FILL_BLANK -> json.decodeFromJsonElement<ExercisePayload.FillBlank>(exercise.payload)
                ExerciseType.SENTENCE_ORDER -> json.decodeFromJsonElement<ExercisePayload.SentenceOrder>(exercise.payload)
                ExerciseType.UNDERLINE_READING, ExerciseType.UNDERLINE_WRITING -> json.decodeFromJsonElement<ExercisePayload.Underline>(exercise.payload)
                ExerciseType.PARAPHRASE -> json.decodeFromJsonElement<ExercisePayload.Paraphrase>(exercise.payload)
                ExerciseType.WORD_USAGE -> json.decodeFromJsonElement<ExercisePayload.WordUsage>(exercise.payload)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
