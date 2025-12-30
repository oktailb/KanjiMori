package org.nihongo.mochi.ui.dictionary

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.R
import org.nihongo.mochi.domain.dictionary.DictionaryItem
import org.nihongo.mochi.domain.dictionary.SearchMode
import org.nihongo.mochi.presentation.MochiBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    viewModel: org.nihongo.mochi.domain.dictionary.DictionaryViewModel,
    drawingBitmap: Bitmap?,
    onOpenDrawing: () -> Unit,
    onClearDrawing: () -> Unit,
    onItemClick: (DictionaryItem) -> Unit
) {
    val results by viewModel.lastResults.collectAsState()
    val focusManager = LocalFocusManager.current
    
    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // --- Filter Section ---
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    
                    // Text Search & Drawing Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.textQuery,
                            onValueChange = { 
                                viewModel.textQuery = it
                                viewModel.applyFilters()
                            },
                            label = { Text(stringResource(R.string.dictionary_search_hint_text)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )
                        
                        IconButton(
                            onClick = onOpenDrawing,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.dictionary_search_by_drawing_desc),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Search Mode Radio Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { 
                                viewModel.searchMode = SearchMode.READING
                                viewModel.applyFilters()
                            }
                        ) {
                            RadioButton(
                                selected = viewModel.searchMode == SearchMode.READING,
                                onClick = { 
                                    viewModel.searchMode = SearchMode.READING
                                    viewModel.applyFilters()
                                }
                            )
                            Text(
                                text = stringResource(R.string.dictionary_mode_reading),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { 
                                viewModel.searchMode = SearchMode.MEANING
                                viewModel.applyFilters()
                            }
                        ) {
                            RadioButton(
                                selected = viewModel.searchMode == SearchMode.MEANING,
                                onClick = { 
                                    viewModel.searchMode = SearchMode.MEANING
                                    viewModel.applyFilters()
                                }
                            )
                            Text(
                                text = stringResource(R.string.dictionary_mode_meaning),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Stroke Count, Exact Match, Thumbnail
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.strokeQuery,
                            onValueChange = { 
                                viewModel.strokeQuery = it
                                viewModel.applyFilters()
                            },
                            label = { Text(stringResource(R.string.dictionary_search_hint_strokes)) },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .clickable { 
                                    viewModel.exactMatch = !viewModel.exactMatch
                                    viewModel.applyFilters()
                                }
                        ) {
                            Checkbox(
                                checked = viewModel.exactMatch,
                                onCheckedChange = { 
                                    viewModel.exactMatch = it
                                    viewModel.applyFilters()
                                }
                            )
                            Text(
                                text = stringResource(R.string.dictionary_match_exact),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Drawing Thumbnail
                        if (drawingBitmap != null) {
                            Card(
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Image(
                                    bitmap = drawingBitmap.asImageBitmap(),
                                    contentDescription = "Drawing",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            IconButton(onClick = onClearDrawing) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.dictionary_clear_drawing_desc),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // --- Results Count (Moved inside the surface) ---
                    Text(
                        text = stringResource(R.string.dictionary_results_count_format, results.size),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Results List ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { item ->
                    DictionaryItemRow(item = item, onClick = onItemClick)
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun DictionaryItemRow(
    item: DictionaryItem,
    onClick: (DictionaryItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kanji Character
        Text(
            text = item.character,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(50.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Details
        Column(modifier = Modifier.weight(1f)) {
            // Readings
            val onReadings = item.readings.filter { it.type == "on" }.map { hiraganaToKatakana(it.text) }
            val kunReadings = item.readings.filter { it.type == "kun" }.map { it.text }
            
            val readingText = buildString {
                if (onReadings.isNotEmpty()) {
                    append(stringResource(R.string.dictionary_reading_on))
                    append(" ")
                    append(onReadings.joinToString(", "))
                }
                if (kunReadings.isNotEmpty()) {
                    if (isNotEmpty()) append("  ")
                    append(stringResource(R.string.dictionary_reading_kun))
                    append(" ")
                    append(kunReadings.joinToString(", "))
                }
            }
            
            Text(
                text = readingText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Meanings
            Text(
                text = item.meanings.joinToString(", "),
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stroke Count
        Text(
            text = "${item.strokeCount} traits",
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun hiraganaToKatakana(s: String): String {
    return s.map { c ->
        if (c in '\u3041'..'\u3096') {
            (c + 0x60)
        } else {
            c
        }
    }.joinToString("")
}
