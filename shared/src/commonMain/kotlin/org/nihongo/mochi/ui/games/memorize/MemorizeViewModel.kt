package org.nihongo.mochi.ui.games.memorize

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.presentation.ViewModel

class MemorizeViewModel(
    private val kanjiRepository: KanjiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- Setup State ---
    private val allPossibleGridSizes = listOf(
        MemorizeGridSize(4, 3), // 12 cards -> 6 pairs
        MemorizeGridSize(4, 4), // 16 cards -> 8 pairs
        MemorizeGridSize(5, 4), // 20 cards -> 10 pairs
        MemorizeGridSize(6, 4), // 24 cards -> 12 pairs
        MemorizeGridSize(6, 5)  // 30 cards -> 15 pairs
    )

    private val _availableGridSizes = MutableStateFlow<List<MemorizeGridSize>>(allPossibleGridSizes)
    val availableGridSizes: StateFlow<List<MemorizeGridSize>> = _availableGridSizes.asStateFlow()

    private val _selectedGridSize = MutableStateFlow(allPossibleGridSizes[1])
    val selectedGridSize: StateFlow<MemorizeGridSize> = _selectedGridSize.asStateFlow()

    private val _maxStrokes = MutableStateFlow(20)
    val maxStrokes: StateFlow<Int> = _maxStrokes.asStateFlow()
    
    private val _selectedMaxStrokes = MutableStateFlow(20)
    val selectedMaxStrokes: StateFlow<Int> = _selectedMaxStrokes.asStateFlow()

    private val _scoresHistory = MutableStateFlow<List<MemorizeGameResult>>(emptyList())
    val scoresHistory: StateFlow<List<MemorizeGameResult>> = _scoresHistory.asStateFlow()

    // --- Game State ---
    private val _cards = MutableStateFlow<List<MemorizeCardState>>(emptyList())
    val cards: StateFlow<List<MemorizeCardState>> = _cards.asStateFlow()

    private val _moves = MutableStateFlow(0)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    private val _gameTimeSeconds = MutableStateFlow(0)
    val gameTimeSeconds: StateFlow<Int> = _gameTimeSeconds.asStateFlow()

    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished: StateFlow<Boolean> = _isGameFinished.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var firstSelectedCardIndex: Int? = null
    private var timerJob: Job? = null

    init {
        val allKanji = kanjiRepository.getAllKanji()
        val maxS = allKanji.maxOfOrNull { it.strokes?.toIntOrNull() ?: 0 } ?: 20
        _maxStrokes.value = maxS
        _selectedMaxStrokes.value = maxS
        updateAvailableGridSizes()
    }

    private fun updateAvailableGridSizes() {
        val levelId = settingsRepository.getSelectedLevel()
        val count = kanjiRepository.getKanjiByLevel(levelId)
            .count { (it.strokes?.toIntOrNull() ?: 0) <= _selectedMaxStrokes.value }
        
        val filtered = allPossibleGridSizes.filter { it.pairsCount <= count }
        
        // Ensure we always have at least the smallest if possible
        _availableGridSizes.value = filtered.ifEmpty { listOf(allPossibleGridSizes.first()) }
        
        // If current selection is no longer available, pick the largest available
        if (_selectedGridSize.value !in _availableGridSizes.value) {
            _selectedGridSize.value = _availableGridSizes.value.last()
        }
    }

    fun onGridSizeSelected(size: MemorizeGridSize) {
        if (size in _availableGridSizes.value) {
            _selectedGridSize.value = size
        }
    }

    fun onMaxStrokesChanged(strokes: Int) {
        _selectedMaxStrokes.value = strokes
        updateAvailableGridSizes()
    }

    fun startGame() {
        val levelId = settingsRepository.getSelectedLevel()
        val allAvailableKanji = kanjiRepository.getKanjiByLevel(levelId)
            .filter { (it.strokes?.toIntOrNull() ?: 0) <= _selectedMaxStrokes.value }
        
        if (allAvailableKanji.isEmpty()) return

        val pairsToSelect = _selectedGridSize.value.pairsCount
        val selectedKanji = allAvailableKanji.shuffled().take(pairsToSelect)
        
        val gameCards = (selectedKanji + selectedKanji)
            .shuffled()
            .mapIndexed { index, kanji ->
                MemorizeCardState(id = index, kanji = kanji)
            }
            
        _cards.value = gameCards
        _moves.value = 0
        _gameTimeSeconds.value = 0
        _isGameFinished.value = false
        firstSelectedCardIndex = null
        
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameTimeSeconds.value++
            }
        }
    }

    fun onCardClicked(index: Int) {
        if (_isProcessing.value || _cards.value[index].isFaceUp || _cards.value[index].isMatched) return

        val currentCards = _cards.value.toMutableList()
        currentCards[index] = currentCards[index].copy(isFaceUp = true)
        _cards.value = currentCards

        if (firstSelectedCardIndex == null) {
            firstSelectedCardIndex = index
        } else {
            _moves.value++
            val firstIndex = firstSelectedCardIndex!!
            if (currentCards[firstIndex].kanji.id == currentCards[index].kanji.id) {
                currentCards[firstIndex] = currentCards[firstIndex].copy(isMatched = true)
                currentCards[index] = currentCards[index].copy(isMatched = true)
                _cards.value = currentCards
                firstSelectedCardIndex = null
                checkGameFinished()
            } else {
                viewModelScope.launch {
                    _isProcessing.value = true
                    delay(800)
                    val resetCards = _cards.value.toMutableList()
                    resetCards[firstIndex] = resetCards[firstIndex].copy(isFaceUp = false)
                    resetCards[index] = resetCards[index].copy(isFaceUp = false)
                    _cards.value = resetCards
                    _isProcessing.value = false
                    firstSelectedCardIndex = null
                }
            }
        }
    }

    private fun checkGameFinished() {
        if (_cards.value.all { it.isMatched }) {
            _isGameFinished.value = true
            timerJob?.cancel()
            saveFinalScore()
        }
    }

    private fun saveFinalScore() {
        val result = MemorizeGameResult(
            moves = _moves.value,
            totalPairs = _selectedGridSize.value.pairsCount,
            gridSizeLabel = _selectedGridSize.value.toString(),
            timeSeconds = _gameTimeSeconds.value,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        val currentHistory = _scoresHistory.value.toMutableList()
        currentHistory.add(0, result)
        _scoresHistory.value = currentHistory.take(10)
    }
}
