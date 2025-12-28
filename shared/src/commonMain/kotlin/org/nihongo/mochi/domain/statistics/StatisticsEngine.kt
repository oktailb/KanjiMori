package org.nihongo.mochi.domain.statistics

import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.util.LevelContentProvider

class StatisticsEngine(
    private val levelContentProvider: LevelContentProvider
) {

    private data class LevelDefinition(val name: String, val xmlName: String)

    private fun getLevelDefinitions(): List<LevelDefinition> {
        return listOf(
            LevelDefinition("JLPT N5", "N5"),
            LevelDefinition("JLPT N4", "N4"),
            LevelDefinition("JLPT N3", "N3"),
            LevelDefinition("JLPT N2", "N2"),
            LevelDefinition("JLPT N1", "N1"),
            LevelDefinition("Grade 1", "Grade 1"),
            LevelDefinition("Grade 2", "Grade 2"),
            LevelDefinition("Grade 3", "Grade 3"),
            LevelDefinition("Grade 4", "Grade 4"),
            LevelDefinition("Grade 5", "Grade 5"),
            LevelDefinition("Grade 6", "Grade 6"),
            LevelDefinition("Collège", "Grade 7"),
            LevelDefinition("Lycée", "Grade 8"),
            LevelDefinition("Hiragana", "Hiragana"),
            LevelDefinition("Katakana", "Katakana"),
            LevelDefinition("Reading User", "user_list"),
            LevelDefinition("Reading N5", "reading_n5"),
            LevelDefinition("Reading N4", "reading_n4"),
            LevelDefinition("Reading N3", "reading_n3"),
            LevelDefinition("Reading N2", "reading_n2"),
            LevelDefinition("Reading N1", "reading_n1"),
            LevelDefinition("Reading 1000", "bccwj_wordlist_1000"),
            LevelDefinition("Reading 2000", "bccwj_wordlist_2000"),
            LevelDefinition("Reading 3000", "bccwj_wordlist_3000"),
            LevelDefinition("Reading 4000", "bccwj_wordlist_4000"),
            LevelDefinition("Reading 5000", "bccwj_wordlist_5000"),
            LevelDefinition("Reading 6000", "bccwj_wordlist_6000"),
            LevelDefinition("Reading 7000", "bccwj_wordlist_7000"),
            LevelDefinition("Reading 8000", "bccwj_wordlist_8000"),
            LevelDefinition("Writing User", "user_list"),
            LevelDefinition("Writing JLPT N5", "N5"),
            LevelDefinition("Writing JLPT N4", "N4"),
            LevelDefinition("Writing JLPT N3", "N3"),
            LevelDefinition("Writing JLPT N2", "N2"),
            LevelDefinition("Writing JLPT N1", "N1"),
            LevelDefinition("Writing Grade 1", "Grade 1"),
            LevelDefinition("Writing Grade 2", "Grade 2"),
            LevelDefinition("Writing Grade 3", "Grade 3"),
            LevelDefinition("Writing Grade 4", "Grade 4"),
            LevelDefinition("Writing Grade 5", "Grade 5"),
            LevelDefinition("Writing Grade 6", "Grade 6"),
            LevelDefinition("Writing Collège", "Grade 7"),
            LevelDefinition("Writing Lycée", "Grade 8")
        )
    }

    fun getAllStatistics(): List<LevelProgress> {
        val levels = getLevelDefinitions()
        return levels.map { level ->
            val type = when {
                level.name.startsWith("Reading") -> StatisticsType.READING
                level.name.startsWith("Writing") -> StatisticsType.WRITING
                else -> StatisticsType.RECOGNITION
            }

            val scoreType = when(type) {
                StatisticsType.READING -> ScoreManager.ScoreType.READING
                StatisticsType.WRITING -> ScoreManager.ScoreType.WRITING
                StatisticsType.RECOGNITION -> ScoreManager.ScoreType.RECOGNITION
            }

            val percentage = if (level.xmlName == "user_list") {
                calculateUserListPercentage(scoreType)
            } else {
                val characters = levelContentProvider.getCharactersForLevel(level.xmlName)
                calculateMasteryPercentage(characters, scoreType)
            }

            LevelProgress(level.name, level.xmlName, percentage.toInt(), type)
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
