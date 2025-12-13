package org.oktail.kanjimori.ui.recognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentRecognitionBinding
import org.oktail.kanjimori.ui.gamerecap.KanjiScore
import org.xmlpull.v1.XmlPullParser

class RecognitionFragment : Fragment() {

    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateAllButtonPercentages()
        setupClickListeners()
    }

    private fun updateAllButtonPercentages() {
        val levels = listOf("N5", "N4", "N3", "N2", "N1", "Classe 1", "Classe 2", "Classe 3", "Classe 4", "Classe 5", "Classe 6", "Test 4", "Test 3", "Test Pre-2", "Test 2", "Hiragana", "Katakana")
        val allKanji = loadAllKanji()

        for (level in levels) {
            val kanjiForLevel = getKanjiForLevel(level, allKanji)
            if (kanjiForLevel.isNotEmpty()) {
                val masteryPercentage = calculateMasteryPercentage(kanjiForLevel)
                updateButtonText(level, masteryPercentage)
            }
        }
    }
    
    private fun loadAllKanji(): Map<String, String> {
        val allKanji = mutableMapOf<String, String>()
        val parser = resources.getXml(R.xml.kanji_levels)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "kanji") {
                    val id = parser.getAttributeValue(null, "id")
                    val character = parser.nextText()
                    if (id != null) {
                        allKanji[id] = character
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return allKanji
    }
    
    private fun getKanjiForLevel(levelName: String, allKanji: Map<String, String>): List<String> {
        val levelKanjiIds = mutableListOf<String>()
        val parser = resources.getXml(R.xml.kanji_levels)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "level" && parser.getAttributeValue(null, "name") == levelName) {
                    parseKanjiIdsForLevel(parser, levelKanjiIds)
                    break // Exit after finding the level
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return levelKanjiIds.mapNotNull { allKanji[it] }
    }

    private fun parseKanjiIdsForLevel(parser: XmlPullParser, levelKanjiIds: MutableList<String>) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_TAG || parser.name != "level") {
            if (eventType == XmlPullParser.START_TAG && parser.name == "kanji_id") {
                levelKanjiIds.add(parser.nextText())
            }
            eventType = parser.next()
        }
    }

    private fun calculateMasteryPercentage(kanjiList: List<String>): Double {
        if (kanjiList.isEmpty()) return 0.0
        val masteredCount = kanjiList.count { 
            val score = ScoreManager.getScore(requireContext(), it)
            (score.successes - score.failures) >= 10
        }
        return (masteredCount.toDouble() / kanjiList.size) * 100
    }

    private fun updateButtonText(level: String, percentage: Double) {
        val formattedPercentage = String.format("%.1f%%", percentage)
        val button = when(level) {
            "N5" -> binding.buttonN5
            "N4" -> binding.buttonN4
            "N3" -> binding.buttonN3
            "N2" -> binding.buttonN2
            "N1" -> binding.buttonN1
            "Classe 1" -> binding.buttonClass1
            "Classe 2" -> binding.buttonClass2
            "Classe 3" -> binding.buttonClass3
            "Classe 4" -> binding.buttonClass4
            "Classe 5" -> binding.buttonClass5
            "Classe 6" -> binding.buttonClass6
            "Test 4" -> binding.buttonTest4
            "Test 3" -> binding.buttonTest3
            "Test Pre-2" -> binding.buttonTestPre2
            "Test 2" -> binding.buttonTest2
            "Hiragana" -> binding.buttonHiragana
            "Katakana" -> binding.buttonKatakana
            else -> null
        }
        button?.text = "$level\n$formattedPercentage"
    }

    private fun setupClickListeners() {
        val action = R.id.action_nav_recognition_to_game_recap
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}