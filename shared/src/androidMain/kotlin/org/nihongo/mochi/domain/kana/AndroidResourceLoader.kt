package org.nihongo.mochi.domain.kana

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class AndroidResourceLoader(private val context: Context) : ResourceLoader {
    override fun loadJson(fileName: String): String {
        val assetPath = "kana/$fileName"
        
        // 1. Try Assets
        try {
            Log.d("Mochi", "Attempting to load from assets: $assetPath")
            return context.assets.open(assetPath).use { stream ->
                InputStreamReader(stream).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Log.w("Mochi", "Failed to load from assets: ${e.message}")
        }

        // 2. Try Java Resources (ClassLoader)
        try {
            val resourcePath = "/kana/$fileName"
            Log.d("Mochi", "Attempting to load from resources: $resourcePath")
            val stream = this::class.java.getResourceAsStream(resourcePath) 
                ?: this::class.java.classLoader?.getResourceAsStream("kana/$fileName")
            
            if (stream != null) {
                return stream.use { s ->
                    InputStreamReader(s).use { reader ->
                        reader.readText()
                    }
                }
            } else {
                Log.e("Mochi", "Resource stream is null for $resourcePath")
            }
        } catch (e: Exception) {
            Log.e("Mochi", "Failed to load from resources: ${e.message}")
        }

        return "{}"
    }
}
