package org.nihongo.mochi.ui.wordquiz

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentWordQuizBinding
import org.nihongo.mochi.ui.game.GameStatus
import org.nihongo.mochi.ui.game.Word
import org.xmlpull.v1.XmlPullParser

class WordQuizFragment : Fragment() {

    private var _binding: FragmentWordQuizBinding? = null
    private val binding get() = _binding!!
    private val args: WordQuizFragmentArgs by navArgs()
    private val viewModel: WordQuizViewModel by viewModels()

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

        if (!viewModel.isGameInitialized) {
            val customWordList = args.customWordList
            loadWords(customWordList)
            
            if (viewModel.allWords.isNotEmpty()) {
                startNewSet()
                viewModel.isGameInitialized = true
            } else {
                findNavController().popBackStack()
            }
        } else {
             restoreUI()
        }

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerClicked(button) }
        }
    }
    
    private fun restoreUI() {
        if (viewModel.allWords.isEmpty()) return
        
        binding.textWordToGuess.text = viewModel.currentWord.text
        updateProgressBar()
        
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEachIndexed { index, button ->
            if (viewModel.currentAnswers.size > index) {
                button.text = viewModel.currentAnswers[index]
            }
            if (viewModel.buttonColors.size > index) {
                 val colorRes = viewModel.buttonColors[index]
                 if (colorRes != 0) {
                     button.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
                 } else {
                     button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
                 }
            }
            button.isEnabled = viewModel.areButtonsEnabled
        }
    }

    private fun startNewSet() {
        viewModel.revisionList.clear()
        viewModel.wordStatus.clear()

        if (viewModel.wordListPosition >= viewModel.allWords.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = viewModel.allWords.drop(viewModel.wordListPosition).take(10)
        viewModel.wordListPosition += nextSet.size

        viewModel.currentWordSet.clear()
        viewModel.currentWordSet.addAll(nextSet)
        viewModel.revisionList.addAll(nextSet)
        viewModel.currentWordSet.forEach { viewModel.wordStatus[it] = GameStatus.NOT_ANSWERED }

        updateProgressBar()
        displayQuestion()
    }

    private fun loadWords(customWordList: Array<String>?) {
        if (customWordList == null) return
        
        viewModel.allWords.clear()

        // Build a temporary map of all known words to their phonetics
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

        // Now, build the allWords list from the custom list, looking up phonetics
        for (wordText in customWordList) {
            val phonetics = allKnownWords[wordText]
            if (phonetics != null) {
                viewModel.allWords.add(Word(wordText, phonetics))
            }
        }
        viewModel.allWords.shuffle()
    }

    private fun displayQuestion() {
        if (viewModel.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        viewModel.currentWord = viewModel.revisionList.random()
        binding.textWordToGuess.text = viewModel.currentWord.text
        viewModel.correctAnswer = viewModel.currentWord.phonetics

        val answers = generateAnswers()
        viewModel.currentAnswers = answers
        
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        
        viewModel.buttonColors.clear()
        repeat(4) { viewModel.buttonColors.add(R.color.button_background) }

        answerButtons.zip(answers).forEachIndexed { index, (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
            viewModel.buttonColors[index] = R.color.button_background
            button.isEnabled = true
        }
        viewModel.areButtonsEnabled = true
    }

    private fun generateAnswers(): List<String> {
        val incorrectAnswers = viewModel.allWords.asSequence()
            .map { it.phonetics }
            .filter { it != viewModel.correctAnswer }
            .distinct()
            .shuffled()
            .take(3)
            .toList()

        return (incorrectAnswers + viewModel.correctAnswer).shuffled()
    }

    private fun onAnswerClicked(button: Button) {
        val isCorrect = button.text == viewModel.correctAnswer

        ScoreManager.saveScore(requireContext(), viewModel.currentWord.text, isCorrect, ScoreManager.ScoreType.READING)
        
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val buttonIndex = answerButtons.indexOf(button)

        if (isCorrect) {
            viewModel.buttonColors[buttonIndex] = R.color.answer_correct
            viewModel.wordStatus[viewModel.currentWord] = GameStatus.CORRECT
            viewModel.revisionList.remove(viewModel.currentWord)
        } else {
            viewModel.buttonColors[buttonIndex] = R.color.answer_incorrect
            viewModel.wordStatus[viewModel.currentWord] = GameStatus.INCORRECT
            
            // Show correct answer
            val correctIndex = answerButtons.indexOfFirst { it.text == viewModel.correctAnswer }
            if (correctIndex != -1) {
                 viewModel.buttonColors[correctIndex] = R.color.answer_correct
                 answerButtons[correctIndex].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_correct))
            }
        }
        
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), viewModel.buttonColors[buttonIndex]))

        updateProgressBar()

        answerButtons.forEach { it.isEnabled = false }
        viewModel.areButtonsEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            displayQuestion()
        }, 1000)
    }

    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < viewModel.currentWordSet.size) {
                val word = viewModel.currentWordSet[i]
                val status = viewModel.wordStatus[word]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                
                indicator.clearColorFilter()
                
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