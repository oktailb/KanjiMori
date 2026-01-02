package org.nihongo.mochi.ui.wordlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.domain.words.WordListEngine
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.ui.theme.AppTheme

class WordListViewModel(
    private val wordRepository: WordRepository,
    private val meaningRepository: MeaningRepository,
    private val kanjiRepository: KanjiRepository,
    private val settingsRepository: SettingsRepository,
    private val levelsRepository: LevelsRepository,
    private val baseColorInt: Int
) : ViewModel() {

    private val engine = WordListEngine(wordRepository)

    private val _displayedWords = MutableStateFlow<List<Triple<WordEntry, Color, Boolean>>>(emptyList())
    val displayedWords: StateFlow<List<Triple<WordEntry, Color, Boolean>>> = _displayedWords.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    // Filters state
    private val _filterKanjiOnly = MutableStateFlow(false)
    val filterKanjiOnly = _filterKanjiOnly.asStateFlow()
    
    private val _filterSimpleWords = MutableStateFlow(false)
    val filterSimpleWords = _filterSimpleWords.asStateFlow()
    
    private val _filterCompoundWords = MutableStateFlow(false)
    val filterCompoundWords = _filterCompoundWords.asStateFlow()
    
    private val _filterIgnoreKnown = MutableStateFlow(false)
    val filterIgnoreKnown = _filterIgnoreKnown.asStateFlow()

    private val _selectedWordType = MutableStateFlow("Tous" to "All") // Internal Key, Display Value
    val selectedWordType = _selectedWordType.asStateFlow()

    private val _screenTitleKey = MutableStateFlow<String?>(null)
    val screenTitleKey = _screenTitleKey.asStateFlow()

    private val pageSize = 80
    private var allKanjiDetails = listOf<KanjiDetail>()

    fun loadList(listName: String) {
        viewModelScope.launch {
            if (listName == "user_custom_list") {
                _screenTitleKey.value = "reading_user_list"
            } else {
                val defs = levelsRepository.loadLevelDefinitions()
                // Find level that has this dataFile in any activity
                var foundKey: String? = null
                
                outer@ for (section in defs.sections.values) {
                    for (level in section.levels) {
                        for (activity in level.activities.values) {
                            if (activity.dataFile == listName) {
                                foundKey = level.name // This is the resource key, e.g. "level_n5"
                                break@outer
                            }
                        }
                    }
                }
                
                _screenTitleKey.value = foundKey
            }

            loadAllKanjiDetails()
            engine.loadList(listName)
            applyFilters()
        }
    }
    
    fun setFilterKanjiOnly(enabled: Boolean) {
        _filterKanjiOnly.value = enabled
        engine.filterKanjiOnly = enabled
        applyFilters()
    }
    
    fun setFilterSimpleWords(enabled: Boolean) {
        _filterSimpleWords.value = enabled
        engine.filterSimpleWords = enabled
        applyFilters()
    }
    
    fun setFilterCompoundWords(enabled: Boolean) {
        _filterCompoundWords.value = enabled
        engine.filterCompoundWords = enabled
        applyFilters()
    }
    
    fun setFilterIgnoreKnown(enabled: Boolean) {
        _filterIgnoreKnown.value = enabled
        engine.filterIgnoreKnown = enabled
        applyFilters()
    }
    
    fun setWordType(type: Pair<String, String>) {
        _selectedWordType.value = type
        engine.filterWordType = type.first
        applyFilters()
    }

    private fun applyFilters() {
        engine.applyFilters()
        _currentPage.value = 0
        updateCurrentPageItems()
    }

    fun nextPage() {
        if (_currentPage.value < _totalPages.value - 1) {
            _currentPage.value++
            updateCurrentPageItems()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updateCurrentPageItems()
        }
    }
    
    fun getGameWordList(): Array<String> {
         return engine.getDisplayedWords().map { it.text }.toTypedArray()
    }

    private fun updateCurrentPageItems() {
        val allDisplayed = engine.getDisplayedWords()
        _totalPages.value = if (allDisplayed.isEmpty()) 0 else (allDisplayed.size + pageSize - 1) / pageSize
        
        val startIndex = _currentPage.value * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(allDisplayed.size)
        
        if (startIndex < allDisplayed.size) {
            val pageItems = allDisplayed.subList(startIndex, endIndex)
            _displayedWords.value = pageItems.map { word ->
                val score = ScoreManager.getScore(word.text, ScoreManager.ScoreType.READING)
                val colorInt = ScorePresentationUtils.getScoreColor(score, baseColorInt)
                val kanjiDetail = allKanjiDetails.firstOrNull { it.character == word.text }
                // Red border logic: kanji found but no meanings (implies potential issue or specific state)
                val isRedBorder = kanjiDetail != null && kanjiDetail.meanings.isEmpty()
                Triple(word, Color(colorInt), isRedBorder)
            }
        } else {
             _displayedWords.value = emptyList()
        }
    }
    
    private fun loadAllKanjiDetails() {
        val locale = settingsRepository.getAppLocale()
        val meanings = meaningRepository.getMeanings(locale)
        val allKanjiEntries = kanjiRepository.getAllKanji()
        
        val details = mutableListOf<KanjiDetail>()
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            details.add(KanjiDetail(id, character, kanjiMeanings, readingsList))
        }
        allKanjiDetails = details
    }
}

