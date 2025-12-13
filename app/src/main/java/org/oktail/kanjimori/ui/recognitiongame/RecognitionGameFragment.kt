package org.oktail.kanjimori.ui.recognitiongame

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentRecognitionGameBinding
import org.xmlpull.v1.XmlPullParser

data class KanjiDetail(val character: String, val meaning: String, val reading: String)
enum class GameStatus { NOT_ANSWERED, CORRECT, INCORRECT }

class RecognitionGameFragment : Fragment() {

    private var _binding: FragmentRecognitionGameBinding? = null
    private val binding get() = _binding!!
    private val args: RecognitionGameFragmentArgs by navArgs()

    private val allKanjiDetails = mutableListOf<KanjiDetail>()
    private var currentKanjiSet = mutableListOf<KanjiDetail>()
    private var revisionList = mutableListOf<KanjiDetail>()
    private val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    private lateinit var currentKanji: KanjiDetail
    private lateinit var gameMode: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        gameMode = args.gameMode

        loadAllKanjiDetails()
        if (allKanjiDetails.isNotEmpty()) {
            startNewSet()
        }

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerClicked(button) }
        }
    }

    private fun startNewSet() {
        val currentSize = currentKanjiSet.size
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()

        val nextSet = allKanjiDetails.drop(currentSize).take(10)
        currentKanjiSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKanjiSet.forEach { kanjiStatus[it] = GameStatus.NOT_ANSWERED }
        
        updateProgressBar()
        displayQuestion()
    }

    private fun loadAllKanjiDetails() {
        val parser = resources.getXml(R.xml.kanji_details)
        var character: String? = null
        var meaning: String? = null
        var reading: String? = null

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "kanji" -> character = parser.getAttributeValue(null, "character")
                            "meaning" -> meaning = parser.nextText()
                            "reading" -> reading = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "kanji" && character != null && meaning != null && reading != null) {
                            allKanjiDetails.add(KanjiDetail(character!!, meaning!!, reading!!))
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            startNewSet()
            return
        }

        currentKanji = revisionList.random()
        binding.textKanjiToGuess.text = currentKanji.character

        val answers = generateAnswers(currentKanji)
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)

        answerButtons.zip(answers).forEach { (button, answer) ->
            button.text = answer
            button.setBackgroundColor(Color.LTGRAY)
            button.isEnabled = true
        }
    }

    private fun generateAnswers(correctKanji: KanjiDetail): List<String> {
        val getAnswerProperty = { detail: KanjiDetail ->
            if (gameMode == "meaning") detail.meaning else detail.reading.split(",").first().trim()
        }

        val incorrectAnswers = allKanjiDetails
            .filter { it != correctKanji }
            .shuffled()
            .take(3)
            .map { getAnswerProperty(it) }
            .toMutableList()

        val allAnswers = incorrectAnswers.apply { add(getAnswerProperty(correctKanji)) }.shuffled()
        return allAnswers
    }

    private fun onAnswerClicked(button: Button) {
        val correctAnswer = if (gameMode == "meaning") currentKanji.meaning else currentKanji.reading.split(",").first().trim()
        val isCorrect = button.text == correctAnswer

        if (isCorrect) {
            button.setBackgroundColor(Color.GREEN)
            revisionList.remove(currentKanji)
            kanjiStatus[currentKanji] = GameStatus.CORRECT
        } else {
            button.setBackgroundColor(Color.RED)
            kanjiStatus[currentKanji] = GameStatus.INCORRECT
        }

        updateProgressBar()
        
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { it.isEnabled = false }

        Handler(Looper.getMainLooper()).postDelayed({
            displayQuestion()
        }, 1000)
    }
    
    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < currentKanjiSet.size) {
                val kanji = currentKanjiSet[i]
                val status = kanjiStatus[kanji]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    else -> indicator.setImageResource(android.R.drawable.checkbox_off_background)
                }
            } else {
                progressIndicators[i].visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}