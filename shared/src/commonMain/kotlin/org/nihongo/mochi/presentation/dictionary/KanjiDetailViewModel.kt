package org.nihongo.mochi.presentation.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordRepository

class KanjiDetailViewModel(
    private val kanjiRepository: KanjiRepository,
    private val meaningRepository: MeaningRepository,
    private val wordRepository: WordRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class KanjiDetailUiState(
        val isLoading: Boolean = false,
        val kanjiCharacter: String? = null,
        val kanjiStrokes: Int = 0,
        val jlptLevel: String? = null,
        val schoolGrade: String? = null,
        val kanjiStructure: String? = null,
        val kanjiMeanings: List<String> = emptyList(),
        val onReadings: List<ReadingItem> = emptyList(),
        val kunReadings: List<ReadingItem> = emptyList(),
        val components: List<ComponentItem> = emptyList(),
        val examples: List<ExampleItem> = emptyList()
    )

    data class ReadingItem(val type: String, val reading: String, val frequency: Int)
    data class ExampleItem(val word: String, val reading: String)
    data class ComponentItem(val character: String, val kanjiRef: String?)

    private val _uiState = MutableStateFlow(KanjiDetailUiState())
    val uiState: StateFlow<KanjiDetailUiState> = _uiState.asStateFlow()

    fun loadKanji(kanjiId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val entry = kanjiRepository.getKanjiById(kanjiId)
            
            if (entry != null) {
                // Parse readings
                val allReadings = entry.readings?.reading?.map { r ->
                    ReadingItem(
                        type = r.type,
                        reading = r.value,
                        frequency = r.frequency?.toIntOrNull() ?: 0
                    )
                } ?: emptyList()

                val onReadings = allReadings.filter { it.type == "on" }
                val kunReadings = allReadings.filter { it.type == "kun" }

                // Parse components
                val components = entry.components?.component?.map { c ->
                    ComponentItem(
                        character = c.text ?: c.kanjiRef ?: "",
                        kanjiRef = c.kanjiRef
                    )
                } ?: emptyList()

                // Load meanings
                val locale = settingsRepository.getAppLocale()
                val meanings = meaningRepository.getMeanings(locale)[kanjiId] ?: emptyList()

                // Load examples
                val examples = wordRepository.getWordsContainingKanji(entry.character)
                    .map { ExampleItem(it.text, it.phonetics) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        kanjiCharacter = entry.character,
                        kanjiStrokes = entry.strokes?.toIntOrNull() ?: 0,
                        jlptLevel = entry.jlptLevel,
                        schoolGrade = entry.schoolGrade,
                        kanjiStructure = entry.components?.structure,
                        kanjiMeanings = meanings,
                        onReadings = onReadings,
                        kunReadings = kunReadings,
                        components = components,
                        examples = examples
                    )
                }
            } else {
                 _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    // Helper needed for navigation logic from UI
    fun findKanjiIdByCharacter(character: String): String? {
        // Warning: This blocking call might be risky if getKanjiByCharacter does heavy DB work.
        // In the original it was running on IO via launch, but the return value logic was tricky.
        // Actually, the original 'navigateToKanji' in Fragment launched a coroutine to call this.
        // Since this method just returns data, it's fine as is, provided the caller handles threads.
        // But getKanjiByCharacter in repo should be thread-safe.
        val entry = kanjiRepository.getKanjiByCharacter(character)
        return entry?.id
    }
}