class WordListFragment : Fragment() {

    private val args: WordListFragmentArgs by navArgs()
    private val wordRepository: WordRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val meaningRepository: MeaningRepository by inject()
    private val kanjiRepository: KanjiRepository by inject()
    private val levelsRepository: LevelsRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                AppTheme {
                    val baseColor = ContextCompat.getColor(context, R.color.recap_grid_base_color)
                    val viewModel: WordListViewModel = viewModel {
                        WordListViewModel(wordRepository, meaningRepository, kanjiRepository, settingsRepository, levelsRepository, baseColor)
                    }

                    // Initial load
                    androidx.compose.runtime.LaunchedEffect(args.wordList) {
                        viewModel.loadList(args.wordList)
                    }
                    
                     val lifecycleOwner = LocalLifecycleOwner.current
                     androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                // Reload to refresh scores if coming back from game
                                viewModel.loadList(args.wordList)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                             lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val displayedWords by viewModel.displayedWords.collectAsState()
                    val currentPage by viewModel.currentPage.collectAsState()
                    val totalPages by viewModel.totalPages.collectAsState()
                    
                    val filterKanjiOnly by viewModel.filterKanjiOnly.collectAsState()
                    val filterSimpleWords by viewModel.filterSimpleWords.collectAsState()
                    val filterCompoundWords by viewModel.filterCompoundWords.collectAsState()
                    val filterIgnoreKnown by viewModel.filterIgnoreKnown.collectAsState()
                    val selectedWordType by viewModel.selectedWordType.collectAsState()
                    val screenTitleKey by viewModel.screenTitleKey.collectAsState()
                    
                    val wordTypeOptions = listOf(
                        "Tous" to getString(R.string.word_type_all),
                        "和" to getString(R.string.word_type_wa),
                        "固" to getString(R.string.word_type_ko),
                        "外" to getString(R.string.word_type_gai),
                        "混" to getString(R.string.word_type_kon),
                        "漢" to getString(R.string.word_type_kan),
                        "記号" to getString(R.string.word_type_kigo)
                    )
                    
                    val listTitle = if (screenTitleKey != null) {
                        val resId = context.resources.getIdentifier(screenTitleKey, "string", context.packageName)
                        if (resId != 0) getString(resId) else args.wordList
                    } else {
                        args.wordList
                    }

                    WordListScreen(
                        listTitle = listTitle,
                        wordsWithColors = displayedWords,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        filterKanjiOnly = filterKanjiOnly,
                        filterSimpleWords = filterSimpleWords,
                        filterCompoundWords = filterCompoundWords,
                        filterIgnoreKnown = filterIgnoreKnown,
                        selectedWordType = selectedWordType,
                        wordTypeOptions = wordTypeOptions,
                        onFilterKanjiOnlyChange = { viewModel.setFilterKanjiOnly(it) },
                        onFilterSimpleWordsChange = { viewModel.setFilterSimpleWords(it) },
                        onFilterCompoundWordsChange = { viewModel.setFilterCompoundWords(it) },
                        onFilterIgnoreKnownChange = { viewModel.setFilterIgnoreKnown(it) },
                        onWordTypeChange = { viewModel.setWordType(it) },
                        onPrevPage = { viewModel.prevPage() },
                        onNextPage = { viewModel.nextPage() },
                        onPlayClick = {
                            val customWordList = viewModel.getGameWordList()
                            val action = WordListFragmentDirections.actionNavWordListToNavWordQuiz(customWordList)
                            findNavController().navigate(action)
                        }
                    )
                }
            }
        }
    }
}
