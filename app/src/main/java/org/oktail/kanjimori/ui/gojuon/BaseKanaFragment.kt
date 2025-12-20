package org.oktail.kanjimori.ui.gojuon

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Space
import android.widget.TextView
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.KanjiScore
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentKanaBinding
import org.xmlpull.v1.XmlPullParser

abstract class BaseKanaFragment : Fragment() {

    private var _binding: FragmentKanaBinding? = null
    protected val binding get() = _binding!!

    abstract val kanaType: KanaType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textLevelTitle.text = getString(kanaType.titleRes)
        populateGrid()

        binding.buttonPlayQuiz.setOnClickListener {
            findNavController().navigate(kanaType.navigationActionId)
        }
    }

    private fun populateGrid() {
        val characters = loadKana(kanaType.xmlResId)
        val charactersByLine = characters.groupBy { it.line }.toSortedMap()

        binding.gridKana.removeAllViews()

        for ((_, charsInLine) in charactersByLine) {
            for (kana in charsInLine) {
                val score = ScoreManager.getScore(requireContext(), kana.value, ScoreManager.ScoreType.RECOGNITION)
                val textView = TextView(context).apply {
                    text = kana.value
                    textSize = 24f
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setTextColor(Color.BLACK)
                    setBackgroundColor(calculateColor(score))
                    val params = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(4, 4, 4, 4)
                    }
                    layoutParams = params
                }
                binding.gridKana.addView(textView)
            }

            val spacersNeeded = 5 - charsInLine.size
            if (spacersNeeded > 0) {
                for (i in 1..spacersNeeded) {
                    val spacer = Space(context).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 0
                            height = GridLayout.LayoutParams.WRAP_CONTENT
                            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        }
                    }
                    binding.gridKana.addView(spacer)
                }
            }
        }
    }

    private fun loadKana(@XmlRes xmlResId: Int): List<KanaCharacter> {
        val kanaList = mutableListOf<KanaCharacter>()
        val parser = resources.getXml(xmlResId)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "character") {
                    val line = parser.getAttributeValue(null, "line").toInt()
                    val phonetics = parser.getAttributeValue(null, "phonetics")
                    val value = parser.nextText()
                    kanaList.add(KanaCharacter(value, line, phonetics))
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return kanaList
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

enum class KanaType(@XmlRes val xmlResId: Int, val titleRes: Int, val navigationActionId: Int) {
    HIRAGANA(R.xml.hiragana, R.string.level_hiragana, R.id.action_nav_hiragana_to_hiragana_quiz),
    KATAKANA(R.xml.katakana, R.string.level_katakana, R.id.action_nav_katakana_to_katakana_quiz)
}

data class KanaCharacter(val value: String, val line: Int, val phonetics: String)
