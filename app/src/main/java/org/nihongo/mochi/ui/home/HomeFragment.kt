package org.nihongo.mochi.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.nihongo.mochi.R
import org.nihongo.mochi.ui.theme.AppTheme

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    HomeScreen(
                        onRecognitionClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_recognition)
                        },
                        onReadingClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_reading)
                        },
                        onWritingClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_writing)
                        },
                        onDictionaryClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_dictionary)
                        },
                        onResultsClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_results)
                        },
                        onOptionsClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_options)
                        },
                        onAboutClick = {
                            findNavController().navigate(R.id.action_nav_home_to_nav_about)
                        }
                    )
                }
            }
        }
    }
}
