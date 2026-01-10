package org.nihongo.mochi.ui.games.memorize

import kotlinx.serialization.Serializable
import org.nihongo.mochi.domain.kanji.KanjiEntry

data class MemorizeCardState(
    val id: Int,
    val kanji: KanjiEntry,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

enum class MemorizeGameMode {
    KANJI_KANJI,
}

data class MemorizeGridSize(
    val rows: Int,
    val cols: Int
) {
    val totalCards: Int get() = rows * cols
    val pairsCount: Int get() = totalCards / 2
    
    override fun toString(): String = "${cols}x${rows}"
}

@Serializable
data class MemorizeGameResult(
    val moves: Int,
    val totalPairs: Int,
    val gridSizeLabel: String,
    val timeSeconds: Int,
    val timestamp: Long
)
