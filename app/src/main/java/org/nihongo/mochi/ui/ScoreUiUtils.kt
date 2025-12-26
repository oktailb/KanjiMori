package org.nihongo.mochi.ui

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import org.nihongo.mochi.R
import org.nihongo.mochi.data.KanjiScore

object ScoreUiUtils {

    fun getScoreColor(context: Context, score: KanjiScore): Int {
        val balance = score.successes - score.failures
        val percentage = (balance.toFloat() / 10.0f).coerceIn(-1.0f, 1.0f)
        val baseColor = ContextCompat.getColor(context, R.color.recap_grid_base_color)

        return when {
            percentage > 0 -> lerpColor(baseColor, Color.GREEN, percentage)
            percentage < 0 -> lerpColor(baseColor, Color.RED, -percentage)
            else -> baseColor
        }
    }

    private fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)

        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)

        val a = (startA + fraction * (endA - startA)).toInt()
        val r = (startR + fraction * (endR - startR)).toInt()
        val g = (startG + fraction * (endG - startG)).toInt()
        val b = (startB + fraction * (endB - startB)).toInt()

        return Color.argb(a, r, g, b)
    }
}
