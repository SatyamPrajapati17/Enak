package com.enak.app.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

        // Focus Streaks & Milestones
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Milestones & Streaks",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🔥 5-Day Mindful Streak", fontSize = 13.sp, color = vibe.accent)
                Text(text = "Target: 7 Days", fontSize = 12.sp, color = TextMuted)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Next reward unlocks a new rare plant for your Mindful Garden in the Lab.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
