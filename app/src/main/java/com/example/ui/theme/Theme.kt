package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CosmicAccentEmerald,
    onPrimary = Color(0xFF071B0B),
    primaryContainer = CosmicPurpleMain,
    onPrimaryContainer = Color.White,
    secondary = CosmicAccentTeal,
    onSecondary = Color(0xFF01141A),
    background = CosmicDeepSpace,
    onBackground = Color(0xFFECEFF1),
    surface = CosmicSlateDark,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = CosmicSlateMedium,
    onSurfaceVariant = Color(0xFFCFD8DC),
    outline = Color(0xFF455A64)
)

private val LightColorScheme = lightColorScheme(
    primary = CosmicPurpleMain,
    onPrimary = Color.White,
    primaryContainer = CosmicAccentEmerald,
    onPrimaryContainer = Color(0xFF071B0B),
    secondary = CosmicAccentTeal,
    onSecondary = Color(0xFF01141A),
    background = LightPlatinum,
    onBackground = Color(0xFF263238),
    surface = LightSurface,
    onSurface = Color(0xFF263238),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF37474F),
    outline = Color(0xFFB0BEC5)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
