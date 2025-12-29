package org.nihongo.mochi.domain.kana

import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking

class KanaRepository(private val resourceLoader: ResourceLoader) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Cache to avoid reloading/parsing every time (optional optimization)
    private var hiraganaCache: List<KanaEntry>? = null
    private var katakanaCache: List<KanaEntry>? = null

    fun getKanaEntries(type: KanaType): List<KanaEntry> {
        // Compose Resources readBytes is suspendable.
        // Since this repository is likely called from UI context or non-suspend paths currently,
        // we use runBlocking for now as a bridge during migration.
        // Ideally, the whole chain up to ViewModel should be suspendable.
        return runBlocking {
            getKanaEntriesSuspend(type)
        }
    }

    suspend fun getKanaEntriesSuspend(type: KanaType): List<KanaEntry> {
        if (type == KanaType.HIRAGANA && hiraganaCache != null) return hiraganaCache!!
        if (type == KanaType.KATAKANA && katakanaCache != null) return katakanaCache!!

        val fileName = when (type) {
            KanaType.HIRAGANA -> "kana/hiragana.json"
            KanaType.KATAKANA -> "kana/katakana.json"
        }
        
        val jsonString = resourceLoader.loadJson(fileName)
        val result = try {
            json.decodeFromString<KanaData>(jsonString).characters
        } catch (e: Exception) {
            println("Error parsing kana data: ${e.message}")
            emptyList()
        }
        
        if (type == KanaType.HIRAGANA) hiraganaCache = result
        if (type == KanaType.KATAKANA) katakanaCache = result
        
        return result
    }
}
