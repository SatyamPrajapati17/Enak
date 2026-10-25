package com.enak.app.ui.intent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.onboarding.PrimaryGradientButton
import com.enak.app.ui.theme.*

@Composable
fun IntentScreen(
    viewModel: MainViewModel,
    onBegin: () -> Unit
) {
    var selectedPurpose by remember { mutableStateOf("Learn") }
    var selectedDuration by remember { mutableIntStateOf(15) }
    var goalText by remember { mutableStateOf("") }
    val vibe = LocalVibe.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "What's your intention?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Name it now. We'll only check in if the session drifts.",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Purpose Selection
        Text(
            text = "Purpose",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        val purposes = listOf("Learn", "Work", "Messages", "Relax", "Explore")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            purposes.forEach { purpose ->
                val selected = selectedPurpose == purpose
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) vibe.primary else DarkSurface)
                        .border(1.dp, if (selected) vibe.accent else GlassBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedPurpose = purpose }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = purpose,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Duration Selection
        Text(
            text = "Duration",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        val durations = listOf(5, 15, 30, 60)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durations.forEach { dur ->
                val selected = selectedDuration == dur
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) vibe.primary else DarkSurface)
                        .border(1.dp, if (selected) vibe.accent else GlassBorder, RoundedCornerShape(16.dp))
                        .clickable { selectedDuration = dur }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dur m",
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Desired Outcome Field
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Desired outcome (optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it },
                placeholder = { Text("e.g. Read chapter 3 or finish code review", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = vibe.primary,
                    unfocusedBorderColor = GlassBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Learning Videos Picker (Curated mindful learning)
        Text(
            text = "Curated Learning Videos",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        val videos = listOf(
            "AI in 15 Minutes" to "Essential principles of deep learning",
            "English Communication" to "Mindful fluency and expression",
            "Web Developer Roadmap" to "Full stack systems in 2026"
        )
        videos.forEach { (title, subtitle) ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        goalText = "Watch: $title"
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = vibe.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryGradientButton(
            text = "Begin Mindfully",
            onClick = {
                viewModel.startSession(
                    purpose = selectedPurpose,
                    plannedMin = selectedDuration,
                    goal = goalText.ifBlank { "Mindful $selectedPurpose" }
                )
                onBegin()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Exit any time. Every nudge can be snoozed or switched off.",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
