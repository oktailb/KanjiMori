package org.nihongo.mochi.ui.gojuon

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.get
import org.nihongo.mochi.domain.game.KanaQuizViewModel
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.ui.theme.AppTheme

abstract class BaseKanaQuizFragment : Fragment() {

    protected val viewModel: KanaQuizViewModel by viewModels {
        viewModelFactory {
            initializer {
                KanaQuizViewModel(get())
            }
        }
    }

    protected lateinit var sharedPreferences: SharedPreferences

    // Abstract property to specify Kana Type (Hiragana or Katakana)
    abstract val kanaType: org.nihongo.mochi.domain.kana.KanaType

    abstract fun getQuizModeArgument(): String
    abstract fun getLevelArgument(): String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    KanaQuizScreen(
                        viewModel = viewModel,
                        onNavigateBack = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        viewModel.setAnimationSpeed(animationSpeed)

        if (!viewModel.isGameInitialized) {
            val success = viewModel.initializeGame(kanaType, getQuizModeArgument(), getLevelArgument())
            if (!success) {
                findNavController().popBackStack()
                return
            }
        }
    }
}
