package com.medly.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4D6B5A),
    secondary = Color(0xFF8A6A45),
    background = Color(0xFFF7F9F5),
    surface = Color(0xFFF7F9F5)
)

@Composable
fun MedlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = Typography(), content = content)
}
