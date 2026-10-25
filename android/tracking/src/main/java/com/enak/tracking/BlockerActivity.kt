package com.enak.tracking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class BlockerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pkgName = intent.getStringExtra("PACKAGE_NAME") ?: "Distracting App"
        val isFromBlockList = intent.getBooleanExtra("IS_FROM_BLOCK_LIST", false) || DistractionBlocker.isAppBlocked(pkgName)
        val blockReason = if (isFromBlockList) {
            "🔒 Locked by Enak App Block List"
        } else {
            intent.getStringExtra("BLOCK_REASON") ?: "Focus Mode Active"
        }

        // Get friendly app name from PackageManager
        val friendlyName = try {
            val appInfo = packageManager.getApplicationInfo(pkgName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            pkgName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }

        setContent {
            BlockerOverlayScreen(
                appName = friendlyName,
                packageName = pkgName,
                blockReason = blockReason,
                isFromBlockList = isFromBlockList,
                onExitApp = {
                    // Send user to Android Home screen
                    val startMain = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(startMain)
                    finish()
                },
                onPutTimer = { minutes ->
                    // Snooze blocking for a while
                    DistractionBlocker.snoozeApp(pkgName, minutes)
                    finish()
                },
                onBackToEnak = {
                    // Navigate directly into Enak
                    try {
                        val enakIntent = Intent(this, Class.forName("com.enak.app.MainActivity")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("NAVIGATE_TO", "focus")
                        }
                        startActivity(enakIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    finish()
                }
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent simply back-pressing out of the blocker into the distracting app
        val startMain = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(startMain)
        finish()
    }
}

@Composable
fun BlockerOverlayScreen(
    appName: String,
    packageName: String,
    blockReason: String,
    isFromBlockList: Boolean = false,
    onExitApp: () -> Unit,
    onPutTimer: (minutes: Int) -> Unit,
    onBackToEnak: () -> Unit
) {
    var isBreathingActive by remember { mutableStateOf(false) }

    if (isBreathingActive) {
        OverlayBreathingExercise(onClose = { isBreathingActive = false })
        return
    }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B10))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsing Warning Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8A2BE2).copy(alpha = pulseAlpha))
                    .border(2.dp, Color(0xFF22E3F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Color(0xFF22E3F0),
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reason Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x338A2BE2))
                    .border(1.dp, Color(0xFF8A2BE2), RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = blockReason,
                    color = Color(0xFF22E3F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "$appName is Blocked",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isFromBlockList) {
                    "This app is in your Enak App Block List. It is locked and will NOT open until you unblock it in the Enak app."
                } else {
                    "Enak is holding this boundary so you stay on purpose. Long screen time is fine; mindless feeds are not."
                },
                color = Color(0xFFA1A1AA),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Option 1: Exit that app (Go Home)
            Button(
                onClick = onExitApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2))
            ) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exit $appName (Go Home)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isFromBlockList) {
                // Option 2: Put timer for a while (+5 Min Snooze) - only for gentle limits
                Button(
                    onClick = { onPutTimer(5) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B26)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Icon(imageVector = Icons.Outlined.HourglassEmpty, contentDescription = null, tint = Color(0xFF22E3F0))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Put Timer for a While (+5m)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Option 3: Back to Enak / Open Enak to Unblock
            Button(
                onClick = onBackToEnak,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFromBlockList) Color(0xFF1B1B26) else Color(0xFF1B1B26)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isFromBlockList) Color(0xFF22E3F0) else Color(0x33FFFFFF)
                )
            ) {
                Icon(
                    imageVector = if (isFromBlockList) Icons.Outlined.LockOpen else Icons.Outlined.Home,
                    contentDescription = null,
                    tint = if (isFromBlockList) Color(0xFF22E3F0) else Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFromBlockList) "Open Enak to Unblock" else "Back to Enak",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Optional Breather
            TextButton(onClick = { isBreathingActive = true }) {
                Icon(imageVector = Icons.Outlined.Spa, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Take a 60-Second Mindful Breather", color = Color(0xFFA1A1AA), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun OverlayBreathingExercise(onClose: () -> Unit) {
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
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B10))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Mindful Pause", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size((180 * scale).dp)
                    .clip(CircleShape)
                    .background(Color(0x338A2BE2))
                    .border(2.dp, Color(0xFF22E3F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = phase, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(text = "$secondsInPhase", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22E3F0))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B26)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done Breather", color = Color.White)
            }
        }
    }
}

@Composable
fun BlockerScreen(
    packageName: String,
    warningNumber: Int = 1,
    isHardBlock: Boolean = false,
    onCloseApp: () -> Unit = {},
    onDismissWarning: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B10))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val warningBadge = if (isHardBlock) "Hard Daily Limit Reached" else "Mindful Warning $warningNumber of 2"
        val headline = if (isHardBlock) "Time to step away." else "Is this what you came for?"

        Text(text = warningBadge, color = Color(0xFF22E3F0), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = headline, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCloseApp,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Close App & Step Away", color = Color.White)
        }

        if (!isHardBlock) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDismissWarning,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Continue Mindfully", color = Color(0xFF22E3F0))
            }
        }
    }
}
