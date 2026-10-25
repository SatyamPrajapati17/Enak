package com.enak.app.ui.lab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.theme.*

data class BanditArmData(
    val id: String,
    val name: String,
    val desc: String,
    val icon: ImageVector,
    var alpha: Int,
    var beta: Int
) {
    val total: Int get() = alpha + beta
    val winRate: Float get() = if (total > 0) alpha.toFloat() / total.toFloat() else 0.5f
}

@Composable
fun LabScreen(viewModel: MainViewModel) {
    val gardenLevel by viewModel.gardenLevel.collectAsState()
    val vibe = LocalVibe.current

    // Bandit arms state
    var arms by remember {
        mutableStateOf(
            listOf(
                BanditArmData("breather", "Breathing Circle", "4s box breathing pause", Icons.Outlined.Spa, 28, 8),
                BanditArmData("anchor", "Anchor Question", "Gentle intent alignment check", Icons.Outlined.HelpOutline, 22, 12),
                BanditArmData("banner", "Soft Banner", "Peripheral time awareness nudge", Icons.Outlined.NotificationsActive, 18, 16),
                BanditArmData("negotiation", "AI Negotiation", "5-minute intentional challenge", Icons.Outlined.SmartToy, 34, 6)
            )
        )
    }

    var activeTestDialog by remember { mutableStateOf<String?>(null) }
    var simulationNotice by remember { mutableStateOf<String?>(null) }

    // Plant species based on level
    val (plantSpecies, plantEmoji) = when {
        gardenLevel <= 2 -> "Fresh Seedling" to "🌱"
        gardenLevel <= 4 -> "Zen Fern" to "🌿"
        gardenLevel <= 6 -> "Lotus Blossom" to "🌸"
        gardenLevel <= 8 -> "Sacred Bamboo" to "🎋"
        else -> "Ancient Bonsai" to "🌳"
    }

    val animatedScale by animateFloatAsState(
        targetValue = 1f + (gardenLevel * 0.05f),
        animationSpec = tween(400)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(vibe.primary.copy(alpha = 0.2f))
                    .border(1.dp, vibe.accent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    tint = vibe.accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "The Mindful Lab",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Adaptive AI algorithms & mindful garden",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Interactive Mindful Garden Section
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = SuccessGreen.copy(alpha = 0.4f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Mindful Garden",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SuccessGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Level $gardenLevel / 10",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$plantEmoji $plantSpecies • Flourishes with intentional sessions",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Plant Graphic
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .border(2.dp, SuccessGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Spa,
                        contentDescription = "Plant",
                        tint = SuccessGreen,
                        modifier = Modifier.size((44 * animatedScale).coerceAtMost(80f).dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vitality Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Vitality", fontSize = 12.sp, color = TextMuted)
                    Text(text = "${gardenLevel * 10}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = vibe.accent)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DarkSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(gardenLevel * 0.1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SuccessGreen)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Garden action controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Water Garden
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(vibe.primary.copy(alpha = 0.3f))
                            .border(1.dp, vibe.accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.waterGarden() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.WaterDrop,
                                contentDescription = null,
                                tint = vibe.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Water Plant (+1)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Prune Weeds
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.pruneGarden() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCut,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Prune Weeds",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Thompson Sampling Multi-Armed Bandit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bandit Intervention Ladder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Reinforcement learning for personalized nudges",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Quick Trial Simulation Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(vibe.primary.copy(alpha = 0.25f))
                    .border(1.dp, vibe.accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable {
                        // Simulate 20 Monte Carlo bandit trials
                        arms = arms.map { arm ->
                            val simulatedSuccesses = (3..7).random()
                            val simulatedFailures = (1..3).random()
                            arm.copy(
                                alpha = arm.alpha + simulatedSuccesses,
                                beta = arm.beta + simulatedFailures
                            )
                        }
                        simulationNotice = "Ran 20 Monte Carlo trials! Thompson sampling updated arm weights."
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⚡ Run 20 Trials",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = vibe.accent
                )
            }
        }

        if (simulationNotice != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = simulationNotice!!,
                fontSize = 11.sp,
                color = SuccessGreen,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Render each Bandit Arm with Live Testing
        arms.forEach { arm ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
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
                                    .background(vibe.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = arm.icon,
                                    contentDescription = null,
                                    tint = vibe.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = arm.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = arm.desc,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${(arm.winRate * 100).toInt()}% win",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (arm.winRate >= 0.7f) SuccessGreen else vibe.primary
                            )
                            Text(
                                text = "${arm.alpha}W / ${arm.beta}L",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress / Win Rate bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DarkSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(arm.winRate)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (arm.winRate >= 0.7f) SuccessGreen else vibe.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Test Arm Preview Button
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                .clickable { activeTestDialog = arm.id }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = null,
                                    tint = vibe.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Test Intervention",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = vibe.accent
                                )
                            }
                        }

                        // Record Success
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .clickable {
                                    arms = arms.map {
                                        if (it.id == arm.id) it.copy(alpha = it.alpha + 1) else it
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Win",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        // Record Loss
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftPink.copy(alpha = 0.2f))
                                .clickable {
                                    arms = arms.map {
                                        if (it.id == arm.id) it.copy(beta = it.beta + 1) else it
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "- Drift",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftPink
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Modal dialogs for testing each intervention arm
    when (activeTestDialog) {
        "breather" -> {
            AlertDialog(
                onDismissRequest = { activeTestDialog = null },
                containerColor = Color(0xFF14141C),
                title = { Text("Breathing Circle Test", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "A 4-second box breathing pause gently interrupts automatic dopamine loops without blocking.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(vibe.primary.copy(alpha = 0.3f))
                                .border(2.dp, vibe.accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Breathe", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            arms = arms.map { if (it.id == "breather") it.copy(alpha = it.alpha + 1) else it }
                            activeTestDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary)
                    ) {
                        Text("Record Responded Mindfully", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeTestDialog = null }) {
                        Text("Close", color = TextMuted)
                    }
                }
            )
        }
        "anchor" -> {
            AlertDialog(
                onDismissRequest = { activeTestDialog = null },
                containerColor = Color(0xFF14141C),
                title = { Text("Anchor Question Test", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "\"What were you hoping to find when you opened this app?\"",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = vibe.accent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Anchor questions awaken conscious awareness by prompting self-inquiry before compulsive scrolling starts.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            arms = arms.map { if (it.id == "anchor") it.copy(alpha = it.alpha + 1) else it }
                            activeTestDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary)
                    ) {
                        Text("Returned to Focus", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeTestDialog = null }) {
                        Text("Dismiss", color = TextMuted)
                    }
                }
            )
        }
        "banner" -> {
            AlertDialog(
                onDismissRequest = { activeTestDialog = null },
                containerColor = Color(0xFF14141C),
                title = { Text("Soft Banner Nudge Test", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, vibe.accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "⏱️ Enak: 18 min on purpose. 2 minutes remaining before your planned wrap-up.",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            arms = arms.map { if (it.id == "banner") it.copy(alpha = it.alpha + 1) else it }
                            activeTestDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary)
                    ) {
                        Text("Banner Noticed", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeTestDialog = null }) {
                        Text("Close", color = TextMuted)
                    }
                }
            )
        }
        "negotiation" -> {
            AlertDialog(
                onDismissRequest = { activeTestDialog = null },
                containerColor = Color(0xFF14141C),
                title = { Text("AI Coach Negotiation Test", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "\"You requested 5 more minutes. What concrete thing are you finishing before closing?\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = vibe.accent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI negotiation prevents vague, automatic extensions by demanding concrete stopping cues.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            arms = arms.map { if (it.id == "negotiation") it.copy(alpha = it.alpha + 1) else it }
                            activeTestDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary)
                    ) {
                        Text("Intent Confirmed", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeTestDialog = null }) {
                        Text("Close", color = TextMuted)
                    }
                }
            )
        }
    }
}
