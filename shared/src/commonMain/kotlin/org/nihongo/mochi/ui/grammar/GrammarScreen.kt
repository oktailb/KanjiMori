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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.toori
import org.nihongo.mochi.ui.ResourceUtils
import kotlin.math.abs
import kotlin.math.min

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
                    val estimatedHeight = (nodes.size * 120).dp + (separators.size * 250).dp + 300.dp
                    val minCanvasHeight = 2000.dp
                    val canvasHeight = max(minCanvasHeight, estimatedHeight)
                    
                    val canvasWidth = maxWidth
                    
                    val nodesById = remember(nodes) { nodes.associateBy { it.rule.id } }
                    val nodesByLevel = remember(nodes) { nodes.groupBy { it.rule.level } }
                    
                    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    val separatorLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                    val nodeWidthDp = 100.dp
                    val nodeHeightDp = 60.dp
                    val nodeHalfWidthPx = with(density) { (nodeWidthDp / 2).toPx() }
                    val nodeHeightPx = with(density) { nodeHeightDp.toPx() } 

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(canvasHeight)
                    ) {
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
                                    
                                    val path = Path()
                                    path.moveTo(childAnchorX, childCenterY)
                                    
                                    path.cubicTo(
                                        x1 = childAnchorX + (if(node.x < 0.5f) 50f else -50f), y1 = childCenterY, 
                                        x2 = centerX, y2 = childCenterY + (gateY - childCenterY) * 0.5f, 
                                        x3 = centerX, y3 = gateY 
                                    )
                                    
                                    drawPath(
                                        path = path,
                                        color = lineColor.copy(alpha = 0.2f), // Very faint
                                        style = Stroke(
                                            width = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
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
                                            // Inter-level dependency: Source is the Toori of the parent's level
                                            // Find the gate corresponding to the parent's level, OR the last gate before the child
                                            // We use separators list which is sorted by Y.
                                            // Find the gate belonging to the level preceding the child's level
                                            // Or more simply: Find the last gate that is ABOVE the child
                                            val gate = separators
                                                .filter { it.y < node.y }
                                                .maxByOrNull { it.y }
                                            
                                            if (gate != null) {
                                                val gateX = centerX
                                                val gateY = gate.y * canvasH
                                                
                                                // Target: Inner Side (closest to center)
                                                val childAnchorX = if (node.x < 0.5f) {
                                                    (node.x * canvasW) + nodeHalfWidthPx // Right edge for left nodes
                                                } else {
                                                    (node.x * canvasW) - nodeHalfWidthPx // Left edge for right nodes
                                                }
                                                val childAnchorY = childCenterY // Vertical center
                                                
                                                // Part A: Draw STRONG link from Parent -> Toori
                                                // Parent Anchor: Inner Side
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) + nodeHalfWidthPx else (parentNode.x * canvasW) - nodeHalfWidthPx
                                                val parentAnchorY = parentCenterY
                                                
                                                val pathParentToGate = Path()
                                                pathParentToGate.moveTo(parentAnchorX, parentAnchorY)
                                                pathParentToGate.cubicTo(
                                                    x1 = parentAnchorX + (if(parentNode.x < 0.5f) 50f else -50f), y1 = parentAnchorY,
                                                    x2 = centerX, y2 = parentAnchorY + (gateY - parentAnchorY) * 0.5f,
                                                    x3 = centerX, y3 = gateY
                                                )
                                                drawPath(
                                                    path = pathParentToGate,
                                                    color = lineColor, // Strong color
                                                    style = Stroke(
                                                        width = 2.dp.toPx(),
                                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                    )
                                                )

                                                // Part B: Draw Link from Toori -> Child (Inner Side)
                                                path.moveTo(gateX, gateY)
                                                
                                                // Curve from Gate Center -> Child Inner Side
                                                path.cubicTo(
                                                    x1 = gateX, y1 = gateY + (childAnchorY - gateY) * 0.5f, // Down from gate
                                                    x2 = childAnchorX + (if(node.x < 0.5f) 50f else -50f), y2 = childAnchorY, // Approach from center side
                                                    x3 = childAnchorX, y3 = childAnchorY
                                                )
                                            }
                                        } else {
                                            // Intra-level dependency (Same level): Go around the OUTSIDE
                                            
                                            // Anchors at OUTER sides
                                            val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) - nodeHalfWidthPx else (parentNode.x * canvasW) + nodeHalfWidthPx
                                            val parentAnchorY = parentCenterY
                                            
                                            val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) - nodeHalfWidthPx else (node.x * canvasW) + nodeHalfWidthPx
                                            val childAnchorY = childCenterY
                                            
                                            path.moveTo(parentAnchorX, parentAnchorY)
                                            
                                            // Control points to curve outward
                                            // Scale offset based on vertical distance to separate lines
                                            val distY = abs(childAnchorY - parentAnchorY)
                                            val baseOffset = 40f
                                            val dynamicOffset = min(distY * 0.2f, 100f) // Cap max width
                                            val totalOffset = baseOffset + dynamicOffset
                                            
                                            val controlOffset = if (parentNode.x < 0.5f) -totalOffset else totalOffset
                                            
                                            path.cubicTo(
                                                x1 = parentAnchorX + controlOffset, y1 = parentAnchorY, 
                                                x2 = childAnchorX + controlOffset, y2 = childAnchorY, 
                                                x3 = childAnchorX, y3 = childAnchorY
                                            )
                                        }

                                        if (!path.isEmpty) {
                                            drawPath(
                                                path = path,
                                                color = lineColor,
                                                style = Stroke(
                                                    width = 2.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Draw Separators (Toori Gates + Text)
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
                                // The Line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(
                                            color = separatorLineColor,
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                )
                                
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

                        // Draw Nodes
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
