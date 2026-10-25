package com.enak.app.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.InstalledAppItem
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.theme.*

@Composable
fun FocusScreen(
    viewModel: MainViewModel,
    onOpenPermissionsCenter: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsState()
    val apps by viewModel.installedApps.collectAsState()
    val vibe = LocalVibe.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Digital Wellbeing",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Per-app daily boundaries & timers",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onOpenPermissionsCenter) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = "Permissions",
                    tint = vibe.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Distraction Blocker Master Switch Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (settings.focusModeActive) vibe.accent else GlassBorder
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Distraction Blocker",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (settings.focusModeActive) SuccessGreen.copy(alpha = 0.2f) else DarkSurface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (settings.focusModeActive) "ACTIVE" else "OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (settings.focusModeActive) SuccessGreen else TextMuted
                            )
                        }
                    }
                    Text(
                        text = "Intercepts distracting apps (Instagram, YouTube, X, Reddit, etc.) with mindful overlay until turned off.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = settings.focusModeActive,
                    onCheckedChange = { viewModel.toggleFocusMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = vibe.accent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Strict Mode Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (settings.strictModeEnabled) vibe.primary else GlassBorder
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Strict Mode",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Prevents turning off timers or force-quitting Enak during sessions.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = settings.strictModeEnabled,
                    onCheckedChange = { viewModel.setStrictMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = vibe.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Permissions Center Shortcut Banner
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenPermissionsCenter() }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = null,
                    tint = vibe.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Permissions Center",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Usage access, overlays, and battery status",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Tracked Applications",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(apps) { app ->
                AppLimitItemCard(
                    app = app,
                    onLimitChange = { newLimit ->
                        viewModel.updateAppLimit(app.packageName, newLimit)
                    }
                )
            }
        }
    }
}

@Composable
fun AppLimitItemCard(
    app: InstalledAppItem,
    onLimitChange: (Int) -> Unit
) {
    val vibe = LocalVibe.current
    val isNearLimit = app.usedMinutes >= (app.dailyLimitMinutes * 0.8)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isNearLimit) SoftPink.copy(alpha = 0.5f) else GlassBorder
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = vibe.primary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.appName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${app.usedMinutes} min used today",
                            fontSize = 12.sp,
                            color = if (isNearLimit) SoftPink else TextSecondary
                        )
                    }
                }

                Text(
                    text = "${app.dailyLimitMinutes}m limit",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = vibe.accent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Limit Quick Selector Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Limit: ", fontSize = 11.sp, color = TextMuted)
                val limits = listOf(15, 30, 45, 60)
                limits.forEach { limit ->
                    val isSel = app.dailyLimitMinutes == limit
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) vibe.primary else DarkSurface)
                            .clickable { onLimitChange(limit) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${limit}m",
                            fontSize = 11.sp,
                            color = if (isSel) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
