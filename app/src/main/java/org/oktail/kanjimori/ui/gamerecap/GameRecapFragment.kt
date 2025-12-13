package org.oktail.kanjimori.ui.gamerecap

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentGameRecapBinding
import org.xmlpull.v1.XmlPullParser
import kotlin.random.Random

data class KanjiScore(val successes: Int, val failures: Int)

class GameRecapFragment : Fragment() {

    private var _binding: FragmentGameRecapBinding? = null
    private val binding get() = _binding!!
    private val args: GameRecapFragmentArgs by navArgs()

    // Simulated scores - replace with real data persistence later
    private val kanjiScores = mutableMapOf<String, KanjiScore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameRecapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val level = args.level
        binding.textLevelTitle.text = level

        val kanjiList = loadKanjiForLevel(level)
        simulateScores(kanjiList) // Simulate scores for now
        populateGrid(kanjiList)

        binding.buttonPlay.setOnClickListener {
            val bundle = Bundle().apply { putString("gameMode", "meaning") } // or "reading"
            findNavController().navigate(R.id.action_game_recap_to_recognition_game, bundle)
        }
    }

    private fun loadKanjiForLevel(level: String): List<String> {
        val kanjiList = mutableListOf<String>()
        val parser = resources.getXml(R.xml.kanji_levels)
        var inTargetLevel = false
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "level") {
                            if (parser.getAttributeValue(null, "name") == level) {
                                inTargetLevel = true
                            }
                        } else if (parser.name == "kanji" && inTargetLevel) {
                            kanjiList.add(parser.nextText())
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "level" && inTargetLevel) {
                            // We've reached the end of our target level, so we can stop.
                            return kanjiList
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return kanjiList
    }

    private fun simulateScores(kanjiList: List<String>) {
        for (kanji in kanjiList) {
            val successes = Random.nextInt(11)
            val failures = Random.nextInt(11)
            kanjiScores[kanji] = KanjiScore(successes, failures)
        }
    }

    private fun populateGrid(kanjiList: List<String>) {
        binding.gridKanji.removeAllViews()
        for (kanji in kanjiList) {
            val score = kanjiScores[kanji] ?: KanjiScore(0, 0)
            val color = calculateColor(score)

            val textView = TextView(context).apply {
                text = kanji
                textSize = 24f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setBackgroundColor(color)
                val params = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                    rowSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params
            }
            binding.gridKanji.addView(textView)
        }
    }

    private fun calculateColor(score: KanjiScore): Int {
        val balance = score.successes - score.failures
        val percentage = (balance.toFloat() / 10.0f).coerceIn(-1.0f, 1.0f)

        return when {
            percentage > 0 -> lerpColor(Color.WHITE, Color.GREEN, percentage)
            percentage < 0 -> lerpColor(Color.WHITE, Color.RED, -percentage)
            else -> Color.WHITE
        }
    }

    private fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)

        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)

        val a = (startA + fraction * (endA - startA)).toInt()
        val r = (startR + fraction * (endR - startR)).toInt()
        val g = (startG + fraction * (endG - startG)).toInt()
        val b = (startB + fraction * (endB - startB)).toInt()

        return Color.argb(a, r, g, b)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}