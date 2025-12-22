package org.nihongo.mochi.ui.gojuon

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.ui.game.GameStatus
import org.nihongo.mochi.ui.game.KanaCharacter
import org.nihongo.mochi.ui.game.KanaProgress
import org.nihongo.mochi.ui.game.KanaQuestionDirection

enum class QuizMode { ROMAJI_TO_KANA, KANA_TO_ROMAJI }

class KanaQuizViewModel : ViewModel() {
    var isGameInitialized = false
    
    // Game Data
    var allKana: List<KanaCharacter> = emptyList() // Loaded from XML
    var currentKanaSet = mutableListOf<KanaCharacter>() // The 10 items in current set
    var revisionList = mutableListOf<KanaCharacter>() // Items not yet fully solved in current set
    var kanaListPosition = 0 // Position in the global list
    
    // Status & Progress
    val kanaStatus = mutableMapOf<KanaCharacter, GameStatus>()
    val kanaProgress = mutableMapOf<KanaCharacter, KanaProgress>()
    
    // Current Question State
    lateinit var currentQuestion: KanaCharacter
    var currentDirection: KanaQuestionDirection = KanaQuestionDirection.NORMAL
    var quizMode: QuizMode = QuizMode.KANA_TO_ROMAJI // Initial preference

    // UI State
    var currentAnswers: List<String> = emptyList()
    var areButtonsEnabled = true
    var buttonColors = mutableListOf<Int>() // Store color resource IDs

    fun resetState() {
        isGameInitialized = false
        allKana = emptyList()
        currentKanaSet.clear()
        revisionList.clear()
        kanaListPosition = 0
        kanaStatus.clear()
        kanaProgress.clear()
        currentAnswers = emptyList()
        areButtonsEnabled = true
        buttonColors.clear()
    }
}