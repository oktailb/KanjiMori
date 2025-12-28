package org.nihongo.mochi.domain.kana

object KanaUtils {

    /**
     * Converts hiragana characters in the string to katakana.
     * Characters that are not hiragana are left unchanged.
     */
    fun hiraganaToKatakana(s: String): String {
        return s.map { c ->
            if (c in '\u3041'..'\u3096') {
                (c + 0x60)
            } else {
                c
            }
        }.joinToString("")
    }

    /**
     * Converts katakana characters in the string to hiragana.
     * Characters that are not katakana are left unchanged.
     */
    fun katakanaToHiragana(s: String): String {
        return s.map { c ->
            if (c in '\u30A1'..'\u30F6') {
                (c - 0x60)
            } else {
                c
            }
        }.joinToString("")
    }
}
