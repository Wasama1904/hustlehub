package com.wasama.hustlehub.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Exact colors from your screenshot
private val TealPrimary = Color(0xFF2DD4BF)      // All button + Completed + Deadline
private val TealSecondary = Color(0xFF20E3B2)
private val CharcoalBg = Color(0xFF0A0A0A)       // Main background - pure dark
private val CardBg = Color(0xFF1E1E1E)           // Kanban cards
private val CardBorder = Color(0xFF2A2A2A)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGrey = Color(0xFF9CA3AF)

private val DarkScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = Color(0xFF0A0A0A),
    primaryContainer = Color(0xFF1A3D38),
    onPrimaryContainer = TealPrimary,
    
    secondary = TealSecondary,
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = TextWhite,
    
    background = CharcoalBg,
    onBackground = TextWhite,
    
    surface = CardBg,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = TextGrey,
    
    error = Color(0xFFEF4444),
    outline = Color(0xFF3A3A3A)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0F2623),
    secondary = TealPrimary,
    background = Color(0xFFF8FAF8),
    surface = Color.White
)

@Composable
fun HustleHubTheme(
    darkTheme: Boolean = true, // FORCE DARK like your screenshot
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        content = content
    )
}