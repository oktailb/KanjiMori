package org.nihongo.mochi.domain.kana

interface ResourceLoader {
    fun loadJson(fileName: String): String
}
