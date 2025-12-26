package org.nihongo.mochi.ui.gojuon

import org.nihongo.mochi.domain.kana.KanaType

class KatakanaQuizFragment : BaseKanaQuizFragment() {

    override val kanaType: KanaType = KanaType.KATAKANA

    override fun getQuizModeArgument(): String {
        return arguments?.getString("quizMode") ?: "Romaji -> Kana"
    }

    override fun getLevelArgument(): String {
        return arguments?.getString("level") ?: "Gojūon"
    }
}
