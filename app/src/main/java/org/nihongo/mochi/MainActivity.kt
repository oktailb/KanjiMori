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
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import org.nihongo.mochi.databinding.ActivityMainBinding

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
