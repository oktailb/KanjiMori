package org.nihongo.mochi.ui.gamerecap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.ui.theme.AppTheme

class GameRecapFragment : Fragment() {

    private val args: GameRecapFragmentArgs by navArgs()
    private val levelContentProvider: LevelContentProvider by inject()
    private val kanjiRepository: KanjiRepository by inject()

    private val pageSize = 80 // 8 columns * 10 rows

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    GameRecapFragmentContent()
                }
            }
        }
    }

    @Composable
    private fun GameRecapFragmentContent() {
        var kanjiList by remember { mutableStateOf<List<KanjiEntry>>(emptyList()) }
        var currentPage by remember { mutableStateOf(0) }
        var gameMode by remember { mutableStateOf("meaning") }
        var readingMode by remember { mutableStateOf("common") }
        
        // Determine initial and enabled states for selectors
        val isMeaningEnabled = args.level != "No Meaning"
        val isReadingEnabled = args.level != "No Reading"

        LaunchedEffect(args.level) {
            kanjiList = loadKanjiForLevel(args.level)
            // Set initial game mode based on what's available
            if (!isMeaningEnabled) gameMode = "reading"
            if (!isReadingEnabled) gameMode = "meaning"
        }
        
        val totalPages = if (kanjiList.isEmpty()) 0 else (kanjiList.size - 1) / pageSize + 1
        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(kanjiList.size)
        val currentKanjiSublist = kanjiList.subList(startIndex, endIndex)

        val baseColor = ContextCompat.getColor(requireContext(), R.color.recap_grid_base_color)
        val kanjiWithColors = currentKanjiSublist.map { kanjiEntry ->
            val score = ScoreManager.getScore(kanjiEntry.character, ScoreManager.ScoreType.RECOGNITION)
            kanjiEntry to Color(ScorePresentationUtils.getScoreColor(score, baseColor))
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
            onPrevPage = { if (currentPage > 0) currentPage-- },
            onNextPage = { if (endIndex < kanjiList.size) currentPage++ },
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
    
    private fun loadKanjiForLevel(levelKey: String): List<KanjiEntry> {
        val characters = levelContentProvider.getCharactersForLevel(levelKey)
        return characters.mapNotNull { kanjiRepository.getKanjiByCharacter(it) }
    }
}
