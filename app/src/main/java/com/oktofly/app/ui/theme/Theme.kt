package com.oktofly.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A5F),
    secondary = Color(0xFF42A5F5),
    onSecondary = Color.White,
    background = Color(0xFF0A1628),
    surface = Color(0xFF0D2137),
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFEF5350),
)

@Composable
fun OktoFlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
