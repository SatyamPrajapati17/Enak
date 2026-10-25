package com.enak.app.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
fun LabScreen(viewModel: MainViewModel) {
    val gardenLevel by viewModel.gardenLevel.collectAsState()
    val vibe = LocalVibe.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Science,
                contentDescription = null,
                tint = vibe.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "The Mindful Lab",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Bandit experiments & mindful garden",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mindful Garden Section
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = SuccessGreen.copy(alpha = 0.5f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Your Mindful Garden",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Level $gardenLevel Blooming Plant • Grows with each conscious session",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .border(2.dp, SuccessGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Spa,
                        contentDescription = "Plant",
                        tint = SuccessGreen,
                        modifier = Modifier.size((40 + gardenLevel * 4).dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "🌸 Focus Bloom: ${gardenLevel * 10}% vitality",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = vibe.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Thompson Sampling Bandit Ladder Section
        Text(
            text = "Bandit Intervention Ladder",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Text(
            text = "Thompson-sampling chooses the intervention style that helps you return to focus most effectively.",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        val banditArms = listOf(
            Triple("Breathing Circle", "4s box breathing pause", 0.78f),
            Triple("Anchor Question", "Gentle intent alignment check", 0.65f),
            Triple("Soft Banner", "Peripheral time awareness nudge", 0.52f),
            Triple("AI Negotiation", "5-minute intentional challenge", 0.84f)
        )

        banditArms.forEach { (name, desc, winRate) ->
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
                        Column {
                            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(text = desc, fontSize = 11.sp, color = TextMuted)
                        }
                        Text(
                            text = "${(winRate * 100).toInt()}% win",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = vibe.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
                                .fillMaxWidth(winRate)
                                .clip(RoundedCornerShape(3.dp))
                                .background(vibe.primary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
