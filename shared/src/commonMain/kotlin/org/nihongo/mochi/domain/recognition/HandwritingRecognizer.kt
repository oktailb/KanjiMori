package org.nihongo.mochi.domain.recognition

import kotlinx.coroutines.flow.StateFlow

data class RecognitionPoint(val x: Float, val y: Float, val t: Long)

data class RecognitionStroke(val points: List<RecognitionPoint>)

enum class ModelStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED
}

interface HandwritingRecognizer {
    val modelStatus: StateFlow<ModelStatus>
    
    fun downloadModel()
    fun recognize(strokes: List<RecognitionStroke>, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit)
    fun isInitialized(): Boolean
}
