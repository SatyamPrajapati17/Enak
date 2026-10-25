package com.enak.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun EnakTheme(
    vibeId: String = "royal",
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val vibe = getVibePalette(vibeId)
    val enakColors = if (isDarkTheme) DarkEnakColors else LightEnakColors

    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = vibe.primary,
            secondary = vibe.accent,
            tertiary = LightPink,
            background = enakColors.background,
            surface = enakColors.surface,
            onPrimary = enakColors.textPrimary,
            onSecondary = Color.Black,
            onBackground = enakColors.textPrimary,
            onSurface = enakColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = vibe.primary,
            secondary = vibe.accent,
            tertiary = SoftPink,
            background = enakColors.background,
            surface = enakColors.surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = enakColors.textPrimary,
            onSurface = enakColors.textPrimary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = enakColors.background.toArgb()
            window.navigationBarColor = enakColors.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    CompositionLocalProvider(
        LocalVibe provides vibe,
        LocalEnakColors provides enakColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
