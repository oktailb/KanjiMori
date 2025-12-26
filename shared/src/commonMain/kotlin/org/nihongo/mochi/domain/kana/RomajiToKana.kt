package org.nihongo.mochi.domain.kana

object RomajiToKana {
    private val m = HashMap<String, String>()
    private var isInitialized = false

    fun init(repository: KanaRepository) {
        if (isInitialized) return
        
        // Load Hiragana from repository
        repository.getKanaEntries(KanaType.HIRAGANA).forEach { entry ->
            m[entry.romaji] = entry.character
        }
        
        // FIX: Remove single 'n' from map because it prevents typing 'na', 'ni', etc.
        // 'n' should only be converted when followed by a consonant (handled in checkReplacement)
        // or when typed as 'nn'.
        m.remove("n")
        
        // Add special/approximate mappings not present in standard list or duplicates
        // Note: The repository might contain duplicate romaji keys (e.g. "ji" for both じ and ぢ), 
        // the last one loaded wins or we need to be careful.
        // In standard Hiragana/Katakana tables:
        // ji -> じ (Z-row) is usually preferred over ぢ (D-row) for input
        // zu -> ず (Z-row) is usually preferred over づ (D-row)
        
        // Let's ensure standard input mappings
        m["ji"] = "じ"
        m["zu"] = "ず"
        
        // Special inputs / alternate romaji
        m["si"] = "し"
        m["ti"] = "ち"
        m["tu"] = "つ"
        m["hu"] = "ふ"
        m["zi"] = "じ"
        m["di"] = "ぢ"
        m["du"] = "づ"
        
        // Archaic / approximations
        m["yi"] = "い"
        m["ye"] = "いe" 
        
        // Small tsu variants
        // m["-"] = "ー" // Usually handled by checkReplacement logic for elongation? 
        // Original code had m["-"] = "ー"
        m["-"] = "ー"
        m["nn"] = "ん"
        
        // Yoon shortcuts
        m["jya"] = "じゃ"; m["jyu"] = "じゅ"; m["jyo"] = "じょ"
        // Others are likely covered by standard repository data (kya, sha, cha, etc.)
        
        isInitialized = true
    }

    // Returns a Pair<Int, String> where Int is the number of characters at the end of text to replace,
    // and String is the replacement. Returns null if no replacement found.
    fun checkReplacement(text: String): Pair<Int, String>? {
        val len = text.length
        if (len == 0) return null
        
        // Priority 1: 3 chars (e.g. kya)
        if (len >= 3) {
            val suffix = text.substring(len - 3)
            if (m.containsKey(suffix)) return Pair(3, m[suffix]!!)
        }
        
        // Priority 2: 2 chars (e.g. ka, nn, or special n+consonant, or double consonant)
        if (len >= 2) {
            val suffix = text.substring(len - 2)
            if (m.containsKey(suffix)) return Pair(2, m[suffix]!!)
            
            val c1 = suffix[0]
            val c2 = suffix[1]
            
            // n + consonant (except y, which waits for nya)
            // vowels and n are not consonants in this context
            if (c1 == 'n' && c2 !in "aiueony") {
                return Pair(2, "ん$c2")
            }
            
            // Double consonant (small tsu)
            if (c1 == c2 && c1 !in "aiueon") {
                return Pair(2, "っ$c2")
            }
        }
        
        // Priority 3: 1 char (e.g. a)
        if (len >= 1) {
            val suffix = text.substring(len - 1)
            if (m.containsKey(suffix)) return Pair(1, m[suffix]!!)
        }
        
        return null
    }
}
