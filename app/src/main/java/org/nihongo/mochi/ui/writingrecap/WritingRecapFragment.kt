package org.nihongo.mochi.ui.writingrecap

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentWritingRecapBinding
import org.nihongo.mochi.ui.ScoreUiUtils
import org.xmlpull.v1.XmlPullParser

class WritingRecapFragment : Fragment() {

    private var _binding: FragmentWritingRecapBinding? = null
    private val binding get() = _binding!!
    private val args: WritingRecapFragmentArgs by navArgs()

    private var kanjiList: List<String> = emptyList()
    // Map character -> ID for navigation
    private var kanjiIdMap: MutableMap<String, String> = mutableMapOf()
    private var currentPage = 0
    private val pageSize = 80 // 8 columns * 10 rows

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingRecapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val level = args.level
        binding.textLevelTitle.text = level

        kanjiList = loadKanjiForLevel(level)

        binding.buttonPlay.setOnClickListener {
            if (level == "user_custom_list") {
                val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(null, kanjiList.toTypedArray())
                findNavController().navigate(action)
            } else {
                val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(level, null)
                findNavController().navigate(action)
            }
        }

        binding.buttonNextPage.setOnClickListener {
            if ((currentPage + 1) * pageSize < kanjiList.size) {
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
        kanjiList = loadKanjiForLevel(args.level)
        updateUi()
    }

    private fun updateUi() {
        if (kanjiList.isEmpty()) {
            binding.gridKanji.removeAllViews()
            binding.textPagination.text = "0..0 / 0"
            binding.buttonPlay.isEnabled = false
            return
        }

        binding.buttonPlay.isEnabled = true

        val kanjiScores = kanjiList.map { ScoreManager.getScore(it, ScoreManager.ScoreType.WRITING) }

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(kanjiList.size)

        binding.gridKanji.removeAllViews()
        for (i in startIndex until endIndex) {
            val kanjiCharacter = kanjiList[i]
            val score = kanjiScores[i]

            val textView = TextView(context).apply {
                text = kanjiCharacter
                textSize = 24f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                
                // Use default text color from theme (usually handled by attribute, but here explicitly using attribute is complex programmatically)
                // A safe bet is using standard color resource or obtaining from theme.
                // Assuming R.color.primary_text or similar exists or we can resolve ?android:attr/textColorPrimary
                val typedValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                setTextColor(ContextCompat.getColor(context, typedValue.resourceId))

                // Using ScoreManager color logic but ensuring it matches theme somewhat?
                // Actually score colors are semantic (Red/Green/Orange/Gray).
                // Let's assume ScoreManager returns appropriate colors, or use resources if needed.
                // For background, we use the score color.
                setBackgroundColor(ScoreUiUtils.getScoreColor(requireContext(), score))
                
                val params = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params
                
                // Add click listener
                setOnClickListener {
                    val id = kanjiIdMap[kanjiCharacter]
                    if (id != null) {
                        val action = WritingRecapFragmentDirections.actionWritingRecapToKanjiDetail(id)
                        findNavController().navigate(action)
                    }
                }
            }
            binding.gridKanji.addView(textView)
        }

        binding.textPagination.text = "${startIndex + 1}..${endIndex} / ${kanjiList.size}"
        binding.buttonPrevPage.isEnabled = currentPage > 0
        binding.buttonPrevPage.alpha = if (currentPage > 0) 1.0f else 0.5f
        binding.buttonNextPage.isEnabled = endIndex < kanjiList.size
        binding.buttonNextPage.alpha = if (endIndex < kanjiList.size) 1.0f else 0.5f
    }

    private fun loadKanjiForLevel(levelName: String): List<String> {
        if (levelName == "user_custom_list") {
            return loadUserListKanji()
        }

        val allKanji = mutableMapOf<String, String>()
        val levelKanjiIds = mutableListOf<String>()
        val parser = resources.getXml(R.xml.kanji_levels)
        kanjiIdMap.clear()

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.name == "kanji") {
                        val id = parser.getAttributeValue(null, "id")
                        val character = parser.nextText()
                        if (id != null) {
                            allKanji[id] = character
                            kanjiIdMap[character] = id
                        }
                    } else if (parser.name == "level" && parser.getAttributeValue(null, "name") == levelName) {
                        parseKanjiIdsForLevel(parser, levelKanjiIds)
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return levelKanjiIds.mapNotNull { allKanji[it] }
    }

    private fun loadUserListKanji(): List<String> {
        populateFullKanjiMap()
        val scores = ScoreManager.getAllScores(ScoreManager.ScoreType.WRITING)
        return scores.filter { (_, score) -> (score.successes - score.failures) < 10 }.keys.toList()
    }
    
    private fun populateFullKanjiMap() {
        if (kanjiIdMap.isNotEmpty()) return 
        
        val parser = resources.getXml(R.xml.kanji_levels)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "kanji") {
                    val id = parser.getAttributeValue(null, "id")
                    val character = parser.nextText()
                    if (id != null) {
                        kanjiIdMap[character] = id
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) { e.printStackTrace() }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}