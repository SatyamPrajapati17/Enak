package com.enak.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val JetBlack = Color(0xFF0B0B10)
val DarkSurface = Color(0xFF13131D)
val CardSurface = Color(0xFF1B1B26)
val GlassBorder = Color(0x26FFFFFF)

val RoyalAmethyst = Color(0xFF8A2BE2)
val ElectricCyan = Color(0xFF22E3F0)
val SoftPink = Color(0xFFF43F5E)
val LightPink = Color(0xFFFB7185)

val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFFA1A1AA)
val TextMuted = Color(0xFF71717A)

val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)

data class EnakColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardSurface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

val DarkEnakColors = EnakColors(
    isDark = true,
    background = Color(0xFF0B0B10),
    surface = Color(0xFF13131D),
    cardSurface = Color(0xFF1B1B26),
    border = Color(0x26FFFFFF),
    textPrimary = Color(0xFFF3F4F6),
    textSecondary = Color(0xFFA1A1AA),
    textMuted = Color(0xFF71717A)
)

val LightEnakColors = EnakColors(
    isDark = false,
    background = Color(0xFFF6F8FC),
    surface = Color(0xFFFFFFFF),
    cardSurface = Color(0xFFECEFF5),
    border = Color(0x1F000000),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8)
)

val LocalEnakColors = staticCompositionLocalOf { DarkEnakColors }

data class VibePalette(
    val name: String,
    val id: String,
    val primary: Color,
    val accent: Color,
    val gradient: List<Color>
)

val VibeRoyal = VibePalette(
    name = "Royal Amethyst",
    id = "royal",
    primary = RoyalAmethyst,
    accent = ElectricCyan,
    gradient = listOf(RoyalAmethyst, ElectricCyan)
)

val VibeCyan = VibePalette(
    name = "Electric Clarity",
    id = "cyan",
    primary = ElectricCyan,
    accent = RoyalAmethyst,
    gradient = listOf(ElectricCyan, RoyalAmethyst)
)

val VibeEmerald = VibePalette(
    name = "Emerald Zen",
    id = "emerald",
    primary = Color(0xFF10B981),
    accent = Color(0xFF06D6A0),
    gradient = listOf(Color(0xFF10B981), Color(0xFF06D6A0))
)

val VibeSunset = VibePalette(
    name = "Sunset Twilight",
    id = "sunset",
    primary = Color(0xFFF43F5E),
    accent = Color(0xFFF59E0B),
    gradient = listOf(Color(0xFFF43F5E), Color(0xFFF59E0B))
)

val LocalVibe = staticCompositionLocalOf { VibeRoyal }

fun getVibePalette(id: String): VibePalette = when (id) {
    "cyan" -> VibeCyan
    "emerald" -> VibeEmerald
    "sunset" -> VibeSunset
    else -> VibeRoyal
}
