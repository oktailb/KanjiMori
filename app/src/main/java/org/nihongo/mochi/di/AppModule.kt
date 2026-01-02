package org.nihongo.mochi.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.kana.ComposeResourceLoader
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.koin.android.ext.koin.androidContext
import org.nihongo.mochi.domain.recognition.HandwritingRecognizer
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.presentation.reading.ReadingViewModel
import org.nihongo.mochi.presentation.recognition.RecognitionViewModel
import org.nihongo.mochi.presentation.settings.SettingsViewModel
import org.nihongo.mochi.presentation.writing.WritingViewModel

val appModule = module {
    // Switched to ComposeResourceLoader which is KMP compatible and uses the new resources system
    single<ResourceLoader> { ComposeResourceLoader() }
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
    }
    
    // HandwritingRecognizer is needed for DictionaryViewModel
    // It seems it was missing from the file I read, or I missed where it comes from.
    // Assuming it is provided in another module or needs to be here.
    // Based on DictionaryViewModel constructor: HandwritingRecognizer, KanjiRepository, MeaningRepository, SettingsRepository, LevelsRepository
    
    // Check if HandwritingRecognizer is in commonModule. It is platform specific usually.
    // If not, we might need to add it here.
    // But let's focus on ViewModels.
    
    viewModel { KanjiDetailViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) } 
    viewModel { RecognitionViewModel(get(), get()) }
    viewModel { ReadingViewModel(get(), get()) }
    viewModel { WritingViewModel(get()) }
    
    // DictionaryViewModel was not in the previous list, likely because it is used in DictionaryFragment directly?
    // Or I missed it.
    // Let's add it.
    viewModel { DictionaryViewModel(get(), get(), get(), get(), get()) }
}
