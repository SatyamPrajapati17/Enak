package com.enak.app.ui.session

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.onboarding.PrimaryGradientButton
import com.enak.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SessionScreen(
    viewModel: MainViewModel,
    onNavigateToIntent: () -> Unit = {},
    onFinish: () -> Unit
) {
    val session by viewModel.liveSession.collectAsState()
    val vibe = LocalVibe.current

    if (!session.isActive) {
        NoActiveSessionContent(
            onStartNewSession = onNavigateToIntent,
            onQuickStart = { purpose, mins, goal ->
                viewModel.startSession(purpose, mins, goal)
            }
        )
        return
    }

    var showNegotiationDialog by remember { mutableStateOf(false) }
    var negotiationReason by remember { mutableStateOf("") }
    var negotiationFeedback by remember { mutableStateOf<String?>(null) }

    val totalPlannedSecs = (session.plannedMinutes * 60).coerceAtLeast(1)
    val progress = (session.elapsedSeconds.toFloat() / totalPlannedSecs.toFloat()).coerceAtMost(1f)

    val minutes = session.elapsedSeconds / 60
    val seconds = session.elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val plannedTime = String.format("%02d:00", session.plannedMinutes)

    if (session.isBreathingActive) {
        BreathingExerciseView(onDismiss = { viewModel.toggleBreathing(false) })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SuccessGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "● Live Session",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = session.purpose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = vibe.accent
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Dual Progress Ring
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background track
                    drawCircle(
                        color = Color(0x1AFFFFFF),
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = vibe.primary,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "of $plannedTime planned",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    if (session.warningCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Warning ${session.warningCount}/2: Soft Drift",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftPink
                        )
                    }
                }
            }

            if (session.goal.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Anchor Intent:", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = session.goal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action row: Breather + 5 more min
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleBreathing(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Icon(imageVector = Icons.Outlined.Spa, contentDescription = null, tint = vibe.accent)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Breather", color = TextPrimary, fontSize = 13.sp)
                }

                Button(
                    onClick = { showNegotiationDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Icon(imageVector = Icons.Outlined.HourglassEmpty, contentDescription = null, tint = vibe.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+5 Min", color = TextPrimary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryGradientButton(
                text = "Finish Session",
                onClick = {
                    viewModel.endSession()
                    onFinish()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Negotiation Dialog
        if (showNegotiationDialog) {
            AlertDialog(
                onDismissRequest = { showNegotiationDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text("Ask for 5 More Minutes", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "State your specific reason. Enak's coach accepts conscious, on-purpose extensions.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = negotiationReason,
                            onValueChange = { negotiationReason = it },
                            placeholder = { Text("e.g. Wrapping up problem 4", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = vibe.primary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        negotiationFeedback?.let { feedback ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = feedback, fontSize = 12.sp, color = vibe.accent)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (negotiationReason.trim().length > 8 && !negotiationReason.contains("just", ignoreCase = true)) {
                                negotiationFeedback = "ACCEPT: Purpose verified."
                                viewModel.extendSession(5)
                                showNegotiationDialog = false
                            } else {
                                negotiationFeedback = "CHALLENGE: Sounds automatic. Take a pause?"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary)
                    ) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNegotiationDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun BreathingExerciseView(onDismiss: () -> Unit) {
    val vibe = LocalVibe.current
    var phase by remember { mutableStateOf("Inhale (4s)") }
    var secondsInPhase by remember { mutableIntStateOf(4) }

    LaunchedEffect(Unit) {
        while (true) {
            phase = "Inhale"
            for (i in 4 downTo 1) {
                secondsInPhase = i
                delay(1000)
            }
            phase = "Hold"
            for (i in 4 downTo 1) {
                secondsInPhase = i
                delay(1000)
            }
            phase = "Exhale"
            for (i in 4 downTo 1) {
                secondsInPhase = i
                delay(1000)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Take a Mindful Breather",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Center your nervous system",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 48.dp)
            )

            Box(
                modifier = Modifier
                    .size((180 * scale).dp)
                    .clip(CircleShape)
                    .background(vibe.primary.copy(alpha = 0.25f))
                    .border(2.dp, vibe.accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = phase,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$secondsInPhase",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = vibe.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Text("Return to Session", color = TextPrimary)
            }
        }
    }
}

@Composable
fun NoActiveSessionContent(
    onStartNewSession: () -> Unit,
    onQuickStart: (purpose: String, mins: Int, goal: String) -> Unit
) {
    val vibe = LocalVibe.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "○ Idle",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(vibe.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Mindful Shield Ready",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = vibe.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Center card with peaceful mindful visual
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(vibe.primary.copy(alpha = 0.15f))
                .border(2.dp, vibe.accent.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Spa,
                contentDescription = null,
                tint = vibe.accent,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Active Session",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Define an intentional focus block before opening distracting apps. We'll hold the boundary so you can focus deeply.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryGradientButton(
            text = "Start New Session",
            onClick = onStartNewSession
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Launch Intention Presets
        Text(
            text = "Quick Launch Intentions",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        val presets = listOf(
            Triple("15m • Learn & Study", "Learn", 15),
            Triple("30m • Deep Focus Block", "Work", 30),
            Triple("45m • Coding / Project", "Work", 45),
            Triple("5m • Mindful Breathing Reset", "Relax", 5)
        )

        presets.forEach { (label, purpose, mins) ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onQuickStart(purpose, mins, label)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = vibe.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Start",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = vibe.accent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Today's stats mini card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Completed", fontSize = 11.sp, color = TextMuted)
                    Text(text = "3 sessions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "On Purpose", fontSize = 11.sp, color = TextMuted)
                    Text(text = "48 min", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = vibe.accent)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Calm Resets", fontSize = 11.sp, color = TextMuted)
                    Text(text = "2 taken", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
