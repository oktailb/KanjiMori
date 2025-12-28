package org.nihongo.mochi.domain.util

import org.nihongo.mochi.domain.game.QuestionDirection

object TextSizeCalculator {
    fun calculateButtonTextSize(textLength: Int, direction: QuestionDirection): Float {
        if (direction == QuestionDirection.REVERSE) {
            return 40f
        }
        return when {
            textLength > 40 -> 10f
            textLength > 20 -> 12f
            else -> 14f
        }
    }

    fun calculateQuestionTextSize(textLength: Int, lineCount: Int, direction: QuestionDirection): Float {
        if (direction == QuestionDirection.NORMAL) {
            return 120f
        }
        return when {
            lineCount > 7 || textLength > 100 -> 14f
            lineCount > 5 || textLength > 70 -> 18f
            lineCount > 3 || textLength > 40 -> 24f
            lineCount > 1 || textLength > 15 -> 32f
            else -> 48f
        }
    }
}
