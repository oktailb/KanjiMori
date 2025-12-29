package org.nihongo.mochi.domain.kana

interface ResourceLoader {
    suspend fun loadJson(fileName: String): String
}
