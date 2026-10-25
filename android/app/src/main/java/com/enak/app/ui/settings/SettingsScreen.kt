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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onResetOnboarding: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsState()
    val vibe = LocalVibe.current

    var editName by remember(settings.name) { mutableStateOf(settings.name) }
    var showExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Settings & Privacy",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Local-first attention protection preferences",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // Profile Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Your Profile", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = editName,
                onValueChange = {
                    editName = it
                    viewModel.setUserName(it)
                },
                label = { Text("Display Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = vibe.primary,
                    unfocusedBorderColor = GlassBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Coach Persona
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Coach Persona", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
                            .background(if (selected) vibe.primary else DarkSurface)
                            .border(1.dp, if (selected) vibe.accent else GlassBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.setPersonality(id) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme Vibe
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Theme Vibe", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
                            .background(if (selected) p.primary.copy(alpha = 0.3f) else DarkSurface)
                            .border(1.5.dp, if (selected) p.primary else GlassBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.setVibe(id) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data Management (JSON Export)
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
                    Text(text = "Export Data (JSON)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = "Download local session history and usage records", fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResetOnboarding() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, tint = SoftPink)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Reset Onboarding", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SoftPink)
                    Text(text = "Reconfigure goals, questionnaire, and permissions", fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Policy Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Zero Data Cloud Transmission", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Enak runs 100% on your device. App package names are inspected solely to compare against your daily limits. No screen contents, keystrokes, personal chats, or audio ever leave your phone.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                containerColor = DarkSurface,
                title = { Text("Export Successful", color = TextPrimary) },
                text = {
                    Text(
                        "Your session logs, bandit weights, and intentionality scores have been compiled into local JSON format.",
                        color = TextSecondary,
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
    }
}
