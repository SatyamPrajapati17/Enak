package com.enak.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalVibe = staticCompositionLocalOf { VibeRoyal }

@Composable
fun EnakTheme(
    vibeId: String = "royal",
    content: @Composable () -> Unit
) {
    val vibe = getVibePalette(vibeId)

    val colorScheme = darkColorScheme(
        primary = vibe.primary,
        secondary = vibe.accent,
        tertiary = LightPink,
        background = JetBlack,
        surface = DarkSurface,
        onPrimary = TextPrimary,
        onSecondary = JetBlack,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = JetBlack.toArgb()
            window.navigationBarColor = JetBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalVibe provides vibe) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
