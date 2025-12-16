package org.oktail.kanjimori.ui.wordlist

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.KanjiScore
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentWordListBinding
import org.xmlpull.v1.XmlPullParser

class WordListFragment : Fragment() {

    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!
    private val args: WordListFragmentArgs by navArgs()

    private var wordList: List<String> = emptyList()
    private var currentPage = 0
    private val pageSize = 80 // 8 columns * 10 rows

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wordListName = args.wordList
        binding.textListTitle.text = wordListName.replace("bccwj_wordlist_", "Common Words ").replace(".xml", "")

        wordList = loadWordsForList(wordListName)

        binding.buttonNextPage.setOnClickListener {
            if ((currentPage + 1) * pageSize < wordList.size) {
                currentPage++
                updateUi()
            }
        }

        binding.buttonPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateUi()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    private fun updateUi() {
        if (wordList.isEmpty()) {
            binding.textStats.text = ""
            binding.gridWords.removeAllViews()
            binding.textPagination.text = "0..0 / 0"
            return
        }

        // --- Process all Words in one go ---
        val scoreBuckets = (0..10).associateWith { 0 }.toMutableMap()
        var newWords = 0
        val wordScores = wordList.map { ScoreManager.getScore(requireContext(), it) }

        for (score in wordScores) {
            if (score.successes == 0 && score.failures == 0) {
                newWords++
            } else {
                val balance = score.successes - score.failures
                val bucket = balance.coerceIn(0, 10)
                scoreBuckets[bucket] = (scoreBuckets[bucket] ?: 0) + 1
            }
        }

        // --- Update Stats UI ---
        val statsText = buildString {
            append("Nouveau: $newWords")
            scoreBuckets.toSortedMap().forEach { (score, count) ->
                if (count > 0) {
                    append(", $score:$count")
                }
            }
        }
        binding.textStats.text = statsText

        // --- Update Grid UI for the current page ---
        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(wordList.size)

        binding.gridWords.removeAllViews()
        for (i in startIndex until endIndex) {
            val word = wordList[i]
            val score = wordScores[i]

            val textView = TextView(context).apply {
                text = word
                textSize = 24f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setBackgroundColor(calculateColor(score))
                val params = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params
            }
            binding.gridWords.addView(textView)
        }

        // --- Update Pagination UI ---
        binding.textPagination.text = "${startIndex + 1}..${endIndex} / ${wordList.size}"
        binding.buttonPrevPage.isEnabled = currentPage > 0
        binding.buttonPrevPage.alpha = if (currentPage > 0) 1.0f else 0.5f
        binding.buttonNextPage.isEnabled = endIndex < wordList.size
        binding.buttonNextPage.alpha = if (endIndex < wordList.size) 1.0f else 0.5f
    }

    private fun loadWordsForList(listName: String): List<String> {
        val words = mutableListOf<String>()
        val resourceId = resources.getIdentifier(listName, "xml", requireContext().packageName)
        if (resourceId == 0) return emptyList()

        val parser = resources.getXml(resourceId)

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
                    words.add(parser.nextText())
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return words
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