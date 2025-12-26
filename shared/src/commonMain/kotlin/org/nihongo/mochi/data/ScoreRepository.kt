package org.nihongo.mochi.data

interface ScoreRepository {
    fun saveScore(key: String, wasCorrect: Boolean, type: ScoreManager.ScoreType)
    fun getScore(key: String, type: ScoreManager.ScoreType): KanjiScore
    fun getAllScores(type: ScoreManager.ScoreType): Map<String, KanjiScore>
    fun decayScores()
    
    // Backup/Restore
    fun getAllDataJson(): String
    fun restoreDataFromJson(json: String)
}
