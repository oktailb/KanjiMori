package org.nihongo.mochi.ui.grammar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.ui.theme.AppTheme

class GrammarFragment : Fragment() {

    private val viewModel: GrammarViewModel by viewModel()
    private val args: GrammarFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Trigger data loading with the passed level ID
        viewModel.loadGraph(args.levelId)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    GrammarScreen(
                        viewModel = viewModel,
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}
