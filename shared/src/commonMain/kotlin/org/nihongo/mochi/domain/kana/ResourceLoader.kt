package org.nihongo.mochi.domain.kana

import org.jetbrains.compose.resources.ExperimentalResourceApi

interface ResourceLoader {
    suspend fun loadJson(fileName: String): String
}
