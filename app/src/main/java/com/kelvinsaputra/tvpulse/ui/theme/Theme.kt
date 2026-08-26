package com.kelvinsaputra.tvpulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TVPulseColorScheme = lightColorScheme(
    primary = TvPulseTeal,
    onPrimary = Color.White,
    primaryContainer = TvPulseTealContainer,
    onPrimaryContainer = TvPulseText,
    background = TvPulseBackground,
    onBackground = TvPulseText,
    surface = TvPulseSurface,
    onSurface = TvPulseText,
    surfaceVariant = TvPulseSurfaceVariant,
    onSurfaceVariant = TvPulseTextSecondary,
    error = TvPulseError,
)

@Composable
fun TVPulseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TVPulseColorScheme,
        typography = Typography,
        content = content,
    )
}
