package org.nihongo.mochi.ui.gojuon

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.domain.game.KanaQuizEngine
import org.nihongo.mochi.domain.game.QuizMode
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanaCharacter
import org.nihongo.mochi.domain.models.KanaProgress
import org.nihongo.mochi.domain.models.KanaQuestionDirection

// Re-export type alias
typealias QuizMode = org.nihongo.mochi.domain.game.QuizMode

class KanaQuizViewModel : ViewModel() {

    private val engine = KanaQuizEngine()

    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    var quizMode: QuizMode
        get() = engine.quizMode
        set(value) { engine.quizMode = value }
    
    // Data
    var allKana: List<KanaCharacter>
        get() = engine.allKana
        set(value) { engine.allKana = value }
    
    // Game State
    var kanaListPosition: Int
        get() = engine.kanaListPosition
        set(value) { engine.kanaListPosition = value }

    val currentKanaSet: MutableList<KanaCharacter>
        get() = engine.currentKanaSet

    val revisionList: MutableList<KanaCharacter>
        get() = engine.revisionList

    val kanaStatus: MutableMap<KanaCharacter, GameStatus>
        get() = engine.kanaStatus

    val kanaProgress: MutableMap<KanaCharacter, KanaProgress>
        get() = engine.kanaProgress
    
    var currentQuestion: KanaCharacter
        get() = engine.currentQuestion
        set(value) { engine.currentQuestion = value }

    var currentDirection: KanaQuestionDirection
        get() = engine.currentDirection
        set(value) { engine.currentDirection = value }
    
    var currentAnswers: List<String>
        get() = engine.currentAnswers
        set(value) { engine.currentAnswers = value }

    // UI Specific State
    var areButtonsEnabled = true
    var buttonColors = mutableListOf<Int>()
    
    fun resetState() {
        engine.resetState()
        areButtonsEnabled = true
        buttonColors.clear()
    }
}
