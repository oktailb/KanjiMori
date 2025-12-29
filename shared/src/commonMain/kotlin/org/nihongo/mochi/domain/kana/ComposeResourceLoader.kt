package org.nihongo.mochi.domain.kana

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.nihongo.mochi.shared.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
class ComposeResourceLoader : ResourceLoader {
    override suspend fun loadJson(fileName: String): String {
        // "files/" prefix is added because we moved resources to "composeResources/files/"
        // Compose Resources accesses them via "files/path/to/file"
        val path = "files/$fileName"
        return try {
            val bytes = Res.readBytes(path)
            bytes.decodeToString()
        } catch (e: Exception) {
            println("Error loading resource $path: ${e.message}")
            "{}"
        }
    }
}
