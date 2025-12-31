package org.nihongo.mochi.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.R
import org.nihongo.mochi.presentation.MochiBackground

@Composable
fun HomeScreen(
    onRecognitionClick: () -> Unit,
    onReadingClick: () -> Unit,
    onWritingClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onResultsClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header Image Container - Transparent and simple Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.mipmap.nihongomochi),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // SECTION 1: MODES DE JEU (Grandes Cartes)
            
            // Recognition Card
            BigModeCard(
                title = stringResource(R.string.menu_recognition),
                subtitle = stringResource(R.string.home_recognition_subtitle),
                kanjiTitle = stringResource(R.string.recognition_title),
                onClick = onRecognitionClick
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Reading Card
            BigModeCard(
                title = stringResource(R.string.menu_reading),
                subtitle = stringResource(R.string.home_reading_subtitle),
                kanjiTitle = stringResource(R.string.reading_title),
                onClick = onReadingClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Writing Card
            BigModeCard(
                title = stringResource(R.string.menu_writing),
                subtitle = stringResource(R.string.home_writing_subtitle),
                kanjiTitle = stringResource(R.string.writing_title),
                onClick = onWritingClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: UTILITAIRES (Grille)
            
            // Row 1: Dictionary & Results
            Row(modifier = Modifier.fillMaxWidth()) {
                SmallUtilityCard(
                    title = stringResource(R.string.menu_dictionary),
                    icon = Icons.Default.Search,
                    onClick = onDictionaryClick,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                
                // Using Star as a placeholder for "sort_by_size" icon
                SmallUtilityCard(
                    title = stringResource(R.string.menu_results),
                    icon = Icons.Default.Star, 
                    onClick = onResultsClick,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Options & About
            Row(modifier = Modifier.fillMaxWidth()) {
                SmallUtilityCard(
                    title = stringResource(R.string.settings_title),
                    icon = Icons.Default.Settings, // ic_menu_manage
                    onClick = onOptionsClick,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                
                SmallUtilityCard(
                    title = stringResource(R.string.menu_about),
                    icon = Icons.Default.Info, 
                    onClick = onAboutClick,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BigModeCard(
    title: String,
    subtitle: String,
    kanjiTitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            
            Text(
                text = kanjiTitle,
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun SmallUtilityCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        // Adding border stroke logic if needed, but CardDefaults handles it via border parameter if we want exact XML replication
        // XML had: app:strokeWidth="1dp", app:strokeColor="?android:attr/textColorHint"
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp).padding(bottom = 8.dp)
            )
            
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
