package org.nihongo.mochi

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import org.nihongo.mochi.databinding.ActivityMainBinding
import org.nihongo.mochi.workers.DecayWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var gamesSignInClient: GamesSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure default language is applied on first launch if not set
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        if (!sharedPreferences.contains("AppLocale")) {
            val defaultLocale = "en_GB"
            sharedPreferences.edit().putString("AppLocale", defaultLocale).apply()
            val localeTag = defaultLocale.replace('_', '-')
            val appLocale = LocaleListCompat.forLanguageTags(localeTag)
            AppCompatDelegate.setApplicationLocales(appLocale)
        } else {
             // Apply stored locale
             val savedLocale = sharedPreferences.getString("AppLocale", "en_GB")!!
             val localeTag = savedLocale.replace('_', '-')
             if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != localeTag) {
                  val appLocale = LocaleListCompat.forLanguageTags(localeTag)
                  AppCompatDelegate.setApplicationLocales(appLocale)
             }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        gamesSignInClient = PlayGames.getGamesSignInClient(this)
        
        setupWorkers()
    }
    
    private fun setupWorkers() {
        // Run the decay check periodically (e.g., every day to check if a week has passed for any item)
        // Minimum interval for PeriodicWorkRequest is 15 minutes.
        // We can run it once a day. The worker logic checks the timestamps.
        
        val decayWorkRequest = PeriodicWorkRequestBuilder<DecayWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS) // Don't run immediately on first install
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MochiDecayWork",
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing schedule if already set
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
                // The player is signed in. You can now use the Games SDK.
            } else {
                Log.d("MainActivity", "Google Play Games sign-in failed or not authenticated.")
                // Player could not be signed in.
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
