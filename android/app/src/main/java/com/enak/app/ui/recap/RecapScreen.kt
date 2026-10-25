package com.enak.app.ui.recap

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.onboarding.PrimaryGradientButton
import com.enak.app.ui.theme.*
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float
)

@Composable
fun RecapScreen(
    viewModel: MainViewModel,
    onDone: () -> Unit
) {
    val vibe = LocalVibe.current
    var regretScore by remember { mutableFloatStateOf(1f) }
    var goalAchieved by remember { mutableStateOf(true) }
    var journalText by remember { mutableStateOf("") }

    // Canvas Confetti Particles
    val particles = remember {
        List(100) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = 0.85f,
                speedX = (Random.nextFloat() - 0.5f) * 1.2f,
                speedY = -(Random.nextFloat() * 1.5f + 0.8f),
                color = if (Random.nextBoolean()) vibe.primary else vibe.accent,
                size = Random.nextFloat() * 14f + 6f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = SuccessGreen,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Session Complete!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Every conscious session rewires your digital habit.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Sentiment Badge
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(vibe.accent, RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Sentiment: Grounded & Intentional",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = vibe.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Achieved Toggle
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Did you fulfill your purpose?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (goalAchieved) "Yes, remained aligned" else "Drifted slightly",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = goalAchieved,
                        onCheckedChange = { goalAchieved = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = vibe.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Regret Slider
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Regret Level: ${regretScore.toInt()} / 5",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = when (regretScore.toInt()) {
                        1 -> "Zero regret — deeply satisfying."
                        2 -> "Good session — minor distraction."
                        3 -> "Moderate — lost some focus."
                        else -> "High drift — let's learn from it."
                    },
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
                Slider(
                    value = regretScore,
                    onValueChange = { regretScore = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = vibe.primary,
                        activeTrackColor = vibe.primary,
                        inactiveTrackColor = DarkSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reflection Journal
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Reflective note (optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = journalText,
                    onValueChange = { journalText = it },
                    placeholder = { Text("What did you notice about your focus?", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = vibe.primary,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryGradientButton(
                text = "Save & Return to Dashboard",
                onClick = onDone
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Confetti Canvas Overlay (dismisses smoothly after animation)
        if (animProgress.value < 0.98f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val t = animProgress.value
                val w = size.width
                val h = size.height
                particles.forEach { p ->
                    val curX = (p.x * w) + (p.speedX * t * 350f)
                    val curY = (p.y * h) + (p.speedY * t * 700f) + (0.5f * 980f * t * t)
                    if (curY in 0f..h && curX in 0f..w) {
                        drawRect(
                            color = p.color.copy(alpha = (1f - t).coerceIn(0f, 1f)),
                            topLeft = Offset(curX, curY),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    }
                }
            }
        }
    }
}
