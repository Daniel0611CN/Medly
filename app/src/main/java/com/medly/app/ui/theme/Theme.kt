package com.medly.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF2D8C82),
    background = Color(0xFFF3F7F5),
    surface = Color(0xFFF3F7F5),
    onBackground = Color(0xFF0F2E2A),
    onSurface = Color(0xFF0F2E2A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF75DED0),
    onPrimary = Color(0xFF003731),
    secondary = Color(0xFFA8D8CF),
    background = Color(0xFF0F2E2A),
    surface = Color(0xFF163A35),
    onBackground = Color(0xFFE2F3EF),
    onSurface = Color(0xFFE2F3EF)
)

@Composable
fun MedlyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
