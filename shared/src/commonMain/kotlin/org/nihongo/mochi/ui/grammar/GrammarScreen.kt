package org.nihongo.mochi.ui.grammar

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.toori
import org.nihongo.mochi.ui.ResourceUtils

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grammar Tree") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
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
                    // Total height calculation: 
                    // This is tricky because we want the tree to be scrollable.
                    // For now, let's assume a fixed height per level or based on the number of levels.
                    // In a real implementation, you might want a zoomable canvas.
                    val canvasHeight = 2000.dp // Arbitrary large height
                    val canvasWidth = maxWidth

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(canvasHeight)
                    ) {
                        // Draw Separators (Toori Gates)
                        separators.forEach { separator ->
                            val yPos = separator.y * canvasHeight.value
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .offset(y = yPos.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                            
                            Image(
                                painter = painterResource(Res.drawable.toori),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.TopCenter)
                                    .offset(y = (yPos - 30).dp), // Centered on the line
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Draw Connections
                        // (To be implemented: Draw lines between dependencies)

                        // Draw Nodes
                        nodes.forEach { node ->
                            val xPos = node.x * canvasWidth.value
                            val yPos = node.y * canvasHeight.value
                            
                            GrammarNodeItem(
                                node = node,
                                modifier = Modifier
                                    .offset(
                                        x = (xPos - 50).dp, // Center horizontally (width 100)
                                        y = (yPos - 30).dp  // Center vertically (height 60)
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
