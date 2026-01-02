package org.nihongo.mochi.domain.kanji

import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository

class KanjiRepository(
    private val resourceLoader: ResourceLoader,
    private val meaningRepository: MeaningRepository,
    private val settingsRepository: SettingsRepository
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedKanji: List<KanjiEntry>? = null

    fun getAllKanji(): List<KanjiEntry> {
        return runBlocking {
            getAllKanjiSuspend()
        }
    }

    suspend fun getAllKanjiSuspend(): List<KanjiEntry> {
        if (cachedKanji != null) return cachedKanji!!
        
        val jsonString = resourceLoader.loadJson("kanji/kanji_details.json")
        return try {
            val root = json.decodeFromString<KanjiDetailsRoot>(jsonString)
            cachedKanji = root.kanjiDetails.kanji
            cachedKanji!!
        } catch (e: Exception) {
            println("Error parsing kanji details: ${e.message}")
            emptyList()
        }
    }

    fun getKanjiById(id: String): KanjiEntry? {
        return getAllKanji().find { it.id == id }
    }
    
    fun getKanjiByCharacter(char: String): KanjiEntry? {
        return getAllKanji().find { it.character == char }
    }
    
    /**
     * Finds Kanji that match a specific level tag (e.g. "n5", "grade1").
     * Since level tags are unique across categories in our data, we don't need the category.
     */
    fun getKanjiByLevel(levelId: String): List<KanjiEntry> {
        return getAllKanji().filter { kanji ->
            kanji.level.any { it.equals(levelId, ignoreCase = true) }
        }
    }
    
    // Legacy support if needed, but redirects to simple level check if category is ignored
    fun getKanjiByLevel(levelType: String, levelValue: String): List<KanjiEntry> {
        // If strict category check is needed:
        // return getAllKanji().filter { it.category.contains(levelType) && it.level.contains(levelValue) }
        // But for now, simple level check is enough as IDs are unique enough
        return getKanjiByLevel(levelValue)
    }

    fun getNativeKanji(): List<KanjiEntry> {
        return getAllKanji().filter { 
            it.category.isEmpty() && it.readings?.reading?.isNotEmpty() == true
        }
    }

    fun getNoReadingKanji(): List<KanjiEntry> {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        return getAllKanji().filter { 
            it.category.isEmpty() && it.readings?.reading?.isEmpty() == true && meanings.containsKey(it.id)
        }
    }

    fun getNoMeaningKanji(): List<KanjiEntry> {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        return getAllKanji().filter { 
            it.category.isEmpty() && !meanings.containsKey(it.id)
        }
    }
}
