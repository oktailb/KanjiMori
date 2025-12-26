package org.nihongo.mochi.domain.kana

import kotlinx.serialization.Serializable

@Serializable
data class KanaEntry(
    val character: String,
    val romaji: String,
    val type: KanaType,
    val line: Int = 0
)

enum class KanaType {
    HIRAGANA,
    KATAKANA
}

@Serializable
data class KanaData(
    val characters: List<KanaEntry>
)
