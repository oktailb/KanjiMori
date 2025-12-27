package org.nihongo.mochi.domain.game

import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.Word

class WordQuizEngine {
    var isGameInitialized = false

    var allWords = mutableListOf<Word>()
    var currentWordSet = mutableListOf<Word>()
    var revisionList = mutableListOf<Word>()
    val wordStatus = mutableMapOf<Word, GameStatus>()
    var wordListPosition = 0
    lateinit var currentWord: Word
    lateinit var correctAnswer: String

    // UI State
    var currentAnswers = listOf<String>()
    
    fun resetState() {
        isGameInitialized = false
        allWords.clear()
        currentWordSet.clear()
        revisionList.clear()
        wordStatus.clear()
        wordListPosition = 0
        currentAnswers = emptyList()
    }
}
