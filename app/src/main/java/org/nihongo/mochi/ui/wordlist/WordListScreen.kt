package org.nihongo.mochi.ui.wordlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.R
import org.nihongo.mochi.domain.words.WordEntry
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.PaginationControls
import org.nihongo.mochi.ui.components.PlayButton

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    listTitle: String,
    wordsWithColors: List<Triple<WordEntry, Color, Boolean>>, // Word, Color, isRedBorder
    currentPage: Int,
    totalPages: Int,
    filterKanjiOnly: Boolean,
    filterSimpleWords: Boolean,
    filterCompoundWords: Boolean,
    filterIgnoreKnown: Boolean,
    selectedWordType: Pair<String, String>,
    wordTypeOptions: List<Pair<String, String>>,
    onFilterKanjiOnlyChange: (Boolean) -> Unit,
    onFilterSimpleWordsChange: (Boolean) -> Unit,
    onFilterCompoundWordsChange: (Boolean) -> Unit,
    onFilterIgnoreKnownChange: (Boolean) -> Unit,
    onWordTypeChange: (Pair<String, String>) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onPlayClick: () -> Unit
) {
    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = listTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            // Filters
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilterCheckbox(
                            text = stringResource(R.string.reading_kanji_solo),
                            checked = filterKanjiOnly,
                            onCheckedChange = onFilterKanjiOnlyChange,
                            modifier = Modifier.weight(1f)
                        )
                        FilterCheckbox(
                            text = stringResource(R.string.reading_simple_words),
                            checked = filterSimpleWords,
                            onCheckedChange = onFilterSimpleWordsChange,
                            modifier = Modifier.weight(1f)
                        )
                        FilterCheckbox(
                            text = stringResource(R.string.reading_compound_words),
                            checked = filterCompoundWords,
                            onCheckedChange = onFilterCompoundWordsChange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Custom Dropdown for Word Type
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .menuAnchor() // menuAnchor handles interactions (click/focus)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                .clickable { expanded = !expanded }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedWordType.second,
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            wordTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.second, color = Color.Black) },
                                    onClick = {
                                        onWordTypeChange(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    FilterCheckbox(
                        text = stringResource(R.string.reading_ignore_known_words),
                        checked = filterIgnoreKnown,
                        onCheckedChange = onFilterIgnoreKnownChange
                    )
                }
            }

            // Word List with scrolling
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                 FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    wordsWithColors.forEach { (word, color, isRedBorder) ->
                        WordChip(
                            text = word.text,
                            backgroundColor = color,
                            isRedBorder = isRedBorder
                        )
                    }
                }
            }

            // Pagination Controls
            PaginationControls(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevClick = onPrevPage,
                onNextClick = onNextPage
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Play Button
            PlayButton(onClick = onPlayClick)
        }
    }
}

@Composable
fun FilterCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Manually handle click on the Row to toggle checkbox
    Row(
        modifier = modifier
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // Handled by Row click
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.White
            )
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Black // Ensuring contrast on white bg
        )
    }
}

@Composable
fun WordChip(
    text: String,
    backgroundColor: Color,
    isRedBorder: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)) // Chip shape
            .background(backgroundColor)
            .then(if (isRedBorder) Modifier.border(2.dp, Color.Red, RoundedCornerShape(16.dp)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}
