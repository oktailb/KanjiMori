package org.nihongo.mochi.domain.kana

object KanaToRomaji {
    private val kanaMap = HashMap<String, String>()
    private var isInitialized = false

    fun init(repository: KanaRepository) {
        if (isInitialized) return
        
        // Load from repository
        repository.getKanaEntries(KanaType.HIRAGANA).forEach { entry ->
            kanaMap[entry.character] = entry.romaji
        }
        repository.getKanaEntries(KanaType.KATAKANA).forEach { entry ->
            kanaMap[entry.character] = entry.romaji
        }
        
        // Add special symbol mapping that might not be in the XML/JSON if it was hardcoded before
        kanaMap["ー"] = "-"
        isInitialized = true
    }

    fun convert(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            // Check 2 chars (e.g. kya, sha, cho)
            if (i + 1 < text.length) {
                val pair = text.substring(i, i + 2)
                if (kanaMap.containsKey(pair)) {
                    sb.append(kanaMap[pair])
                    i += 2
                    continue
                }
            }
            
            // Check 1 char
            val single = text.substring(i, i + 1)
            if (kanaMap.containsKey(single)) {
                sb.append(kanaMap[single])
            } else {
                // If sokuon (small tsu)
                if (single == "っ" || single == "ッ") {
                    if (i + 1 < text.length) {
                        // Look ahead for next character's romaji to double the consonant
                        var nextRomaji: String? = null
                        // Try to find romaji for next 2 chars first (e.g. っkya)
                        if (i + 2 < text.length && kanaMap.containsKey(text.substring(i + 1, i + 3))) {
                             nextRomaji = kanaMap[text.substring(i + 1, i + 3)]
                        } else if (i + 1 < text.length && kanaMap.containsKey(text.substring(i + 1, i + 2))) {
                             nextRomaji = kanaMap[text.substring(i + 1, i + 2)]
                        }
                        
                        if (nextRomaji != null && nextRomaji.isNotEmpty()) {
                            sb.append(nextRomaji[0])
                        }
                    }
                } else {
                    sb.append(single) // Keep original if not found (kanji, punctuation)
                }
            }
            i++
        }
        return sb.toString()
    }
}
