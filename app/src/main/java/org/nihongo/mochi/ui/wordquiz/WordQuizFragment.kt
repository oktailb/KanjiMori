package org.nihongo.mochi.ui.wordquiz

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.android.ext.android.get
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY

class WordQuizFragment : Fragment() {

    private val args: WordQuizFragmentArgs by navArgs()
    
    private val viewModel: WordQuizViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WordQuizViewModel(
                    wordRepository = get()
                ) as T
            }
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.state.collectAsState()
                val buttonStates by viewModel.buttonStates.collectAsState()
                val currentWord by viewModel.currentWord.collectAsState()
                val currentAnswers by viewModel.currentAnswers.collectAsState()
                val wordStatuses by viewModel.wordStatuses.collectAsState()
                val buttonsEnabled by viewModel.areButtonsEnabled.collectAsState()
                
                WordQuizScreen(
                    wordToGuess = currentWord?.text,
                    gameStatus = wordStatuses,
                    answers = currentAnswers,
                    buttonStates = buttonStates,
                    buttonsEnabled = buttonsEnabled,
                    onAnswerClick = { index, answer ->
                        viewModel.submitAnswer(answer, index)
                    }
                )
                
                if (state is GameState.Finished) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pronunciationMode = sharedPreferences.getString(PRONUNCIATION_PREF_KEY, "Hiragana") ?: "Hiragana"
        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        viewModel.updateSettings(pronunciationMode, animationSpeed)

        val customWordList = args.customWordList?.toList()
        viewModel.initializeGame(customWordList)
    }
}
