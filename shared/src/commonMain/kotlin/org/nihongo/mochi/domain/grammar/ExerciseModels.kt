package org.nihongo.mochi.domain.grammar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Exercise(
    val id: String,
    val type: ExerciseType,
    val tags: List<String>,
    val payload: JsonObject
)

@Serializable
enum class ExerciseType {
    FILL_BLANK,
    SENTENCE_ORDER,
    UNDERLINE_READING,
    UNDERLINE_WRITING,
    PARAPHRASE,
    WORD_USAGE
}

@Serializable
sealed class ExercisePayload {
    @Serializable
    @SerialName("FILL_BLANK")
    data class FillBlank(
        val sentence: String,
        val correct: String,
        val distractors: List<String>
    ) : ExercisePayload()

    @Serializable
    @SerialName("SENTENCE_ORDER")
    data class SentenceOrder(
        val prefix: String = "",
        val suffix: String = "",
        val blocks: List<String> // In correct order
    ) : ExercisePayload()

    @Serializable
    data class Underline(
        val sentence: String,
        val correct: String,
        val distractors: List<String>
    ) : ExercisePayload()

    @Serializable
    @SerialName("PARAPHRASE")
    data class Paraphrase(
        @SerialName("base_sentence") val baseSentence: String,
        val correct: String,
        val distractors: List<String>
    ) : ExercisePayload()

    @Serializable
    @SerialName("WORD_USAGE")
    data class WordUsage(
        val word: String,
        val options: List<UsageOption>
    ) : ExercisePayload()
}

@Serializable
data class UsageOption(
    val text: String,
    val is_correct: Boolean
)
