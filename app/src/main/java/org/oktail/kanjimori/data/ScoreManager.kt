package org.oktail.kanjimori.data

import android.content.Context

object ScoreManager {

    private const val PREFS_NAME = "KanjiMoriScores"

    // Optional prefix to distinguish reading scores from recognition scores if needed
    // However, the prompt implies "reading" scores are for words (which might be single kanji)
    // while "recognition" is typically for Kanji meaning/reading in isolation.
    // If the "word" is exactly the same string as the "kanji", we have a collision.
    // The user explicitly requested: "assures toi que les scores de reading sont bien distincts des scores de reconnaissance"
    
    // We will use a prefix for reading scores to separate them from recognition (Kanji) scores.
    // Recognition scores (Kanji) seem to use the character directly as key.
    // We will change the save/get methods to accept a type or implement separate methods.
    
    // To maintain backward compatibility with existing recognition scores which use the raw kanji character as key:
    // Recognition (Kanji): key = "字"
    // Reading (Word): key = "reading_字" or just "字" if it's a word? 
    // The user said "certains mots sont certes constitués d'un seul kanji mais je souhaite quand meme des scores séparés"
    // So if the word is "木" (Ki - tree) in Reading section, it should be different from "木" in Recognition section.
    
    // Implementation strategy:
    // Overload methods to accept a 'type' or 'prefix'.
    // Default behavior (no prefix) -> Recognition scores (to keep existing data valid).
    // New behavior -> Reading scores use "reading_" prefix.

    enum class ScoreType {
        RECOGNITION, // Default, raw key
        READING      // Prefixed key
    }

    fun saveScore(context: Context, key: String, wasCorrect: Boolean, type: ScoreType = ScoreType.RECOGNITION) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        
        val actualKey = getActualKey(key, type)
        val currentScore = getScoreInternal(context, actualKey)
        
        val newSuccesses = currentScore.successes + if (wasCorrect) 1 else 0
        val newFailures = currentScore.failures + if (!wasCorrect) 1 else 0

        editor.putString(actualKey, "$newSuccesses-$newFailures")
        editor.apply()
    }

    fun getScore(context: Context, key: String, type: ScoreType = ScoreType.RECOGNITION): KanjiScore {
        val actualKey = getActualKey(key, type)
        return getScoreInternal(context, actualKey)
    }

    private fun getScoreInternal(context: Context, actualKey: String): KanjiScore {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val scoreString = sharedPreferences.getString(actualKey, "0-0") ?: "0-0"

        return try {
            val parts = scoreString.split("-")
            val successes = parts[0].toInt()
            val failures = parts[1].toInt()
            KanjiScore(successes, failures)
        } catch (e: Exception) {
            KanjiScore(0, 0)
        }
    }

    private fun getActualKey(key: String, type: ScoreType): String {
        return when (type) {
            ScoreType.RECOGNITION -> key
            ScoreType.READING -> "reading_$key"
        }
    }

    fun getAllScores(context: Context, type: ScoreType = ScoreType.RECOGNITION): Map<String, KanjiScore> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.all.mapNotNull { (storedKey, value) ->
            if (value is String) {
                val isReadingKey = storedKey.startsWith("reading_")
                
                val isValidKey = when (type) {
                    ScoreType.RECOGNITION -> !isReadingKey
                    ScoreType.READING -> isReadingKey
                }
                
                if (isValidKey) {
                    val cleanKey = if (type == ScoreType.READING) storedKey.removePrefix("reading_") else storedKey
                    
                    try {
                        val parts = value.split("-")
                        val successes = parts[0].toInt()
                        val failures = parts[1].toInt()
                        cleanKey to KanjiScore(successes, failures)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }.toMap()
    }
}