package org.nihongo.mochi.domain.game

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress
import kotlin.random.Random

enum class QuestionType { MEANING, READING }

interface TextNormalizer {
    fun normalize(text: String): String
}

class WritingGameEngine(private val textNormalizer: TextNormalizer? = null) {
    var isGameInitialized = false
    
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    
    var kanjiListPosition = 0
    val currentKanjiSet = mutableListOf<KanjiDetail>()
    val revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    
    lateinit var currentKanji: KanjiDetail
    var currentQuestionType: QuestionType = QuestionType.MEANING
    
    // UI State for Feedback
    var isAnswerProcessing = false
    var lastAnswerStatus: Boolean? = null
    var showCorrectionFeedback = false
    var correctionDelayPending = false

    fun resetState() {
        isGameInitialized = false
        allKanjiDetails.clear()
        kanjiListPosition = 0
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        isAnswerProcessing = false
        lastAnswerStatus = null
        showCorrectionFeedback = false
        correctionDelayPending = false
    }

    fun startNewSet(): Boolean {
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()

        if (kanjiListPosition >= allKanjiDetails.size) {
            return false
        }

        val nextSet = allKanjiDetails.drop(kanjiListPosition).take(10)
        kanjiListPosition += nextSet.size

        currentKanjiSet.clear()
        currentKanjiSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKanjiSet.forEach {
            kanjiStatus[it] = GameStatus.NOT_ANSWERED
            kanjiProgress[it] = KanjiProgress()
        }
        return true
    }

    fun nextQuestion() {
        if (revisionList.isEmpty()) return

        isAnswerProcessing = false
        lastAnswerStatus = null
        showCorrectionFeedback = false
        correctionDelayPending = false

        currentKanji = revisionList.random()
        val progress = kanjiProgress[currentKanji]!!
        
        currentQuestionType = when {
            !progress.meaningSolved && !progress.readingSolved -> if (Random.nextDouble() < 0.5) QuestionType.MEANING else QuestionType.READING
            !progress.meaningSolved -> QuestionType.MEANING
            else -> QuestionType.READING
        }
    }

    fun submitAnswer(userAnswer: String): Boolean {
        if (isAnswerProcessing) return false
        isAnswerProcessing = true

        val isCorrect = if (currentQuestionType == QuestionType.MEANING) {
            checkMeaning(userAnswer)
        } else {
            checkReading(userAnswer)
        }

        ScoreManager.saveScore(currentKanji.character, isCorrect, ScoreManager.ScoreType.WRITING)

        lastAnswerStatus = isCorrect
        
        if (isCorrect) {
            val progress = kanjiProgress[currentKanji]!!
            if (currentQuestionType == QuestionType.MEANING) progress.meaningSolved = true
            else progress.readingSolved = true

            if (progress.meaningSolved && progress.readingSolved) {
                kanjiStatus[currentKanji] = GameStatus.CORRECT
                revisionList.remove(currentKanji)
            } else {
                kanjiStatus[currentKanji] = GameStatus.PARTIAL
            }
        } else {
            kanjiStatus[currentKanji] = GameStatus.INCORRECT
            showCorrectionFeedback = true
        }

        return isCorrect
    }

    private fun checkMeaning(answer: String): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = false)
        return currentKanji.meanings.any { normalizeForComparison(it, isReading = false) == normalizedAnswer }
    }

    private fun checkReading(answer: String): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = true)
        return currentKanji.readings.any { normalizeForComparison(it.value, isReading = true) == normalizedAnswer }
    }

    private fun normalizeForComparison(input: String, isReading: Boolean): String {
        var normalized = input.lowercase()

        if (!isReading && textNormalizer != null) {
            normalized = textNormalizer.normalize(normalized)
        }

        if (isReading) {
            normalized = KanaUtils.katakanaToHiragana(normalized)
        }

        return normalized.replace(Regex("[.\\s-]"), "")
    }
}
