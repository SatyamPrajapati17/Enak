package com.enak.app.ui.today

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.onboarding.PrimaryGradientButton
import com.enak.app.ui.theme.*
import java.util.Calendar

@Composable
fun TodayScreen(
    viewModel: MainViewModel,
    onStartIntent: () -> Unit,
    onOpenFocus: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsState()
    val vibe = LocalVibe.current

    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11 -> "Good morning"
        hour in 12..16 -> "Good afternoon"
        hour in 17..21 -> "Good evening"
        else -> "Quiet night"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Greeting Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$greeting, ${settings.name}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Your mind, today",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            // Streak Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = WarningAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "5 days",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Intentionality Score Hero Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = vibe.primary.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Intentionality Score",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "88 / 100",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = vibe.accent
                    )
                    Text(
                        text = "High clarity • 12% drift detected",
                        fontSize = 12.sp,
                        color = SuccessGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(vibe.primary.copy(alpha = 0.15f))
                        .border(3.dp, vibe.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = vibe.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stat Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "On purpose",
                value = "3 of 4",
                subtitle = "sessions",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Time aligned",
                value = "48 min",
                subtitle = "today",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Calm resets",
                value = "2",
                subtitle = "taken",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Focus Mode Switcher Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (settings.focusModeActive) vibe.primary else Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = if (settings.focusModeActive) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Focus Shield Mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (settings.focusModeActive) "Active: Distracting apps blocked" else "Off: Normal monitoring",
                            fontSize = 12.sp,
                            color = if (settings.focusModeActive) vibe.accent else TextMuted
                        )
                    }
                }

                Switch(
                    checked = settings.focusModeActive,
                    onCheckedChange = { viewModel.toggleFocusMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = vibe.primary,
                        uncheckedTrackColor = DarkSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Theme Vibe Picker ("Theme changing as per vibe")
        Text(
            text = "App Theme Vibe",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val vibes = listOf(
                "royal" to "Royal Amethyst",
                "cyan" to "Cyan Clarity",
                "emerald" to "Emerald Zen",
                "sunset" to "Sunset Rose"
            )
            vibes.forEach { (id, label) ->
                val selected = settings.vibe == id
                val palette = getVibePalette(id)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) palette.primary.copy(alpha = 0.25f) else DarkSurface)
                        .border(
                            1.5.dp,
                            if (selected) palette.primary else GlassBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.setVibe(id) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(palette.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label.split(" ")[0],
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start With An Intention Section
        Text(
            text = "Start with an intention",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        val purposes = listOf("Learn", "Messages", "Relax", "Explore", "Work")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            purposes.take(4).forEach { purpose ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .clickable { onStartIntent() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = purpose, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryGradientButton(
            text = "New Intentional Session",
            onClick = onStartIntent
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val vibe = LocalVibe.current
    GlassCard(modifier = modifier) {
        Text(text = title, fontSize = 11.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = vibe.primary
        )
        Text(text = subtitle, fontSize = 10.sp, color = TextSecondary)
    }
}
