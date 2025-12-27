package org.nihongo.mochi.domain.game

import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanaCharacter
import org.nihongo.mochi.domain.models.KanaProgress
import org.nihongo.mochi.domain.models.KanaQuestionDirection

enum class QuizMode {
    KANA_TO_ROMAJI,
    ROMAJI_TO_KANA
}

class KanaQuizEngine {
    var isGameInitialized = false
    var quizMode: QuizMode = QuizMode.KANA_TO_ROMAJI
    
    // Data
    var allKana = listOf<KanaCharacter>()
    
    // Game State
    var kanaListPosition = 0
    val currentKanaSet = mutableListOf<KanaCharacter>()
    val revisionList = mutableListOf<KanaCharacter>()
    val kanaStatus = mutableMapOf<KanaCharacter, GameStatus>()
    val kanaProgress = mutableMapOf<KanaCharacter, KanaProgress>()
    
    lateinit var currentQuestion: KanaCharacter
    var currentDirection: KanaQuestionDirection = KanaQuestionDirection.NORMAL
    
    // UI State
    var currentAnswers = listOf<String>()
    
    fun resetState() {
        isGameInitialized = false
        allKana = emptyList()
        kanaListPosition = 0
        currentKanaSet.clear()
        revisionList.clear()
        kanaStatus.clear()
        kanaProgress.clear()
        currentAnswers = emptyList()
    }
}
