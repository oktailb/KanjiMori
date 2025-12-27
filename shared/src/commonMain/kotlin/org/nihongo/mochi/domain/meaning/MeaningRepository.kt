package org.nihongo.mochi.domain.meaning

import kotlinx.serialization.json.Json
import org.nihongo.mochi.domain.kana.ResourceLoader

class MeaningRepository(private val resourceLoader: ResourceLoader) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cachedMeanings = mutableMapOf<String, Map<String, List<String>>>()

    fun getMeanings(locale: String): Map<String, List<String>> {
        val fileName = getFileName(locale)
        if (cachedMeanings.containsKey(locale)) {
            return cachedMeanings[locale]!!
        }

        return try {
            val jsonString = resourceLoader.loadJson(fileName)
            // If resource loader returns empty object string for missing file, handle it
            if (jsonString == "{}") throw Exception("File not found or empty: $fileName")
            
            val root = json.decodeFromString<MeaningRoot>(jsonString)
            val meaningsMap = root.meanings.kanji.associate { it.id to it.meaning }
            cachedMeanings[locale] = meaningsMap
            meaningsMap
        } catch (e: Exception) {
            println("Error parsing meaning list for $locale (file: $fileName): ${e.message}")
            // Fallback to english (GB) if the locale is not found
            // We use "en_GB" which corresponds to "meanings-en-rGB.json" via getFileName
            if (locale != "en_GB" && locale != "en-GB" && locale != "en") {
                getMeanings("en_GB")
            } else {
                emptyMap()
            }
        }
    }

    private fun getFileName(locale: String): String {
        // Convert "en_GB" or "en-GB" to "en-rGB"
        // Convert "fr_FR" to "fr-rFR"
        val parts = locale.replace("-", "_").split("_")
        val formattedSuffix = if (parts.size >= 2) {
            "${parts[0]}-r${parts[1]}"
        } else {
            locale
        }
        return "meanings/meanings-$formattedSuffix.json"
    }
}
