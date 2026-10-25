package com.enak.app.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.theme.*

@Composable
fun InsightsScreen() {
    val vibe = LocalVibe.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Intentional Insights",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Weekly patterns & attention drift analysis",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // Time On Purpose vs Drift Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Weekly Alignment",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "On Purpose", fontSize = 12.sp, color = TextMuted)
                    Text(text = "11h 24m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = vibe.primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Drifted", fontSize = 12.sp, color = TextMuted)
                    Text(text = "1h 48m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftPink)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Alignment", fontSize = 12.sp, color = TextMuted)
                    Text(text = "86%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SoftPink)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.86f)
                        .background(vibe.primary)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.14f)
                        .background(SoftPink)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7x24 Risky Hours Heatmap
        Text(
            text = "7x24 Attention Risk Heatmap",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Text(
            text = "Cyan = High Intentionality • Pink = High Risk Hours",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                days.forEachIndexed { dayIdx, dayName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier.width(14.dp)
                        )
                        for (h in 0..23) {
                            val isRisky = (dayIdx in 4..6 && h >= 22) || (h in 23..2)
                            val isFocused = h in 9..17
                            val cellColor = when {
                                isRisky -> SoftPink.copy(alpha = 0.7f)
                                isFocused -> vibe.accent.copy(alpha = 0.6f)
                                else -> Color(0x22FFFFFF)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Focus Streaks & Milestones Section
        Text(
            text = "Conscious Streaks & Milestones",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Text(
            text = "Consistent intentional sessions unlock garden species & coach personas",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        // 1. Streak Tracker Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔥 5-Day Streak",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = vibe.accent
                        )
                    }
                    Text(
                        text = "Personal Best: 14 Days • 86% Alignment",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(vibe.primary.copy(alpha = 0.2f))
                        .border(1.dp, vibe.accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Active Today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days Circles
            val weekDays = listOf("M" to true, "T" to true, "W" to true, "T" to true, "F" to true, "S" to true, "S" to false)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEachIndexed { idx, (day, done) ->
                    val isToday = idx == 5
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        done -> vibe.primary
                                        isToday -> vibe.accent.copy(alpha = 0.3f)
                                        else -> DarkSurface
                                    }
                                )
                                .border(
                                    1.5.dp,
                                    when {
                                        isToday -> vibe.accent
                                        done -> vibe.primary
                                        else -> GlassBorder
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (done) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = day,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            color = if (done || isToday) vibe.accent else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Multi-tier Milestones Cards
        data class MilestoneItem(
            val title: String,
            val description: String,
            val current: Int,
            val target: Int,
            val reward: String,
            val isUnlocked: Boolean
        )

        val milestones = listOf(
            MilestoneItem(
                title = "🥉 3-Day Foundation",
                description = "Build the core intentional habit without compulsive drifting",
                current = 3,
                target = 3,
                reward = "Sprout Plant & +10 Garden Vitality",
                isUnlocked = true
            ),
            MilestoneItem(
                title = "🥈 7-Day Consistency",
                description = "Complete one week of conscious screen time sessions",
                current = 5,
                target = 7,
                reward = "Unlocks Zen Bamboo species in Lab",
                isUnlocked = false
            ),
            MilestoneItem(
                title = "🥇 14-Day Deep Mastery",
                description = "Maintain quiet hours & intent boundaries for 2 weeks",
                current = 5,
                target = 14,
                reward = "Unlocks Monk AI Coach voice & persona",
                isUnlocked = false
            ),
            MilestoneItem(
                title = "💎 30-Day Digital Sovereign",
                description = "A month of complete liberation from mindless feeds",
                current = 5,
                target = 30,
                reward = "Unlocks Golden Bonsai & Sovereign Theme",
                isUnlocked = false
            )
        )

        milestones.forEach { item ->
            val progress = (item.current.toFloat() / item.target.toFloat()).coerceIn(0f, 1f)
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                borderColor = if (item.isUnlocked) SuccessGreen.copy(alpha = 0.5f) else GlassBorder
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        if (item.isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Completed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        } else {
                            Text(
                                text = "${item.current}/${item.target} days",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = vibe.accent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
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
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (item.isUnlocked) SuccessGreen else vibe.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CardGiftcard,
                            contentDescription = null,
                            tint = vibe.accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reward: ${item.reward}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Lifetime Mindful Metrics
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Lifetime Impact",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "42", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = vibe.primary)
                    Text(text = "Sessions", fontSize = 11.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "14h 20m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = vibe.accent)
                    Text(text = "Time Saved", fontSize = 11.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "28", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    Text(text = "Loops Broken", fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
