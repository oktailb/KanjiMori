package org.nihongo.mochi.ui.gamerecap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nihongo.mochi.R
import org.nihongo.mochi.ui.theme.AppTheme

class GameRecapFragment : Fragment() {

    private val args: GameRecapFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val baseColor = ContextCompat.getColor(requireContext(), R.color.recap_grid_base_color)
                    val viewModel: GameRecapViewModel = koinViewModel { parametersOf(baseColor) }
                    
                    GameRecapFragmentContent(viewModel)
                }
            }
        }
    }

    @Composable
    private fun GameRecapFragmentContent(viewModel: GameRecapViewModel) {
        val kanjiWithColors by viewModel.kanjiListWithColors.collectAsState()
        val currentPage by viewModel.currentPage.collectAsState()
        val totalPages by viewModel.totalPages.collectAsState()
        
        var gameMode by remember { mutableStateOf("meaning") }
        var readingMode by remember { mutableStateOf("common") }
        
        val isMeaningEnabled = args.level != "No Meaning"
        val isReadingEnabled = args.level != "No Reading"

        LaunchedEffect(args.level, gameMode) {
            viewModel.loadLevel(args.level, gameMode)
            if (!isMeaningEnabled) gameMode = "reading"
            if (!isReadingEnabled) gameMode = "meaning"
        }
        
        GameRecapScreen(
            levelTitle = args.level,
            kanjiListWithColors = kanjiWithColors,
            currentPage = currentPage,
            totalPages = totalPages,
            gameMode = gameMode,
            readingMode = readingMode,
            isMeaningEnabled = isMeaningEnabled,
            isReadingEnabled = isReadingEnabled,
            onKanjiClick = {
                val action = GameRecapFragmentDirections.actionGameRecapToKanjiDetail(it.id)
                findNavController().navigate(action)
            },
            onPrevPage = { viewModel.prevPage(gameMode) },
            onNextPage = { viewModel.nextPage(gameMode) },
            onGameModeChange = { gameMode = it },
            onReadingModeChange = { readingMode = it },
            onPlayClick = {
                val action = GameRecapFragmentDirections.actionGameRecapToRecognitionGame(
                    level = args.level,
                    gameMode = gameMode,
                    readingMode = readingMode,
                    customWordList = null
                )
                findNavController().navigate(action)
            }
        )
    }
}
