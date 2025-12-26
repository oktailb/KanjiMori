package org.nihongo.mochi

import android.app.Application
import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nihongo.mochi.data.ScoreManager

class MochiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val scoresPrefs = getSharedPreferences("NihongoMochiScores", Context.MODE_PRIVATE)
        val userListsPrefs = getSharedPreferences("NihongoMochiUserLists", Context.MODE_PRIVATE)
        val appSettingsPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Migration logic for User Lists (Set<String> -> JSON String)
        migrateUserLists(userListsPrefs)

        val scoresSettings = SharedPreferencesSettings(scoresPrefs)
        val userListSettings = SharedPreferencesSettings(userListsPrefs)
        val appSettings = SharedPreferencesSettings(appSettingsPrefs)

        ScoreManager.init(scoresSettings, userListSettings, appSettings)
    }

    private fun migrateUserLists(prefs: android.content.SharedPreferences) {
        val migratedKey = "MIGRATED_TO_JSON"
        if (prefs.getBoolean(migratedKey, false)) return

        val editor = prefs.edit()
        val allEntries = prefs.all

        for ((key, value) in allEntries) {
            if (key == migratedKey) continue
            if (value is Set<*>) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val set = value as Set<String>
                    val jsonList = Json.encodeToString(set)
                    editor.putString(key, jsonList)
                    // We keep the old key/value? SharedPreferences allows storing different types for same key?
                    // No, we must overwrite or remove. 
                    // putString will overwrite the Set if the key is the same.
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        editor.putBoolean(migratedKey, true)
        editor.apply()
    }
}
