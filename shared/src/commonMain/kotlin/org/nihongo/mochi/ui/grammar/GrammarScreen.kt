package org.nihongo.mochi.ui.grammar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.stonepath
import org.nihongo.mochi.shared.generated.resources.toori
import org.nihongo.mochi.ui.ResourceUtils
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    viewModel: GrammarViewModel,
    onBackClick: () -> Unit
) {
    val nodes by viewModel.nodes.collectAsState()
    val separators by viewModel.separators.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val stonePathBitmap = remember { 
        // We can't synchronously load resources inside composable easily without a loader
        // But for KMP/Compose Multiplatform 'painterResource' returns a Painter. 
        // For drawing on Canvas we prefer ImageBitmap.
        // Let's use a simpler approach: Draw the path using a loop of Image composables in a Box BEHIND the canvas content?
        // Or better: Use 'painterResource' inside a draw scope if possible? No.
        // We will use 'imageResource' which is a suspend function, usually loaded via LaunchedEffect or produceState.
        // For simplicity and stability, let's just use a Box with repeated Images in the background layer (Z-order).
        null
    }
    
    // We'll use a separate Composable for the background path to handle resource loading cleanly
    val stonePathPainter = painterResource(Res.drawable.stonepath)

    Scaffold(
        // No TopBar
    ) { paddingValues ->
        MochiBackground {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    val estimatedHeight = (nodes.size * 100).dp + (separators.size * 350).dp + 2300.dp
                    val minCanvasHeight = 2000.dp
                    val canvasHeight = max(minCanvasHeight, estimatedHeight)
                    
                    val canvasWidth = maxWidth
                    
                    val nodesById = remember(nodes) { nodes.associateBy { it.rule.id } }
                    val nodesByLevel = remember(nodes) { nodes.groupBy { it.rule.level } }
                    
                    // Pre-calculate Outer Tracks (X-Offsets) to prevent overlapping vertical lines
                    // Key: Pair(ParentId, ChildId) -> TrackIndex
                    val outerConnectionTracks = remember(nodes) {
                        data class ConnInfo(
                            val key: Pair<String, String>, 
                            val minY: Float, 
                            val maxY: Float,
                            val length: Float
                        )
                        
                        val tracksLeft = mutableListOf<Float>() // Stores the maxY of the last connection on this track
                        val tracksRight = mutableListOf<Float>()
                        val mapping = mutableMapOf<Pair<String, String>, Int>()
                        val bufferY = 0.02f // Small vertical buffer so lines don't touch end-to-end perfectly

                        // 1. Gather all intra-level same-side connections
                        val connections = nodes.flatMap { child ->
                            child.rule.dependencies.mapNotNull { parentId ->
                                val parent = nodesById[parentId] ?: return@mapNotNull null
                                if (parent.rule.level == child.rule.level) {
                                    val isLeft = child.x < 0.5f
                                    val isParentLeft = parent.x < 0.5f
                                    if (isLeft == isParentLeft) {
                                        val minY = min(parent.y, child.y)
                                        val maxY = max(parent.y, child.y)
                                        Triple(ConnInfo(parentId to child.rule.id, minY, maxY, maxY - minY), isLeft, minY)
                                    } else null
                                } else null
                            }
                        }
                        // 2. Sort by Length ASCENDING (Shortest wires inside), then by Position
                        .sortedWith(compareBy({ it.first.length }, { it.first.minY }))

                        // 3. Allocate Tracks
                        connections.forEach { (info, isLeft, _) ->
                            val tracks = if (isLeft) tracksLeft else tracksRight
                            
                            // Find the first track that is free at info.minY
                            // A track is free if trackEnd < info.minY
                            var allocatedTrack = -1
                            for (i in tracks.indices) {
                                if (tracks[i] + bufferY < info.minY) {
                                    tracks[i] = info.maxY // Extend track
                                    allocatedTrack = i
                                    break
                                }
                            }
                            
                            if (allocatedTrack == -1) {
                                // Create new track
                                tracks.add(info.maxY)
                                allocatedTrack = tracks.lastIndex
                            }
                            
                            mapping[info.key] = allocatedTrack
                        }
                        mapping
                    }
                    
                    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    val separatorLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                    val nodeWidthDp = 100.dp
                    val nodeHeightDp = 60.dp
                    val nodeHalfWidthPx = with(density) { (nodeWidthDp / 2).toPx() }
                    // Used implicitly for visual vertical centering calculations
                    val nodeHeightPx = with(density) { nodeHeightDp.toPx() } 
                    val centerChannelPadding = with(density) { 0.dp.toPx() }
                    val cornerRadius = with(density) { 16.dp.toPx() }
                    val trackSpacingPx = with(density) { 4.dp.toPx() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(canvasHeight)
                    ) {
                        // LAYER 1: Links (Bottom)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height
                            val centerX = canvasW / 2

                            // 1. Draw Links from Nodes to their Level's Toori (Structural, Faint)
                            separators.forEach { separator ->
                                val gateY = separator.y * canvasH
                                val levelNodes = nodesByLevel[separator.levelId] ?: emptyList()
                                
                                levelNodes.forEach { node ->
                                    val childAnchorX = if (node.x < 0.5f) {
                                        (node.x * canvasW) + nodeHalfWidthPx // Right edge
                                    } else {
                                        (node.x * canvasW) - nodeHalfWidthPx // Left edge
                                    }
                                    val childCenterY = node.y * canvasH
                                    
                                    val childChannelX = if (node.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                    
                                    val path = Path()
                                    path.moveTo(childAnchorX, childCenterY)
                                    
                                    val distY = gateY - childCenterY
                                    val dir = sign(distY)

                                    if (abs(distY) > cornerRadius * 1.5f) {
                                        val turnY = childCenterY + (dir * cornerRadius)
                                        // 1. Curve into channel
                                        path.quadraticBezierTo(childChannelX, childCenterY, childChannelX, turnY)
                                        // 2. Vertical line to Gate Y
                                        path.lineTo(childChannelX, gateY)
                                    } else {
                                        // Too close: S-Curve
                                        path.cubicTo(
                                            childChannelX, childCenterY, 
                                            childChannelX, gateY, 
                                            childChannelX, gateY
                                        )
                                    }
                                    
                                    drawPath(
                                        path = path,
                                        color = lineColor,
                                        style = Stroke(
                                            width = 2.dp.toPx(),
                                            pathEffect = null // Solid line, no dashes
                                        )
                                    )
                                }
                            }

                            // 2. Draw Dependency Connections
                            nodes.forEach { node ->
                                node.rule.dependencies.forEach { dependencyId ->
                                    val parentNode = nodesById[dependencyId]
                                    if (parentNode != null) {
                                        val parentCenterY = parentNode.y * canvasH
                                        val childCenterY = node.y * canvasH
                                        
                                        // Robust check for Inter-Level Dependency
                                        val isInterLevel = parentNode.rule.level != node.rule.level
                                        
                                        val path = Path()

                                        if (isInterLevel) {
                                            // Inter-level: Pipe Style via Gate
                                            val gate = separators
                                                .filter { it.y < node.y }
                                                .maxByOrNull { it.y }
                                            
                                            if (gate != null) {
                                                val gateY = gate.y * canvasH
                                                
                                                val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) + nodeHalfWidthPx else (node.x * canvasW) - nodeHalfWidthPx
                                                val childAnchorY = childCenterY
                                                
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) + nodeHalfWidthPx else (parentNode.x * canvasW) - nodeHalfWidthPx
                                                val parentAnchorY = parentCenterY
                                                
                                                val parentChannelX = if(parentNode.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                
                                                // Part A: Parent -> Gate
                                                val pathParentToGate = Path()
                                                pathParentToGate.moveTo(parentAnchorX, parentAnchorY)

                                                val distToGate = gateY - parentAnchorY
                                                if (abs(distToGate) > cornerRadius * 1.5f) {
                                                    val turnY = if(distToGate > 0) parentAnchorY + cornerRadius else parentAnchorY - cornerRadius
                                                    pathParentToGate.quadraticBezierTo(parentChannelX, parentAnchorY, parentChannelX, turnY)
                                                    pathParentToGate.lineTo(parentChannelX, gateY)
                                                } else {
                                                    pathParentToGate.cubicTo(parentChannelX, parentAnchorY, centerX, parentAnchorY + distToGate * 0.5f, centerX, gateY)
                                                }
                                                
                                                drawPath(
                                                    path = pathParentToGate,
                                                    color = lineColor, 
                                                    style = Stroke(width = 2.dp.toPx())
                                                )

                                                // Part B: Gate -> Child
                                                val childChannelX = if(node.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                
                                                path.moveTo(childChannelX, gateY)
                                                
                                                val distFromGate = childAnchorY - gateY
                                                if (abs(distFromGate) > cornerRadius * 1.5f) {
                                                     val turnY = childAnchorY - cornerRadius 
                                                     path.lineTo(childChannelX, turnY)
                                                     path.quadraticBezierTo(childChannelX, childAnchorY, childAnchorX, childAnchorY)
                                                } else {
                                                    path.cubicTo(childChannelX, gateY + distFromGate * 0.5f, childChannelX, childAnchorY, childAnchorX, childAnchorY)
                                                }
                                            }
                                        } else {
                                            // Intra-level
                                            val isCrossing = (parentNode.x < 0.5f) != (node.x < 0.5f)
                                            
                                            if (isCrossing) {
                                                // Pipe Style: Inner Side -> Center Channel -> Cross -> Center Channel -> Inner Side
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) + nodeHalfWidthPx else (parentNode.x * canvasW) - nodeHalfWidthPx
                                                val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) + nodeHalfWidthPx else (node.x * canvasW) - nodeHalfWidthPx
                                                
                                                val parentChannelX = if(parentNode.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                val childChannelX = if(node.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                
                                                path.moveTo(parentAnchorX, parentCenterY)
                                                
                                                val distY = childCenterY - parentCenterY
                                                val dir = sign(distY)
                                                
                                                if (abs(distY) > cornerRadius * 2.5f) {
                                                    // Vertical Pipe with Crossing
                                                    val turn1Y = parentCenterY + (dir * cornerRadius)
                                                    val midY = parentCenterY + (distY / 2)
                                                    val turn2Y = childCenterY - (dir * cornerRadius)
                                                    
                                                    // 1. Enter Channel
                                                    path.quadraticBezierTo(parentChannelX, parentCenterY, parentChannelX, turn1Y)
                                                    // 2. Vertical to near Mid
                                                    path.lineTo(parentChannelX, midY - (dir * cornerRadius))
                                                    // 3. Cross Over (S-Curve)
                                                    path.cubicTo(parentChannelX, midY, childChannelX, midY, childChannelX, midY + (dir * cornerRadius))
                                                    // 4. Vertical to near Child
                                                    path.lineTo(childChannelX, turn2Y)
                                                    // 5. Exit Channel
                                                    path.quadraticBezierTo(childChannelX, childCenterY, childAnchorX, childCenterY)
                                                } else {
                                                    // Too close for full pipe: S-Curve across center
                                                    path.cubicTo(
                                                        parentChannelX, parentCenterY,
                                                        childChannelX, childCenterY,
                                                        childAnchorX, childCenterY
                                                    )
                                                }
                                            } else {
                                                // Pipe Style: Outer Side -> Outer Channel -> Vertical -> Outer Channel -> Outer Side
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) - nodeHalfWidthPx else (parentNode.x * canvasW) + nodeHalfWidthPx
                                                val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) - nodeHalfWidthPx else (node.x * canvasW) + nodeHalfWidthPx
                                                
                                                // Retrieve allocated track index
                                                val trackIndex = outerConnectionTracks[dependencyId to node.rule.id] ?: 0
                                                val dynamicExtra = trackIndex * trackSpacingPx
                                                
                                                val baseOffset = 40f
                                                val totalOffset = baseOffset + dynamicExtra
                                                
                                                val controlOffset = if (parentNode.x < 0.5f) -totalOffset else totalOffset
                                                val outerChannelX = parentAnchorX + controlOffset
                                                
                                                path.moveTo(parentAnchorX, parentCenterY)
                                                
                                                val distY = childCenterY - parentCenterY
                                                val dir = sign(distY)

                                                if (abs(distY) > cornerRadius * 2f) {
                                                    val turn1Y = parentCenterY + (dir * cornerRadius)
                                                    val turn2Y = childCenterY - (dir * cornerRadius)
                                                    
                                                    // 1. Enter Outer Channel
                                                    path.quadraticBezierTo(outerChannelX, parentCenterY, outerChannelX, turn1Y)
                                                    // 2. Vertical Line
                                                    path.lineTo(outerChannelX, turn2Y)
                                                    // 3. Exit Outer Channel
                                                    path.quadraticBezierTo(outerChannelX, childCenterY, childAnchorX, childCenterY)
                                                } else {
                                                    // Too close: Simple Bezier
                                                    path.cubicTo(
                                                        outerChannelX, parentCenterY,
                                                        outerChannelX, childCenterY,
                                                        childAnchorX, childCenterY
                                                    )
                                                }
                                            }
                                        }

                                        if (!path.isEmpty) {
                                            drawPath(
                                                path = path,
                                                color = lineColor,
                                                style = Stroke(
                                                    width = 2.dp.toPx(),
                                                    pathEffect = null
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // LAYER 2: Stone Path (Above Links, Below Content)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                             // We want to tile the image vertically from top to bottom
                             // Since we don't know the exact height of the drawable in DP easily without loading it,
                             // we can use a Column with repeating Images. 
                             // Assuming the drawable has a reasonable aspect ratio.
                             // Let's fix the width to 24.dp as requested.
                             val stoneWidth = 24.dp
                             
                             // We need to cover 'canvasHeight'
                             // We can use a Column or a lazy column or a custom layout.
                             // Simple Column is fine since canvasHeight is finite.
                             
                             // NOTE: Ideally we should measure the image to know how many times to repeat.
                             // Here we will just repeat enough times to cover a large height, 
                             // or use a Box with repeat background pattern if Modifier supported it easily in Compose.
                             // Let's use a Column with weight/fill? No, height is explicit.
                             
                             Column(
                                 modifier = Modifier.width(stoneWidth)
                             ) {
                                 val repeatCount = 250 // Enough to cover 2000+ dp if each stone is e.g. 50dp
                                 repeat(repeatCount) {
                                     Image(
                                         painter = stonePathPainter,
                                         contentDescription = null,
                                         contentScale = ContentScale.FillWidth,
                                         modifier = Modifier.fillMaxWidth(),
                                         alpha = 0.9f
                                     )
                                 }
                             }
                        }

                        // LAYER 3: Separators (Toori Gates + Text)
                        separators.forEach { separator ->
                            val yPosPx = separator.y * canvasHeight.value
                            val levelName = ResourceUtils.resolveStringResource("level_${separator.levelId}")?.let { 
                                stringResource(it) 
                            } ?: separator.levelId.uppercase()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (yPosPx).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // The Gate and Text
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.offset(y = (-180).dp)
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.toori),
                                        contentDescription = null,
                                        modifier = Modifier.size(280.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    
                                    Text(
                                        text = levelName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .offset(y = (-100).dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // LAYER 4: Nodes
                        nodes.forEach { node ->
                            val xPosPx = node.x * canvasWidth.value
                            val yPosPx = node.y * canvasHeight.value
                            
                            GrammarNodeItem(
                                node = node,
                                modifier = Modifier
                                    .offset(
                                        x = (xPosPx - 50).dp, // Center horizontally (width 100)
                                        y = (yPosPx - 30).dp  // Center vertically (height 60)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrammarNodeItem(
    node: GrammarNode,
    modifier: Modifier = Modifier
) {
    val description = ResourceUtils.resolveStringResource(node.rule.description)?.let { 
        stringResource(it) 
    } ?: node.rule.id

    Box(
        modifier = modifier
            .width(100.dp)
            .height(60.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 3,
            lineHeight = 12.sp
        )
    }
}
