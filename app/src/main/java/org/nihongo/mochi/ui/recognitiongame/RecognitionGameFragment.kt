package org.nihongo.mochi.ui.recognitiongame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentRecognitionGameBinding
import org.nihongo.mochi.domain.game.GameState
import org.nihongo.mochi.domain.game.QuestionDirection
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.settings.PRONUNCIATION_PREF_KEY

class RecognitionGameFragment : Fragment() {

    private var _binding: FragmentRecognitionGameBinding? = null
    private val binding get() = _binding!!
    private val args: RecognitionGameFragmentArgs by navArgs()
    private val viewModel: RecognitionGameViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionGameBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pass pronunciation mode to ViewModel
        val pronunciationMode = sharedPreferences.getString(PRONUNCIATION_PREF_KEY, "Hiragana") ?: "Hiragana"
        viewModel.updatePronunciationMode(pronunciationMode)
        
        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        viewModel.setAnimationSpeed(animationSpeed)

        if (!viewModel.isGameInitialized) {
            initializeGame()
            viewModel.isGameInitialized = true
        }

        setupUI()
        setupStateObservation()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEachIndexed { index, button ->
            button.setOnClickListener { onAnswerClicked(button, index) }
        }
    }

    private fun setupStateObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: GameState) {
        when(state) {
            GameState.Loading -> { 
                // Could show loading indicator
            }
            GameState.WaitingForAnswer -> {
                displayQuestion()
            }
            is GameState.ShowingResult -> {
                showResult(state.isCorrect, state.selectedAnswerIndex)
            }
            GameState.Finished -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun initializeGame() {
        viewModel.gameMode = args.gameMode
        viewModel.readingMode = args.readingMode
        val level = args.level
        val customWordList = args.customWordList?.toList() ?: emptyList()

        loadAllKanjiDetails()

        val kanjiCharsForLevel: List<String> = if (customWordList.isNotEmpty()) {
            customWordList
        } else {
            MochiApplication.levelContentProvider.getCharactersForLevel(level)
        }

        viewModel.allKanjiDetails.clear()
        viewModel.allKanjiDetails.addAll(
            viewModel.allKanjiDetailsXml.filter {
                kanjiCharsForLevel.contains(it.character) && it.meanings.isNotEmpty()
            }
        )
        viewModel.allKanjiDetails.shuffle()
        viewModel.kanjiListPosition = 0

        if (viewModel.allKanjiDetails.isNotEmpty()) {
            viewModel.startGame()
        } else {
            Log.e("RecognitionGameFragment", "No kanji loaded for level: $level.")
            findNavController().popBackStack()
        }
    }

    private fun setupUI() {
        if (viewModel.allKanjiDetails.isEmpty()) return
        // UI setup is mostly handled by state observation now
    }
    
    private fun updateButtonSize(button: Button, text: String) {
        if (viewModel.currentDirection == QuestionDirection.REVERSE) {
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
        } else {
            val length = text.length
            val newSize = when {
                length > 40 -> 10f
                length > 20 -> 12f
                else -> 14f
            }
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
        }
    }

    private fun loadAllKanjiDetails() {
        val locale = MochiApplication.settingsRepository.getAppLocale()
        val meanings = MochiApplication.meaningRepository.getMeanings(locale)
        val allKanjiEntries = MochiApplication.kanjiRepository.getAllKanji()
        
        viewModel.allKanjiDetailsXml.clear()
        
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            val kanjiDetail = KanjiDetail(id, character, kanjiMeanings, readingsList)
            viewModel.allKanjiDetailsXml.add(kanjiDetail)
        }
    }

    private fun displayQuestion() {
        displayQuestionText()
        updateProgressBar()

        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        val answers = viewModel.currentAnswers

        viewModel.buttonColors.clear()
        repeat(4) { viewModel.buttonColors.add(R.color.button_background) }

        answerButtons.zip(answers).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
            button.isEnabled = true
            updateButtonSize(button, answerText)
        }
        viewModel.areButtonsEnabled = true
    }

    private fun displayQuestionText() {
        if (viewModel.currentDirection == QuestionDirection.NORMAL) {
            binding.textKanjiToGuess.text = viewModel.currentKanji.character
            binding.textKanjiToGuess.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120f)
        } else {
            val questionText = if (viewModel.gameMode == "meaning") {
                viewModel.currentKanji.meanings.joinToString("\n")
            } else {
                viewModel.getFormattedReadings(viewModel.currentKanji)
            }
            binding.textKanjiToGuess.text = questionText

            val length = questionText.length
            val lineCount = questionText.count { it == '\n' } + 1

            val newSize = when {
                lineCount > 7 || length > 100 -> 14f
                lineCount > 5 || length > 70 -> 18f
                lineCount > 3 || length > 40 -> 24f
                lineCount > 1 || length > 15 -> 32f
                else -> 48f
            }
            binding.textKanjiToGuess.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
        }
    }

    private fun onAnswerClicked(button: Button, index: Int) {
        val selectedAnswer = button.text.toString()
        viewModel.submitAnswer(selectedAnswer, index)
        // Disable buttons immediately to prevent double clicks
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        answerButtons.forEach { it.isEnabled = false }
    }
    
    private fun showResult(isCorrect: Boolean, selectedIndex: Int) {
        val answerButtons = listOf(binding.buttonAnswer1, binding.buttonAnswer2, binding.buttonAnswer3, binding.buttonAnswer4)
        
        if (isCorrect) {
             viewModel.buttonColors[selectedIndex] = R.color.answer_correct
             // Check partial
             val status = viewModel.kanjiStatus[viewModel.currentKanji]
             if (status == GameStatus.PARTIAL) {
                 viewModel.buttonColors[selectedIndex] = R.color.answer_neutral
             }
        } else {
             viewModel.buttonColors[selectedIndex] = R.color.answer_incorrect
        }
        
        answerButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(requireContext(), viewModel.buttonColors[selectedIndex]))
        updateProgressBar()
        
        answerButtons.forEach { it.isEnabled = false }
    }

    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < viewModel.currentKanjiSet.size) {
                val kanji = viewModel.currentKanjiSet[i]
                val status = viewModel.kanjiStatus[kanji]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE

                indicator.clearColorFilter()

                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    GameStatus.PARTIAL -> {
                        indicator.setImageResource(android.R.drawable.ic_menu_recent_history)
                        indicator.setColorFilter(ContextCompat.getColor(requireContext(), R.color.answer_neutral))
                    }
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
