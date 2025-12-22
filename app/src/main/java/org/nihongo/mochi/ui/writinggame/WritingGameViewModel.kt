package org.nihongo.mochi.ui.writinggame

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.ui.game.GameStatus
import org.nihongo.mochi.ui.game.KanjiDetail
import org.nihongo.mochi.ui.game.Reading

data class KanjiProgress(var meaningSolved: Boolean = false, var readingSolved: Boolean = false)
enum class QuestionType { MEANING, READING }

class WritingGameViewModel : ViewModel() {
    var isGameInitialized = false

    val allKanjiDetailsXml = mutableListOf<KanjiDetail>()
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    var currentKanjiSet = mutableListOf<KanjiDetail>()
    var revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    var kanjiListPosition = 0
    lateinit var currentKanji: KanjiDetail
    var currentQuestionType: QuestionType = QuestionType.MEANING
    var isAnswerProcessing = false
    
    // UI State
    var lastAnswerStatus: Boolean? = null // true if correct, false if incorrect, null if neutral/waiting
    var showCorrectionFeedback = false
    var correctionDelayPending = false

    fun resetState() {
        isGameInitialized = false
        allKanjiDetailsXml.clear()
        allKanjiDetails.clear()
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        kanjiListPosition = 0
        isAnswerProcessing = false
        lastAnswerStatus = null
        showCorrectionFeedback = false
        correctionDelayPending = false
    }
}