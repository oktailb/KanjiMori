package org.nihongo.mochi.ui.writinggame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.game.TextNormalizer
import org.nihongo.mochi.domain.game.WritingGameEngine
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress
import java.text.Normalizer

// Re-export type alias for compatibility
typealias QuestionType = org.nihongo.mochi.domain.game.QuestionType

class WritingGameViewModel : ViewModel() {
    
    private val androidNormalizer = object : TextNormalizer {
        override fun normalize(text: String): String {
             return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        }
    }
    
    private val engine = WritingGameEngine(androidNormalizer)
    
    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    val allKanjiDetails: MutableList<KanjiDetail>
        get() = engine.allKanjiDetails

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

    val currentKanji: KanjiDetail
        get() = engine.currentKanji

    val currentQuestionType: QuestionType
        get() = engine.currentQuestionType

    // Observables
    val state: StateFlow<GameState> = engine.state
    
    // Feedback state from engine (though StateFlow handles main flow)
    var showCorrectionFeedback: Boolean
        get() = engine.showCorrectionFeedback
        set(value) { engine.showCorrectionFeedback = value }

    var correctionDelayPending: Boolean
        get() = engine.correctionDelayPending
        set(value) { engine.correctionDelayPending = value }

    fun setAnimationSpeed(speed: Float) {
        engine.animationSpeed = speed
    }

    fun startNewSet(): Boolean {
        return engine.startNewSet()
    }

    fun nextQuestion() {
        engine.nextQuestion()
    }
    
    fun startGame() {
        engine.startGame()
    }

    fun submitAnswer(userAnswer: String) {
        viewModelScope.launch {
            engine.submitAnswer(userAnswer)
        }
    }

    fun resetState() {
        engine.resetState()
        allKanjiDetailsXml.clear()
    }
}
