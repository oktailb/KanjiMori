package org.nihongo.mochi.ui.gojuon

import org.nihongo.mochi.R

class HiraganaQuizFragment : BaseKanaQuizFragment() {

    override fun getXmlResourceId(): Int {
        return R.xml.hiragana
    }

    override fun getQuizModeArgument(): String {
        return arguments?.getString("quizMode") ?: "Romaji -> Kana"
    }

    override fun getLevelArgument(): String {
        return arguments?.getString("level") ?: "Gojūon"
    }
}