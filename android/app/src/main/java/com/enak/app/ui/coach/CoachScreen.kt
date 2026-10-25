package com.enak.app.ui.coach

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.VolumeUp
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
import com.enak.app.ui.CoachMessage
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.theme.*
import java.util.Locale

@Composable
fun CoachScreen(viewModel: MainViewModel) {
    val messages by viewModel.coachMessages.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val vibe = LocalVibe.current
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(context) {
        val t = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = t
        onDispose {
            t.stop()
            t.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mindful AI Coach",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Grounded in non-judgmental awareness",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = settings.personality.replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = vibe.accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Prompts Chips
        val prompts = listOf(
            "Why do I overrun?",
            "Plan my evening",
            "Resist doomscrolling"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.sendCoachMessage(prompt) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = prompt, fontSize = 11.sp, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isUser) vibe.primary else DarkSurface)
                            .border(1.dp, if (isUser) Color.Transparent else GlassBorder, RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = if (isUser) Color.White else TextPrimary
                            )
                            if (!isUser) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.VolumeUp,
                                        contentDescription = "Speak",
                                        tint = vibe.accent,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, null)
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask your coach...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = vibe.primary,
                    unfocusedBorderColor = GlassBorder
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendCoachMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(vibe.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
