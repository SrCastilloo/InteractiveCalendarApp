package com.danielcastillo.calendariointeractivo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF101828)
val Lagoon = Color(0xFF10B7A7)
val Coral = Color(0xFFFF6B6B)
val Gold = Color(0xFFFFBF47)
val Iris = Color(0xFF6672FF)
val Mint = Color(0xFFDFFBF4)
val Cloud = Color(0xFFF7FAFC)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = Lagoon,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Ink,
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE7E5),
    tertiary = Gold,
    onTertiary = Ink,
    background = Cloud,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9EEF5),
    onSurfaceVariant = Color(0xFF475467),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFB42318)
)

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF47E1D1),
    onPrimary = Ink,
    primaryContainer = Color(0xFF0C5F58),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFF8F88),
    onSecondary = Ink,
    tertiary = Gold,
    onTertiary = Ink,
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFEFF6FF),
    surface = Color(0xFF121B2D),
    onSurface = Color(0xFFEFF6FF),
    surfaceVariant = Color(0xFF263244),
    onSurfaceVariant = Color(0xFFB8C4D8),
    outline = Color(0xFF48566C)
)

@Composable
fun CalendarioInteractivoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
