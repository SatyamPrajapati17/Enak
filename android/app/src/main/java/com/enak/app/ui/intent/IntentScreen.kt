package com.enak.app.ui.intent

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
        val context = LocalContext.current
        Text(
            text = "Curated Learning Videos",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        data class CuratedVideo(
            val title: String,
            val subtitle: String,
            val minutes: Int,
            val url: String,
            val tag: String
        )

        val videos = listOf(
            CuratedVideo(
                title = "Learn AI in 15 Minutes",
                subtitle = "Core fundamentals of AI, neural networks & LLMs",
                minutes = 15,
                url = "https://youtu.be/j5VtrvjA_LM?si=HvlSvP8pvLsOfTta",
                tag = "AI & Tech"
            ),
            CuratedVideo(
                title = "English Learning Session",
                subtitle = "Fluent daily communication & vocabulary mastery",
                minutes = 20,
                url = "https://youtu.be/m-OCMzN4trc?si=yZY060mNBPNYp7pc",
                tag = "Languages"
            ),
            CuratedVideo(
                title = "How to Start Fitness",
                subtitle = "Beginner home workouts, posture & consistent habits",
                minutes = 15,
                url = "https://youtu.be/BGXGdUj93BM?si=ZidRziM-mM0x7VXC",
                tag = "Health"
            ),
            CuratedVideo(
                title = "Web Developer Roadmap",
                subtitle = "Modern full-stack paths & architecture in 2026",
                minutes = 30,
                url = "https://youtu.be/-4e3ewcTupM?si=t1qcLoHrRAjdayNK",
                tag = "Coding"
            )
        )

        videos.forEach { video ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(vibe.primary.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayCircle,
                                contentDescription = null,
                                tint = vibe.accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = video.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurface)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = video.tag, fontSize = 10.sp, color = vibe.accent)
                                }
                            }
                            Text(
                                text = "${video.minutes} min • ${video.subtitle}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: Set as Intention
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, vibe.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedPurpose = "Learn"
                                    selectedDuration = video.minutes
                                    goalText = "Watch: ${video.title}"
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Set as Intention",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = vibe.accent
                            )
                        }

                        // Button 2: Watch on YouTube
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(vibe.primary.copy(alpha = 0.3f))
                                .border(1.dp, vibe.accent, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedPurpose = "Learn"
                                    selectedDuration = video.minutes
                                    goalText = "Watch: ${video.title}"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Watch Video",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
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
