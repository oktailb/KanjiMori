package org.oktail.kanjimori.ui.wordquiz

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentWordQuizBinding
import org.xmlpull.v1.XmlPullParser

data class Word(val text: String, val phonetics: String)
enum class GameStatus { NOT_ANSWERED, CORRECT, INCORRECT }

class WordQuizFragment : Fragment() {

    private var _binding: FragmentWordQuizBinding? = null
    private val binding get() = _binding!!
    private val args: WordQuizFragmentArgs by navArgs()

    private val allWords = mutableListOf<Word>()
    private var currentWordSet = mutableListOf<Word>()
    private var revisionList = mutableListOf<Word>()
    private val wordStatus = mutableMapOf<Word, GameStatus>()
    private var wordListPosition = 0
    private lateinit var currentWord: Word
    private lateinit var correctAnswer: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wordListName = args.wordList
        val ignoreKnown = args.ignoreKnownWords
        loadWords(wordListName, ignoreKnown)

        if (allWords.isNotEmpty()) {
            startNewSet()
        } else {
            findNavController().popBackStack()
        }

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerClicked(button) }
        }
    }

    private fun startNewSet() {
        revisionList.clear()
        wordStatus.clear()

        if (wordListPosition >= allWords.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = allWords.drop(wordListPosition).take(10)
        wordListPosition += nextSet.size

        currentWordSet.clear()
        currentWordSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentWordSet.forEach { wordStatus[it] = GameStatus.NOT_ANSWERED }

        updateProgressBar()
        displayQuestion()
    }

    private fun loadWords(listName: String, ignoreKnown: Boolean) {
        // Special case for user custom list
        if (listName == "user_custom_list") {
             val scores = ScoreManager.getAllScores(requireContext(), ScoreManager.ScoreType.READING)
             val allKnownWords = mutableMapOf<String, String>()
             val listsToScan = listOf(
                 "bccwj_wordlist_1000", "bccwj_wordlist_2000", "bccwj_wordlist_3000", 
                 "bccwj_wordlist_4000", "bccwj_wordlist_5000", "bccwj_wordlist_6000", 
                 "bccwj_wordlist_7000", "bccwj_wordlist_8000"
             )
             
             for (list in listsToScan) {
                 val resourceId = resources.getIdentifier(list, "xml", requireContext().packageName)
                 if (resourceId != 0) {
                     val parser = resources.getXml(resourceId)
                     try {
                         var eventType = parser.eventType
                         while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
                                val phonetics = parser.getAttributeValue(null, "phonetics") ?: ""
                                val text = parser.nextText()
                                if (phonetics.isNotEmpty()) {
                                    allKnownWords[text] = phonetics
                                }
                            }
                            eventType = parser.next()
                         }
                     } catch (e: Exception) {
                         // Ignore
                     }
                 }
             }
             
             scores.forEach { (wordText, score) ->
                 if ((score.successes - score.failures) < 10) {
                     val phonetics = allKnownWords[wordText]
                     if (phonetics != null) {
                        allWords.add(Word(wordText, phonetics))
                     }
                 }
             }
             allWords.shuffle()
             return
        }

        val resourceId = resources.getIdentifier(listName, "xml", requireContext().packageName)
        if (resourceId == 0) return

        val parser = resources.getXml(resourceId)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
                    val phonetics = parser.getAttributeValue(null, "phonetics") ?: ""
                    val text = parser.nextText()
                    if (phonetics.isNotEmpty()) {
                        val score = ScoreManager.getScore(requireContext(), text, ScoreManager.ScoreType.READING)
                        val isKnown = (score.successes - score.failures) >= 10
                        if (!ignoreKnown || !isKnown) {
                            allWords.add(Word(text, phonetics))
                        }
                    }
                }
                eventType = parser.next()
            }
            allWords.shuffle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            startNewSet()
            return
        }

        currentWord = revisionList.random()
        binding.textWordToGuess.text = currentWord.text
        correctAnswer = currentWord.phonetics

        val answers = generateAnswers()
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(Color.LTGRAY)
            button.isEnabled = true
        }
    }

    private fun generateAnswers(): List<String> {
        val incorrectAnswers = allWords.asSequence()
            .map { it.phonetics }
            .filter { it != correctAnswer }
            .distinct()
            .shuffled()
            .take(3)
            .toList()

        return (incorrectAnswers + correctAnswer).shuffled()
    }

    private fun onAnswerClicked(button: Button) {
        val isCorrect = button.text == correctAnswer

        ScoreManager.saveScore(requireContext(), currentWord.text, isCorrect, ScoreManager.ScoreType.READING)

        if (isCorrect) {
            button.setBackgroundColor(Color.GREEN)
            wordStatus[currentWord] = GameStatus.CORRECT
            revisionList.remove(currentWord)
        } else {
            button.setBackgroundColor(Color.RED)
            wordStatus[currentWord] = GameStatus.INCORRECT
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
            if (i < currentWordSet.size) {
                val word = currentWordSet[i]
                val status = wordStatus[word]
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
