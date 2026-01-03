package org.nihongo.mochi.ui.writingrecap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nihongo.mochi.R
import org.nihongo.mochi.ui.theme.AppTheme

class WritingRecapFragment : Fragment() {

    private val args: WritingRecapFragmentArgs by navArgs()

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
                    val viewModel: WritingRecapViewModel = koinViewModel { parametersOf(baseColor) }

                    // Initial load
                    androidx.compose.runtime.LaunchedEffect(args.level) {
                        viewModel.loadLevel(args.level)
                    }
                    
                    val lifecycleOwner = LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                viewModel.loadLevel(args.level)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                             lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val kanjiList by viewModel.kanjiList.collectAsState()
                    val currentPage by viewModel.currentPage.collectAsState()
                    val totalPages by viewModel.totalPages.collectAsState()
                    
                    WritingRecapScreen(
                        levelTitle = args.level, // Pass the key, resolution is handled in Composable
                        kanjiListWithColors = kanjiList,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onKanjiClick = { kanjiEntry ->
                            val action = WritingRecapFragmentDirections.actionWritingRecapToKanjiDetail(kanjiEntry.id)
                            findNavController().navigate(action)
                        },
                        onPrevPage = { viewModel.prevPage() },
                        onNextPage = { viewModel.nextPage() },
                        onPlayClick = {
                            val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(args.level, null)
                            findNavController().navigate(action)
                        }
                    )
                }
            }
        }
    }
}
