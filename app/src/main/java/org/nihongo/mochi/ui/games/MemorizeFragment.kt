package org.nihongo.mochi.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.compose.koinViewModel
import org.nihongo.mochi.ui.games.memorize.MemorizeGameScreen
import org.nihongo.mochi.ui.games.memorize.MemorizeSetupScreen
import org.nihongo.mochi.ui.games.memorize.MemorizeViewModel
import org.nihongo.mochi.ui.theme.AppTheme

class MemorizeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val viewModel: MemorizeViewModel = koinViewModel()
                    var isPlaying by remember { mutableStateOf(false) }

                    if (!isPlaying) {
                        MemorizeSetupScreen(
                            viewModel = viewModel,
                            onBackClick = { findNavController().popBackStack() },
                            onStartGame = { isPlaying = true }
                        )
                    } else {
                        MemorizeGameScreen(
                            viewModel = viewModel,
                            onBackClick = { isPlaying = false }
                        )
                    }
                }
            }
        }
    }
}
