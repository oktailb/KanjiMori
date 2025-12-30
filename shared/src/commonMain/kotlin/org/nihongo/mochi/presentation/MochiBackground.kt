package org.nihongo.mochi.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.background_day
import org.nihongo.mochi.shared.generated.resources.background_night

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MochiBackground(
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) Res.drawable.background_night else Res.drawable.background_day

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        content()
    }
}
