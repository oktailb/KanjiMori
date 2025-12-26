package org.nihongo.mochi

import android.app.Application
import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kana.AndroidResourceLoader
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.domain.kana.RomajiToKana

class MochiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Init Settings
        val scoresSettings = SharedPreferencesSettings(getSharedPreferences("scores", Context.MODE_PRIVATE))
        val userListSettings = SharedPreferencesSettings(getSharedPreferences("user_lists", Context.MODE_PRIVATE))
        val appSettings = SharedPreferencesSettings(getSharedPreferences("settings", Context.MODE_PRIVATE))
        
        ScoreManager.init(scoresSettings, userListSettings, appSettings)

        // Init Kana Domain
        val resourceLoader = AndroidResourceLoader(this)
        val kanaRepository = KanaRepository(resourceLoader)
        
        KanaToRomaji.init(kanaRepository)
        RomajiToKana.init(kanaRepository)
    }
}
