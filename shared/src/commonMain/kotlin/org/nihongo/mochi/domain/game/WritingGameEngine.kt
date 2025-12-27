package org.nihongo.mochi.domain.game

import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress

enum class QuestionType { MEANING, READING }

class WritingGameEngine {
    var isGameInitialized = false
    
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    
    var kanjiListPosition = 0
    val currentKanjiSet = mutableListOf<KanjiDetail>()
    val revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    
    lateinit var currentKanji: KanjiDetail
    var currentQuestionType: QuestionType = QuestionType.MEANING
    var correctAnswer: String = ""
    
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
}
