package org.oktail.kanjimori.ui.recognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentRecognitionBinding

class RecognitionFragment : Fragment() {

    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    // Variables to hold the percentage values
    private var hiraganaPercentage = 0.0
    private var katakanaPercentage = 0.0
    private var n5Percentage = 0.0
    private var n4Percentage = 0.0
    private var n3Percentage = 0.0
    private var n2Percentage = 0.0
    private var n1Percentage = 0.0
    private var class1Percentage = 0.0
    private var class2Percentage = 0.0
    private var class3Percentage = 0.0
    private var class4Percentage = 0.0
    private var class5Percentage = 0.0
    private var class6Percentage = 0.0
    private var test4Percentage = 0.0
    private var test3Percentage = 0.0
    private var testPre2Percentage = 0.0
    private var test2Percentage = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateButtonText()
        setupClickListeners()

        return root
    }

    private fun setupClickListeners() {
        binding.buttonHiragana.setOnClickListener { navigateToRecap("Hiragana") }
        binding.buttonKatakana.setOnClickListener { navigateToRecap("Katakana") }
        binding.buttonN5.setOnClickListener { navigateToRecap("N5") }
        binding.buttonN4.setOnClickListener { navigateToRecap("N4") }
        binding.buttonN3.setOnClickListener { navigateToRecap("N3") }
        binding.buttonN2.setOnClickListener { navigateToRecap("N2") }
        binding.buttonN1.setOnClickListener { navigateToRecap("N1") }
        binding.buttonClass1.setOnClickListener { navigateToRecap("Classe 1") }
        binding.buttonClass2.setOnClickListener { navigateToRecap("Classe 2") }
        binding.buttonClass3.setOnClickListener { navigateToRecap("Classe 3") }
        binding.buttonClass4.setOnClickListener { navigateToRecap("Classe 4") }
        binding.buttonClass5.setOnClickListener { navigateToRecap("Classe 5") }
        binding.buttonClass6.setOnClickListener { navigateToRecap("Classe 6") }
        binding.buttonTest4.setOnClickListener { navigateToRecap("Test 4") }
        binding.buttonTest3.setOnClickListener { navigateToRecap("Test 3") }
        binding.buttonTestPre2.setOnClickListener { navigateToRecap("Test Pre-2") }
        binding.buttonTest2.setOnClickListener { navigateToRecap("Test 2") }
    }

    private fun navigateToRecap(level: String) {
        val bundle = Bundle().apply { putString("level", level) }
        findNavController().navigate(R.id.action_nav_recognition_to_game_recap, bundle)
    }

    private fun updateButtonText() {
        binding.buttonHiragana.text = "Hiragana\n$hiraganaPercentage%"
        binding.buttonKatakana.text = "Katakana\n$katakanaPercentage%"
        binding.buttonN5.text = "N5\n$n5Percentage%"
        binding.buttonN4.text = "N4\n$n4Percentage%"
        binding.buttonN3.text = "N3\n$n3Percentage%"
        binding.buttonN2.text = "N2\n$n2Percentage%"
        binding.buttonN1.text = "N1\n$n1Percentage%"
        binding.buttonClass1.text = "Classe 1\n$class1Percentage%"
        binding.buttonClass2.text = "Classe 2\n$class2Percentage%"
        binding.buttonClass3.text = "Classe 3\n$class3Percentage%"
        binding.buttonClass4.text = "Classe 4\n$class4Percentage%"
        binding.buttonClass5.text = "Classe 5\n$class5Percentage%"
        binding.buttonClass6.text = "Classe 6\n$class6Percentage%"
        binding.buttonTest4.text = "Test 4\n$test4Percentage%"
        binding.buttonTest3.text = "Test 3\n$test3Percentage%"
        binding.buttonTestPre2.text = "Test Pre-2\n$testPre2Percentage%"
        binding.buttonTest2.text = "Test 2\n$test2Percentage%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}