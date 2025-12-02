package com.example.projecttracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Braon nijanse
val BrownDarkest = Color(0xFF3E2723)
val BrownDark = Color(0xFF4E342E)
val BrownPrimary = Color(0xFF6D4C41)
val BrownLight = Color(0xFF8D6E63)
val BrownLighter = Color(0xFFA1887F)
val BrownLightest = Color(0xFFBCAAA4)

// Zlatne nijanse
val GoldDark = Color(0xFFB8860B)
val GoldPrimary = Color(0xFFDAA520)
val GoldLight = Color(0xFFFFD700)
val GoldBright = Color(0xFFFFC107)

// Površine
val SurfaceDark = Color(0xFF1C1410)
val Surface = Color(0xFF2D1E18)
val SurfaceLight = Color(0xFF3E2A20)

// Tekstovi
val TextPrimary = Color(0xFFF5E6D3)
val TextSecondary = Color(0xFFD7C4B0)
val TextDisabled = Color(0xFF9E8B7B)

private val DarkColorScheme = darkColorScheme(
    primary = BrownPrimary,
    secondary = GoldPrimary,
    tertiary = GoldLight,
    background = SurfaceDark,
    surface = Surface,
    surfaceVariant = SurfaceLight,
    onPrimary = TextPrimary,
    onSecondary = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Color(0xFFCF6679),
    outline = BrownLight
)

@Composable
fun ProjectTrackerTheme(
    darkTheme: Boolean = true, // Uvek dark za futuristički look
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
