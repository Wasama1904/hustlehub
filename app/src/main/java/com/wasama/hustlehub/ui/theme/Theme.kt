package com.wasama.hustlehub.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Charcoal = Color(0xFF0F2623)
private val Teal = Color(0xFF2DD4BF)
private val LightBg = Color(0xFFF8FAF8)

private val LightScheme = lightColorScheme(
    primary = Charcoal,
    secondary = Teal,
    background = LightBg,
    surface = Color.White,
    onPrimary = Color.White
)

@Composable
fun HustleHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightScheme, content = content)
}