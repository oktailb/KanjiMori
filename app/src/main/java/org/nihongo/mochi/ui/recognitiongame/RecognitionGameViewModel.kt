package org.nihongo.mochi.ui.recognitiongame

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.domain.game.RecognitionGameEngine
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress

// Re-export type aliases to maintain compatibility with Fragment imports if they use these types from ViewModel package
// Although Fragments should ideally import from domain.models directly
typealias QuestionDirection = org.nihongo.mochi.domain.game.QuestionDirection

class RecognitionGameViewModel : ViewModel() {
    
    private val engine = RecognitionGameEngine()
    
    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    val allKanjiDetailsXml = mutableListOf<KanjiDetail>() // Keep XML separate if needed for Android resource loading, or move to Engine if pure data
    
    val allKanjiDetails: MutableList<KanjiDetail>
        get() = engine.allKanjiDetails

    var currentKanjiSet: MutableList<KanjiDetail>
        get() = engine.currentKanjiSet
        set(value) { engine.currentKanjiSet = value }

    var revisionList: MutableList<KanjiDetail>
        get() = engine.revisionList
        set(value) { engine.revisionList = value }

    val kanjiStatus: MutableMap<KanjiDetail, GameStatus>
        get() = engine.kanjiStatus

    val kanjiProgress: MutableMap<KanjiDetail, KanjiProgress>
        get() = engine.kanjiProgress

    var kanjiListPosition: Int
        get() = engine.kanjiListPosition
        set(value) { engine.kanjiListPosition = value }

    var currentKanji: KanjiDetail
        get() = engine.currentKanji
        set(value) { engine.currentKanji = value }

    var correctAnswer: String
        get() = engine.correctAnswer
        set(value) { engine.correctAnswer = value }

    var gameMode: String
        get() = engine.gameMode
        set(value) { engine.gameMode = value }

    var readingMode: String
        get() = engine.readingMode
        set(value) { engine.readingMode = value }

    var currentDirection: QuestionDirection
        get() = engine.currentDirection
        set(value) { engine.currentDirection = value }
    
    var currentAnswers: List<String>
        get() = engine.currentAnswers
        set(value) { engine.currentAnswers = value }

    // UI Specific State (Platform dependent)
    var areButtonsEnabled = true
    var buttonColors = mutableListOf<Int>() // Resource IDs are platform specific

    fun resetState() {
        engine.resetState()
        allKanjiDetailsXml.clear()
        areButtonsEnabled = true
        buttonColors.clear()
    }
}
