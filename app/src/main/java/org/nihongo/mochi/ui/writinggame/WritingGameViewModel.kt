package org.nihongo.mochi.ui.writinggame

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.domain.game.QuestionType
import org.nihongo.mochi.domain.game.WritingGameEngine
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress

// Re-export type alias for compatibility
typealias QuestionType = org.nihongo.mochi.domain.game.QuestionType

class WritingGameViewModel : ViewModel() {
    
    private val engine = WritingGameEngine()
    
    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    val allKanjiDetails: MutableList<KanjiDetail>
        get() = engine.allKanjiDetails

    // Keep XML separate as it might be specific to Android resource loading/initialization
    val allKanjiDetailsXml = mutableListOf<KanjiDetail>()

    var kanjiListPosition: Int
        get() = engine.kanjiListPosition
        set(value) { engine.kanjiListPosition = value }

    val currentKanjiSet: MutableList<KanjiDetail>
        get() = engine.currentKanjiSet

    val revisionList: MutableList<KanjiDetail>
        get() = engine.revisionList

    val kanjiStatus: MutableMap<KanjiDetail, GameStatus>
        get() = engine.kanjiStatus

    val kanjiProgress: MutableMap<KanjiDetail, KanjiProgress>
        get() = engine.kanjiProgress

    var currentKanji: KanjiDetail
        get() = engine.currentKanji
        set(value) { engine.currentKanji = value }

    var currentQuestionType: QuestionType
        get() = engine.currentQuestionType
        set(value) { engine.currentQuestionType = value }

    var correctAnswer: String
        get() = engine.correctAnswer
        set(value) { engine.correctAnswer = value }

    var isAnswerProcessing: Boolean
        get() = engine.isAnswerProcessing
        set(value) { engine.isAnswerProcessing = value }

    var lastAnswerStatus: Boolean?
        get() = engine.lastAnswerStatus
        set(value) { engine.lastAnswerStatus = value }

    var showCorrectionFeedback: Boolean
        get() = engine.showCorrectionFeedback
        set(value) { engine.showCorrectionFeedback = value }

    var correctionDelayPending: Boolean
        get() = engine.correctionDelayPending
        set(value) { engine.correctionDelayPending = value }

    fun resetState() {
        engine.resetState()
        allKanjiDetailsXml.clear()
    }
}
