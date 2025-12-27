package org.nihongo.mochi.domain.game

import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress

enum class QuestionDirection { NORMAL, REVERSE }

class RecognitionGameEngine {
    var isGameInitialized = false
    
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    var currentKanjiSet = mutableListOf<KanjiDetail>()
    var revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    var kanjiListPosition = 0
    lateinit var currentKanji: KanjiDetail
    lateinit var correctAnswer: String
    lateinit var gameMode: String
    lateinit var readingMode: String
    var currentDirection: QuestionDirection = QuestionDirection.NORMAL
    
    // UI State
    var currentAnswers = listOf<String>()
    
    fun resetState() {
        isGameInitialized = false
        allKanjiDetails.clear()
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        kanjiListPosition = 0
        currentAnswers = emptyList()
    }
}
