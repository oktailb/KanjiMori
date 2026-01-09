package org.nihongo.mochi.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.home.BigModeCard

@Composable
fun GamesScreen(
    onBackClick: () -> Unit,
    onTaquinClick: () -> Unit,
    onSimonClick: () -> Unit,
    onTetrisClick: () -> Unit,
    onCrosswordsClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "GAMES",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )

            BigModeCard(
                title = "Taquin",
                subtitle = "Remettez le tableau des Kanas dans l'ordre",
                kanjiTitle = "パズル",
                onClick = onTaquinClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = "Simon Says",
                subtitle = "Mémorisez et répétez la séquence de sons",
                kanjiTitle = "記憶",
                onClick = onSimonClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = "Tetris Kanji",
                subtitle = "Assemblez les composants pour former des Kanjis",
                kanjiTitle = "テトリス",
                onClick = onTetrisClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BigModeCard(
                title = "Crosswords",
                subtitle = "Mots croisés thématiques par niveau JLPT",
                kanjiTitle = "十字語",
                onClick = onCrosswordsClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
