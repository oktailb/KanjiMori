package org.nihongo.mochi.ui.reading

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
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.nihongo.mochi.R
import org.nihongo.mochi.domain.statistics.ReadingViewModel
import org.nihongo.mochi.presentation.models.ReadingLevelInfoState
import org.nihongo.mochi.ui.theme.AppTheme

// Wrapper to expose state flow to Compose
class ReadingComposeViewModel : ViewModel() {

    private val _state = MutableStateFlow(ReadingScreenState())
    val state: StateFlow<ReadingScreenState> = _state.asStateFlow()
    
    private val domainViewModel = ReadingViewModel()

    fun refreshData() {
        domainViewModel.calculatePercentages()
        _state.update { 
            ReadingScreenState(
                jlptLevels = listOf(
                    ReadingLevelInfoState("jlpt_wordlist_n5", "N5", domainViewModel.n5Percentage.toInt()),
                    ReadingLevelInfoState("jlpt_wordlist_n4", "N4", domainViewModel.n4Percentage.toInt()),
                    ReadingLevelInfoState("jlpt_wordlist_n3", "N3", domainViewModel.n3Percentage.toInt()),
                    ReadingLevelInfoState("jlpt_wordlist_n2", "N2", domainViewModel.n2Percentage.toInt()),
                    ReadingLevelInfoState("jlpt_wordlist_n1", "N1", domainViewModel.n1Percentage.toInt()),
                ),
                wordLevels = listOf(
                    ReadingLevelInfoState("bccwj_wordlist_1000", "1000", domainViewModel.words1000Percentage.toInt()),
                    ReadingLevelInfoState("bccwj_wordlist_2000", "2000", domainViewModel.words2000Percentage.toInt()),
                    ReadingLevelInfoState("bccwj_wordlist_3000", "3000", domainViewModel.words3000Percentage.toInt()),
                    ReadingLevelInfoState("bccwj_wordlist_4000", "4000", domainViewModel.words4000Percentage.toInt()),
                    ReadingLevelInfoState("bccwj_wordlist_5000", "5000", domainViewModel.words5000Percentage.toInt()),
                    ReadingLevelInfoState("bccwj_wordlist_6000", "6000", domainViewModel.words6000Percentage.toInt()),
                    ReadingLevelInfoState("bccwj_wordlist_7000", "7000", domainViewModel.words7000Percentage.toInt()),
                    ReadingLevelInfoState("bccwj_wordlist_8000", "8000", domainViewModel.words8000Percentage.toInt()),
                ),
                userListInfo = ReadingLevelInfoState(
                    "user_custom_list", 
                    "Liste de l'utilisateur", // Should use resource string in fragment
                    domainViewModel.userListPercentage.toInt()
                )
            )
        }
    }
}

data class ReadingScreenState(
    val jlptLevels: List<ReadingLevelInfoState> = emptyList(),
    val wordLevels: List<ReadingLevelInfoState> = emptyList(),
    val userListInfo: ReadingLevelInfoState = ReadingLevelInfoState("user_custom_list", "", 0)
)

class ReadingFragment : Fragment() {

    private val viewModel: ReadingComposeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val state by viewModel.state.collectAsState()
                    // Update user list display name from resources
                    val userListLabel = getString(R.string.reading_user_list)
                    val uiState = state.copy(
                        userListInfo = state.userListInfo.copy(displayName = userListLabel)
                    )

                    ReadingScreen(
                        jlptLevels = uiState.jlptLevels,
                        wordLevels = uiState.wordLevels,
                        userListInfo = uiState.userListInfo,
                        onLevelClick = { levelId ->
                            navigateToWordList(levelId)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.refreshData()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    private fun navigateToWordList(listId: String) {
        val action = ReadingFragmentDirections.actionNavReadingToWordList(listId)
        findNavController().navigate(action)
    }
}
