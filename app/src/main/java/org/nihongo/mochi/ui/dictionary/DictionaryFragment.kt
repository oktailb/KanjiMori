package org.nihongo.mochi.ui.dictionary

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.recognition.ModelStatus
import org.nihongo.mochi.domain.recognition.RecognitionPoint
import org.nihongo.mochi.domain.recognition.RecognitionStroke
import org.nihongo.mochi.ui.theme.AppTheme
import kotlin.math.max
import kotlin.math.min

class DictionaryFragment : Fragment() {

    private val androidRecognizer = AndroidMlKitRecognizer()

    private val viewModel: DictionaryViewModel by activityViewModels {
        viewModelFactory {
            initializer {
                DictionaryViewModel(
                    handwritingRecognizer = androidRecognizer,
                    kanjiRepository = get(),
                    meaningRepository = get(),
                    settingsRepository = get(),
                    levelsRepository = get() // Added levelsRepository here
                )
            }
        }
    }
    
    // State for showing the drawing dialog
    private val showDrawingDialog = mutableStateOf(false)
    
    // Keep track of the drawing bitmap state for Compose
    private val drawingBitmapState = mutableStateOf<Bitmap?>(null)
    
    // Keep track of the last Ink object for internal logic
    private var lastUiInk: Ink? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    // Main Screen
                    DictionaryScreen(
                        viewModel = viewModel,
                        drawingBitmap = drawingBitmapState.value,
                        onOpenDrawing = { checkAndOpenDrawing() },
                        onClearDrawing = { clearDrawing() },
                        onItemClick = { item ->
                            val action = DictionaryFragmentDirections.actionDictionaryToKanjiDetail(item.id)
                            findNavController().navigate(action)
                        }
                    )
                    
                    // Drawing Dialog (Overlay)
                    if (showDrawingDialog.value) {
                        ComposeDrawingDialog(
                            onDismiss = { showDrawingDialog.value = false },
                            onInkDrawn = { ink ->
                                processInk(ink)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrawingObservation()
        
        // Ensure model is downloaded
        viewModel.downloadModel()
        
        // Initial load
        viewModel.loadDictionaryData()
    }
    
    private fun checkAndOpenDrawing() {
         // Check model status before showing dialog
        if (viewModel.modelStatus.value != ModelStatus.DOWNLOADED) {
            Toast.makeText(context, "Model not ready: ${viewModel.modelStatus.value}", Toast.LENGTH_SHORT).show()
            viewModel.downloadModel() // Try again
            return
        }
        showDrawingDialog.value = true
    }
    
    private fun processInk(ink: Ink) {
        lastUiInk = ink
        updateDrawingThumbnail()
        val strokes = convertInkToStrokes(ink)
        Log.i("DictionaryFragment", "Submitting ${strokes.size} strokes for recognition")
        viewModel.recognizeInk(strokes)
    }
    
    private fun clearDrawing() {
        lastUiInk = null
        updateDrawingThumbnail()
        viewModel.clearDrawingFilter()
    }

    private fun convertInkToStrokes(ink: Ink): List<RecognitionStroke> {
        val strokes = mutableListOf<RecognitionStroke>()
        for (inkStroke in ink.strokes) {
            val points = mutableListOf<RecognitionPoint>()
            for (inkPoint in inkStroke.points) {
                points.add(RecognitionPoint(inkPoint.x, inkPoint.y, inkPoint.timestamp ?: 0L))
            }
            strokes.add(RecognitionStroke(points))
        }
        return strokes
    }

    private fun setupDrawingObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recognitionResults.collect { candidates ->
                     if (candidates == null) {
                         if (lastUiInk != null) {
                             lastUiInk = null
                             updateDrawingThumbnail()
                         }
                     } else {
                         // Results received
                         val strokes = viewModel.lastStrokes
                         if (strokes != null) {
                             lastUiInk = strokesToInk(strokes)
                             updateDrawingThumbnail()
                         }
                     }
                }
            }
        }
    }

    private fun strokesToInk(strokes: List<RecognitionStroke>): Ink {
        val builder = Ink.builder()
        for (stroke in strokes) {
            val inkStrokeBuilder = Ink.Stroke.builder()
            for (point in stroke.points) {
                inkStrokeBuilder.addPoint(Ink.Point.create(point.x, point.y, point.t))
            }
            builder.addStroke(inkStrokeBuilder.build())
        }
        return builder.build()
    }

    private fun updateDrawingThumbnail() {
        if (lastUiInk != null) {
            val bitmap = renderInkToBitmap(lastUiInk!!, 100)
            drawingBitmapState.value = bitmap
        } else {
            drawingBitmapState.value = null
        }
    }
    
    private fun renderInkToBitmap(ink: Ink, targetSize: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        if (ink.strokes.isEmpty()) return bitmap

        for (stroke in ink.strokes) {
            for (point in stroke.points) {
                minX = min(minX, point.x)
                minY = min(minY, point.y)
                maxX = max(maxX, point.x)
                maxY = max(maxY, point.y)
            }
        }

        val drawingWidth = maxX - minX
        val drawingHeight = maxY - minY
        if (drawingWidth <= 0 || drawingHeight <= 0) return bitmap

        val margin = targetSize * 0.1f
        val effectiveSize = targetSize - (2 * margin)
        val scale = min(effectiveSize / drawingWidth, effectiveSize / drawingHeight)

        val dx = -minX * scale + margin
        val dy = -minY * scale + margin

        canvas.translate(dx, dy)
        canvas.scale(scale, scale)

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            strokeWidth = 3f / scale // Keep stroke width constant regardless of scale
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        for (stroke in ink.strokes) {
            val path = Path()
            if (stroke.points.isNotEmpty()) {
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
                canvas.drawPath(path, paint)
            }
        }
        return bitmap
    }
}
