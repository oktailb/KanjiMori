package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ComposeDrawingCanvas(
    modifier: Modifier = Modifier,
    onStrokeComplete: (List<InkStroke>) -> Unit,
    clearTrigger: Int
) {
    // State for the strokes that are completed and stored as our KMP abstraction
    val completedStrokes = remember { mutableStateListOf<InkStroke>() }
    // State for the points of the stroke currently being drawn
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    // When the clear trigger changes, reset everything
    LaunchedEffect(clearTrigger) {
        completedStrokes.clear()
        currentPoints = emptyList()
        // Also notify listener that canvas is cleared
        onStrokeComplete(emptyList())
    }

    Canvas(
        modifier = modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    // Start a new stroke by adding the first point
                    currentPoints = listOf(offset)
                },
                onDrag = { change, _ ->
                    change.consume()
                    // Add the new point to the current stroke
                    currentPoints = currentPoints + change.position
                },
                onDragEnd = {
                    // Finalize the current stroke
                    val strokeBuilder = newStrokeBuilder()
                    currentPoints.forEach { point ->
                        // Using 0L for time is a simplification that works for now.
                        strokeBuilder.addPoint(createInkPoint(point.x, point.y, 0L))
                    }
                    completedStrokes.add(strokeBuilder.build())

                    // Notify the listener with the complete list of strokes
                    onStrokeComplete(completedStrokes.toList())

                    // Clear the current points to end the drawing of the current line
                    currentPoints = emptyList()
                },
                onDragCancel = {
                    // Clear the current stroke if the gesture is cancelled
                    currentPoints = emptyList()
                }
            )
        }
    ) {
        val strokeStyle = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Draw completed strokes from their points
        completedStrokes.forEach { inkStroke ->
            val path = Path()
            val points = inkStroke.getPoints()
            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)
                points.forEach { point ->
                    path.lineTo(point.x, point.y)
                }
                drawPath(path = path, color = Color.Black, style = strokeStyle)
            }
        }

        // Draw the current stroke in progress from its points
        if (currentPoints.size > 1) {
            val currentPath = Path()
            currentPath.moveTo(currentPoints.first().x, currentPoints.first().y)
            for (i in 1 until currentPoints.size) {
                currentPath.lineTo(currentPoints[i].x, currentPoints[i].y)
            }
            drawPath(path = currentPath, color = Color.Black, style = strokeStyle)
        }
    }
}
