package org.nihongo.mochi

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.android.inject
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.workers.DecayWorker
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var gamesSignInClient: GamesSignInClient
    
    // Injecting here works for onCreate, but for attachBaseContext we need manual retrieval
    private val settingsRepository: SettingsRepository by inject()

    override fun attachBaseContext(newBase: Context) {
        // Manual locale application for older Android versions (API < 33)
        // We can't use Koin injection here yet as the context isn't fully ready or Koin might rely on it.
        // We manually read SharedPreferences to be safe and fast.
        val prefs = newBase.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLocaleCode = prefs.getString("AppLocale", "en_GB") ?: "en_GB"
        val localeTag = savedLocaleCode.replace('_', '-')
        
        val localeList = LocaleListCompat.forLanguageTags(localeTag)
        // This helps AppCompatDelegate know what we want before it restores state
        AppCompatDelegate.setApplicationLocales(localeList)
        
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply stored theme
        val savedTheme = settingsRepository.getTheme()
        val nightMode = if (savedTheme == "dark") {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            // Default to system if not set or "light" (assuming default is light/system)
            // If you want to force light when "light" is stored:
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
        
        // Redundant check but good for runtime changes while app is open
        val savedLocale = settingsRepository.getAppLocale()
        val localeTag = savedLocale.replace('_', '-')
        
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != localeTag) {
             val appLocale = LocaleListCompat.forLanguageTags(localeTag)
             AppCompatDelegate.setApplicationLocales(appLocale)
        }

        setContentView(R.layout.activity_main)

        // Ensure NavHostFragment is properly set up
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
        if (navHostFragment == null) {
            Log.e("MainActivity", "NavHostFragment not found!")
        }

        gamesSignInClient = PlayGames.getGamesSignInClient(this)
        
        setupWorkers()
    }
    
    private fun setupWorkers() {
        val decayWorkRequest = PeriodicWorkRequestBuilder<DecayWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MochiDecayWork",
            ExistingPeriodicWorkPolicy.KEEP,
            decayWorkRequest
        )
    }

    override fun onResume() {
        super.onResume()
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask ->
            if (isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated) {
                Log.d("MainActivity", "Google Play Games sign-in successful.")
            } else {
                Log.d("MainActivity", "Google Play Games sign-in failed or not authenticated.")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // No AppBarConfiguration needed anymore as we don't have an ActionBar
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
