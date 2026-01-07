package org.nihongo.mochi.presentation

import org.nihongo.mochi.data.LearningScore

object ScorePresentationUtils {
    // ARGB constants compatible with generic Int colors
    private const val COLOR_GREEN = 0xFF00FF00.toInt()
    private const val COLOR_RED = 0xFFFF0000.toInt()

    /**
     * Calculates the interpolated color for a given score.
     *
     * @param score The score to evaluate.
     * @param baseColor The base color (ARGB Int) to interpolate from (usually neutral).
     * @return The calculated ARGB color.
     */
    fun getScoreColor(score: LearningScore, baseColor: Int): Int {
        val balance = score.successes - score.failures
        val percentage = (balance.toFloat() / 10.0f).coerceIn(-1.0f, 1.0f)

        return when {
            percentage > 0 -> lerpColor(baseColor, COLOR_GREEN, percentage)
            percentage < 0 -> lerpColor(baseColor, COLOR_RED, -percentage)
            else -> baseColor
        }
    }

    /**
     * Linearly interpolates between two ARGB colors.
     */
    fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = (startColor shr 24) and 0xff
        val startR = (startColor shr 16) and 0xff
        val startG = (startColor shr 8) and 0xff
        val startB = startColor and 0xff

        val endA = (endColor shr 24) and 0xff
        val endR = (endColor shr 16) and 0xff
        val endG = (endColor shr 8) and 0xff
        val endB = endColor and 0xff

        val a = (startA + fraction * (endA - startA)).toInt()
        val r = (startR + fraction * (endR - startR)).toInt()
        val g = (startG + fraction * (endG - startG)).toInt()
        val b = (startB + fraction * (endB - startB)).toInt()

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
