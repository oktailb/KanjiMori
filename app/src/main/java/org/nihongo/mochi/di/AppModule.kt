package org.nihongo.mochi.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module
import org.nihongo.mochi.domain.kana.ComposeResourceLoader
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.koin.android.ext.koin.androidContext

val appModule = module {
    // Switched to ComposeResourceLoader which is KMP compatible and uses the new resources system
    single<ResourceLoader> { ComposeResourceLoader() }
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
    }
}
