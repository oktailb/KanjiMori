package org.nihongo.mochi.ui.gojuon

import org.nihongo.mochi.domain.kana.KanaType

class HiraganaQuizFragment : BaseKanaQuizFragment() {

    override val kanaType: KanaType = KanaType.HIRAGANA

    override fun getQuizModeArgument(): String {
        return arguments?.getString("quizMode") ?: "Romaji -> Kana"
    }

    override fun getLevelArgument(): String {
        return arguments?.getString("level") ?: "Gojūon"
    }
}
