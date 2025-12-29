package org.nihongo.mochi.domain.statistics

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider

class StatisticsEngine(
    private val levelContentProvider: LevelContentProvider
) {

    private data class LevelDefinition(
        val name: String, 
        val xmlName: String, 
        val type: StatisticsType,
        val category: String,
        val sortOrder: Int
    )

    private fun getLevelDefinitions(): List<LevelDefinition> {
        return listOf(
            // --- RECOGNITION ---
            // Kanas
            LevelDefinition("Hiragana", "Hiragana", StatisticsType.RECOGNITION, "Kanas", 1),
            LevelDefinition("Katakana", "Katakana", StatisticsType.RECOGNITION, "Kanas", 2),
            
            // JLPT
            LevelDefinition("JLPT N5", "N5", StatisticsType.RECOGNITION, "JLPT", 1),
            LevelDefinition("JLPT N4", "N4", StatisticsType.RECOGNITION, "JLPT", 2),
            LevelDefinition("JLPT N3", "N3", StatisticsType.RECOGNITION, "JLPT", 3),
            LevelDefinition("JLPT N2", "N2", StatisticsType.RECOGNITION, "JLPT", 4),
            LevelDefinition("JLPT N1", "N1", StatisticsType.RECOGNITION, "JLPT", 5),

            // School
            LevelDefinition("Grade 1", "Grade 1", StatisticsType.RECOGNITION, "School", 1),
            LevelDefinition("Grade 2", "Grade 2", StatisticsType.RECOGNITION, "School", 2),
            LevelDefinition("Grade 3", "Grade 3", StatisticsType.RECOGNITION, "School", 3),
            LevelDefinition("Grade 4", "Grade 4", StatisticsType.RECOGNITION, "School", 4),
            LevelDefinition("Grade 5", "Grade 5", StatisticsType.RECOGNITION, "School", 5),
            LevelDefinition("Grade 6", "Grade 6", StatisticsType.RECOGNITION, "School", 6),
            LevelDefinition("Collège", "Grade 7", StatisticsType.RECOGNITION, "School", 7),
            LevelDefinition("Lycée", "Grade 8", StatisticsType.RECOGNITION, "School", 8),

            // Challenges
            LevelDefinition("Native Challenge", "Native Challenge", StatisticsType.RECOGNITION, "Challenges", 1),
            LevelDefinition("No Reading", "No Reading", StatisticsType.RECOGNITION, "Challenges", 2),
            LevelDefinition("No Meaning", "No Meaning", StatisticsType.RECOGNITION, "Challenges", 3),

            // --- READING ---
            LevelDefinition("Reading User", "user_list", StatisticsType.READING, "User", 1),
            
            // JLPT
            LevelDefinition("Reading N5", "reading_n5", StatisticsType.READING, "JLPT", 1),
            LevelDefinition("Reading N4", "reading_n4", StatisticsType.READING, "JLPT", 2),
            LevelDefinition("Reading N3", "reading_n3", StatisticsType.READING, "JLPT", 3),
            LevelDefinition("Reading N2", "reading_n2", StatisticsType.READING, "JLPT", 4),
            LevelDefinition("Reading N1", "reading_n1", StatisticsType.READING, "JLPT", 5),
            
            // Frequency
            LevelDefinition("Reading 1000", "bccwj_wordlist_1000", StatisticsType.READING, "Frequency", 1),
            LevelDefinition("Reading 2000", "bccwj_wordlist_2000", StatisticsType.READING, "Frequency", 2),
            LevelDefinition("Reading 3000", "bccwj_wordlist_3000", StatisticsType.READING, "Frequency", 3),
            LevelDefinition("Reading 4000", "bccwj_wordlist_4000", StatisticsType.READING, "Frequency", 4),
            LevelDefinition("Reading 5000", "bccwj_wordlist_5000", StatisticsType.READING, "Frequency", 5),
            LevelDefinition("Reading 6000", "bccwj_wordlist_6000", StatisticsType.READING, "Frequency", 6),
            LevelDefinition("Reading 7000", "bccwj_wordlist_7000", StatisticsType.READING, "Frequency", 7),
            LevelDefinition("Reading 8000", "bccwj_wordlist_8000", StatisticsType.READING, "Frequency", 8),

            // --- WRITING ---
            LevelDefinition("Writing User", "user_list", StatisticsType.WRITING, "User", 1),

            // JLPT
            LevelDefinition("Writing JLPT N5", "N5", StatisticsType.WRITING, "JLPT", 1),
            LevelDefinition("Writing JLPT N4", "N4", StatisticsType.WRITING, "JLPT", 2),
            LevelDefinition("Writing JLPT N3", "N3", StatisticsType.WRITING, "JLPT", 3),
            LevelDefinition("Writing JLPT N2", "N2", StatisticsType.WRITING, "JLPT", 4),
            LevelDefinition("Writing JLPT N1", "N1", StatisticsType.WRITING, "JLPT", 5),
            
            // School
            LevelDefinition("Writing Grade 1", "Grade 1", StatisticsType.WRITING, "School", 1),
            LevelDefinition("Writing Grade 2", "Grade 2", StatisticsType.WRITING, "School", 2),
            LevelDefinition("Writing Grade 3", "Grade 3", StatisticsType.WRITING, "School", 3),
            LevelDefinition("Writing Grade 4", "Grade 4", StatisticsType.WRITING, "School", 4),
            LevelDefinition("Writing Grade 5", "Grade 5", StatisticsType.WRITING, "School", 5),
            LevelDefinition("Writing Grade 6", "Grade 6", StatisticsType.WRITING, "School", 6),
            LevelDefinition("Writing Collège", "Grade 7", StatisticsType.WRITING, "School", 7),
            LevelDefinition("Writing Lycée", "Grade 8", StatisticsType.WRITING, "School", 8)
        )
    }

    fun getAllStatistics(): List<LevelProgress> {
        val levels = getLevelDefinitions()
        return levels.map { level ->
            val scoreType = when(level.type) {
                StatisticsType.READING -> ScoreManager.ScoreType.READING
                StatisticsType.WRITING -> ScoreManager.ScoreType.WRITING
                StatisticsType.RECOGNITION -> ScoreManager.ScoreType.RECOGNITION
                // Default to RECOGNITION for unknown types
                else -> ScoreManager.ScoreType.RECOGNITION
            }

            val percentage = if (level.xmlName == "user_list") {
                calculateUserListPercentage(scoreType)
            } else {
                val characters = levelContentProvider.getCharactersForLevel(level.xmlName)
                calculateMasteryPercentage(characters, scoreType)
            }

            LevelProgress(level.name, level.xmlName, percentage.toInt(), level.type, level.category, level.sortOrder)
        }
    }

    private fun calculateUserListPercentage(scoreType: ScoreManager.ScoreType): Double {
        val scores = ScoreManager.getAllScores(scoreType)
        if (scores.isEmpty()) return 0.0

        val totalEncountered = scores.size
        val mastered = scores.count { (_, score) -> (score.successes - score.failures) >= 10 }

        return if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private fun calculateMasteryPercentage(characterList: List<String>, scoreType: ScoreManager.ScoreType): Double {
        if (characterList.isEmpty()) return 0.0

        val totalMasteryPoints = characterList.sumOf { character ->
            val score = ScoreManager.getScore(character, scoreType)
            val balance = score.successes - score.failures
            balance.coerceIn(0, 10).toDouble()
        }

        val maxPossiblePoints = characterList.size * 10.0
        if (maxPossiblePoints == 0.0) return 0.0

        return (totalMasteryPoints / maxPossiblePoints) * 100.0
    }
}
