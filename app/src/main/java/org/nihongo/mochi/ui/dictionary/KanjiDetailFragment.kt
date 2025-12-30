package org.nihongo.mochi.ui.dictionary

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.R
import org.nihongo.mochi.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class KanjiDetailFragment : Fragment() {

    private val args: KanjiDetailFragmentArgs by navArgs()
    private val viewModel: KanjiDetailViewModel by viewModel()
    
    // Font handling
    private var kanjiStrokeOrderTypeface: Typeface? = null
    private val fontFileName = "KanjiStrokeOrders_v4.005.ttf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadCustomFont()
    }

    private fun loadCustomFont() {
        try {
            // Attempt 1: Load from Assets
            kanjiStrokeOrderTypeface = Typeface.createFromAsset(requireContext().assets, "fonts/$fontFileName")
        } catch (e: Exception) {
            Log.e("KanjiDetailFragment", "Could not load font from assets: $e")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    KanjiDetailScreen(
                        viewModel = viewModel,
                        kanjiStrokeOrderTypeface = kanjiStrokeOrderTypeface,
                        onKanjiClick = { kanjiChar -> navigateToKanji(kanjiChar) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load initial data
        args.kanjiId?.let { viewModel.loadKanji(it) }
    }

    private fun navigateToKanji(character: String) {
        // Navigation logic kept in Fragment as it depends on Android Navigation Component
        // ViewModel helper is used to find ID
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val id = viewModel.findKanjiIdByCharacter(character)
            withContext(Dispatchers.Main) {
                if (id != null) {
                    try {
                        val action = KanjiDetailFragmentDirections.actionKanjiDetailToKanjiDetail(id)
                        findNavController().navigate(action)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
