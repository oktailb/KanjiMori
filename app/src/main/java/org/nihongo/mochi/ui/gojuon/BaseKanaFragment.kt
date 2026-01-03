package org.nihongo.mochi.ui.gojuon

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nihongo.mochi.R
import org.nihongo.mochi.ui.theme.AppTheme

abstract class BaseKanaFragment : Fragment() {

    abstract val kanaType: KanaFragmentType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val baseColor = ContextCompat.getColor(context, R.color.recap_grid_base_color)
                    val viewModel: KanaRecapViewModel = koinViewModel { parametersOf(baseColor) }
                    
                    val configuration = LocalConfiguration.current
                    val pageSize = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 16

                    // Initial load
                    androidx.compose.runtime.LaunchedEffect(kanaType, pageSize) {
                        viewModel.loadKana(kanaType.domainType, pageSize)
                    }
                    
                    val lifecycleOwner = LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                viewModel.refreshScores()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                             lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val charactersByLine by viewModel.charactersByLine.collectAsState()
                    val linesToShow by viewModel.linesToShow.collectAsState()
                    val kanaColors by viewModel.kanaColors.collectAsState()
                    val currentPage by viewModel.currentPage.collectAsState()
                    val totalPages by viewModel.totalPages.collectAsState()

                    KanaRecapScreen(
                        title = getString(kanaType.titleRes),
                        kanaListWithColors = emptyList(), // Not used in this specific screen variant
                        linesToShow = linesToShow,
                        charactersByLine = charactersByLine,
                        kanaColors = kanaColors,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPrevPage = { viewModel.prevPage(pageSize) },
                        onNextPage = { viewModel.nextPage(pageSize) },
                        onPlayClick = {
                            findNavController().navigate(kanaType.navigationActionId)
                        }
                    )
                }
            }
        }
    }
}

enum class KanaFragmentType(val domainType: org.nihongo.mochi.domain.kana.KanaType, val titleRes: Int, val navigationActionId: Int) {
    HIRAGANA(org.nihongo.mochi.domain.kana.KanaType.HIRAGANA, R.string.level_hiragana, R.id.action_nav_hiragana_to_hiragana_quiz),
    KATAKANA(org.nihongo.mochi.domain.kana.KanaType.KATAKANA, R.string.level_katakana, R.id.action_nav_katakana_to_katakana_quiz)
}
