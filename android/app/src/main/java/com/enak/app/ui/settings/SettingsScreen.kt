package com.enak.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.theme.*
import com.enak.app.util.PermissionUtils

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onResetOnboarding: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsState()
    val vibe = LocalVibe.current
    val colors = LocalEnakColors.current
    val context = LocalContext.current

    var editName by remember(settings.name) { mutableStateOf(settings.name) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Settings & Preferences",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            text = "Customize theme, permissions connection & data",
            fontSize = 12.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // 1. Appearance (Theme Mode: Dark, Light, System)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Appearance Theme",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = "Switch between Dark and Light mode, or follow Android system default",
                fontSize = 11.sp,
                color = colors.textMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            val themeOptions = listOf(
                Triple("dark", "🌙 Dark", "Deep contrast"),
                Triple("light", "☀️ Light", "Clean white"),
                Triple("system", "⚙️ System", "Auto match")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { (mode, title, _) ->
                    val selected = settings.themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) vibe.primary else colors.cardSurface)
                            .border(1.dp, if (selected) vibe.accent else colors.border, RoundedCornerShape(14.dp))
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else colors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Theme Vibe Color Accent
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Color Accent Vibe", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            val vibes = listOf("royal" to "Royal", "cyan" to "Cyan", "emerald" to "Emerald", "sunset" to "Sunset")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vibes.forEach { (id, label) ->
                    val selected = settings.vibe == id
                    val p = getVibePalette(id)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) p.primary.copy(alpha = 0.35f) else colors.cardSurface)
                            .border(1.5.dp, if (selected) p.primary else colors.border, RoundedCornerShape(14.dp))
                            .clickable { viewModel.setVibe(id) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) (if (colors.isDark) Color.White else p.primary) else colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Device Permissions & Restricted Settings Connection
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = vibe.accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Device Permissions & Connection",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "If Accessibility or Notification Listener shows 'Restricted setting' or toggle disabled on Android 13+:\n1. Tap 'Open Device App Info' below\n2. Tap the ⋮ three-dot menu in the top-right corner\n3. Tap 'Allow restricted settings'\n4. Return and enable the permissions",
                fontSize = 12.sp,
                color = colors.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    try {
                        context.startActivity(PermissionUtils.getAppSettingsIntent(context))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = vibe.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.SettingsApplications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Device App Info (Restricted Settings)", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Profile Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Your Profile", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = editName,
                onValueChange = {
                    editName = it
                    viewModel.setUserName(it)
                },
                label = { Text("Display Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = vibe.primary,
                    unfocusedBorderColor = colors.border
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Coach Persona
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Coach Persona", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            val personas = listOf("calm" to "Calm Zen", "strict" to "Strict", "friendly" to "Friendly", "monk" to "Monk")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                personas.forEach { (id, label) ->
                    val selected = settings.personality == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) vibe.primary else colors.cardSurface)
                            .border(1.dp, if (selected) vibe.accent else colors.border, RoundedCornerShape(14.dp))
                            .clickable { viewModel.setPersonality(id) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else colors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Data Management (Export & Reset from Scratch)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showExportDialog = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.Download, contentDescription = null, tint = vibe.accent)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Export Data (JSON)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Text(text = "Download local session history and usage records", fontSize = 11.sp, color = colors.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetDialog = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.DeleteForever, contentDescription = null, tint = SoftPink)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Start From Scratch (Reset All Data)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SoftPink)
                    Text(text = "Wipe all stored data and re-run onboarding questionnaire", fontSize = 11.sp, color = colors.textMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Privacy Guarantee Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Zero Cloud Transmission", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Enak runs 100% on your device. App package names are inspected solely to compare against your limits. Automatic cloud backups have been disabled so re-installing always starts completely fresh.",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                containerColor = colors.surface,
                title = { Text("Export Successful", color = colors.textPrimary) },
                text = {
                    Text(
                        "Your session logs, bandit weights, and intentionality scores have been compiled into local JSON format.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showExportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary)
                    ) {
                        Text("Done")
                    }
                }
            )
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                containerColor = colors.surface,
                title = { Text("Reset All Data?", color = colors.textPrimary) },
                text = {
                    Text(
                        "This will wipe all session history, plant garden progress, and preferences, allowing you to start fresh from scratch.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetDialog = false
                            viewModel.resetAllData {
                                onResetOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPink)
                    ) {
                        Text("Reset & Start Scratch", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                }
            )
        }
    }
}
