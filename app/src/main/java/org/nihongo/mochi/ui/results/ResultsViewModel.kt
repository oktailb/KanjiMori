package org.nihongo.mochi.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.services.CloudSaveService
import org.nihongo.mochi.domain.statistics.StatisticsEngine

class ResultsViewModel(
    private val cloudSaveService: CloudSaveService,
    private val statisticsEngine: StatisticsEngine
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Used for snapshot naming, logic could be moved if needed
    private var currentSaveName = "NihongoMochiSnapshot"

    init {
        checkSignInStatus()
    }

    fun checkSignInStatus() {
        viewModelScope.launch {
            _isAuthenticated.value = cloudSaveService.isAuthenticated()
        }
    }

    fun signIn() {
        viewModelScope.launch {
            val success = cloudSaveService.signIn()
            _isAuthenticated.value = success
            if (!success) {
                _message.value = "Connexion échouée"
            }
        }
    }

    fun saveGame() {
        viewModelScope.launch {
            val data = ScoreManager.getAllDataJson()
            val desc = "Backup " + java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date())
            
            // Generate unique name if needed (logic from Fragment moved here partially, 
            // but unique name generation on NEW snapshot is tricky without knowing if we are creating new or overwriting)
            // For now, we assume overwriting currentSaveName or creating if passed explicitly.
            // Complex save flows often need UI interaction (selecting slot). 
            // The Service abstraction might need to return the Intent for the UI to handle the selection, 
            // then callback with the chosen snapshot name.
            
            // Since the original code used the UI to pick a snapshot, we might need to keep that part in Fragment 
            // or abstract it further.
            
            // For direct save (if we knew the name):
            val success = cloudSaveService.saveGame(currentSaveName, data, desc)
            if (success) {
                _message.value = "Sauvegarde effectuée"
            } else {
                _message.value = "Erreur de sauvegarde"
            }
        }
    }

    fun loadGame(data: String) {
        // Called after getting data from Service/UI
        ScoreManager.restoreDataFromJson(data)
        _message.value = "Données restaurées"
        // Trigger UI refresh of stats
    }
    
    fun setCurrentSaveName(name: String) {
        currentSaveName = name
    }

    fun clearMessage() {
        _message.value = null
    }
}
