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
                ExerciseType.FILL_BLANK -> {
                    ExercisePayload.FillBlank(
                        sentence = exercise.payload["sentence"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        correct = exercise.payload["correct"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        distractors = exercise.payload["distractors"]?.let { json.decodeFromJsonElement<List<String>>(it) } ?: emptyList()
                    )
                }
                ExerciseType.SENTENCE_ORDER -> {
                    ExercisePayload.SentenceOrder(
                        prefix = exercise.payload["prefix"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        suffix = exercise.payload["suffix"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        blocks = exercise.payload["blocks"]?.let { json.decodeFromJsonElement<List<String>>(it) } ?: emptyList()
                    )
                }
                ExerciseType.UNDERLINE_READING, ExerciseType.UNDERLINE_WRITING -> {
                    ExercisePayload.Underline(
                        sentence = exercise.payload["sentence"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        correct = exercise.payload["correct"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        distractors = exercise.payload["distractors"]?.let { json.decodeFromJsonElement<List<String>>(it) } ?: emptyList()
                    )
                }
                ExerciseType.PARAPHRASE -> {
                    ExercisePayload.Paraphrase(
                        baseSentence = exercise.payload["base_sentence"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        correct = exercise.payload["correct"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        distractors = exercise.payload["distractors"]?.let { json.decodeFromJsonElement<List<String>>(it) } ?: emptyList()
                    )
                }
                ExerciseType.WORD_USAGE -> {
                    ExercisePayload.WordUsage(
                        word = exercise.payload["word"]?.let { json.decodeFromJsonElement<String>(it) } ?: "",
                        options = exercise.payload["options"]?.let { json.decodeFromJsonElement<List<UsageOption>>(it) } ?: emptyList()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
