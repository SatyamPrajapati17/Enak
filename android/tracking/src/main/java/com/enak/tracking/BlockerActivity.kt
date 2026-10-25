package com.enak.tracking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

        val pkgName = intent.getStringExtra("PACKAGE_NAME") ?: "Target App"
        val warningNumber = intent.getIntExtra("WARNING_NUMBER", 1)
        val isHardBlock = intent.getBooleanExtra("IS_HARD_BLOCK", false)

        setContent {
            BlockerScreen(
                packageName = pkgName,
                warningNumber = warningNumber,
                isHardBlock = isHardBlock,
                onCloseApp = {
                    // Send user to Android Home screen
                    val startMain = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(startMain)
                    finish()
                },
                onDismissWarning = {
                    finish()
                }
            )
        }
    }
}

@Composable
fun BlockerScreen(
    packageName: String,
    warningNumber: Int,
    isHardBlock: Boolean,
    onCloseApp: () -> Unit,
    onDismissWarning: () -> Unit
) {
    var showBreather by remember { mutableStateOf(false) }
    var showNegotiation by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B0B10),
            Color(0xFF161224),
            Color(0xFF0B0B10)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showBreather) {
            BreatherDialog(
                onFinished = {
                    showBreather = false
                    if (!isHardBlock) onDismissWarning()
                }
            )
        } else if (showNegotiation) {
            NegotiationDialog(
                packageName = packageName,
                onAccepted = {
                    showNegotiation = false
                    onDismissWarning()
                },
                onDeclined = {
                    showNegotiation = false
                }
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Warning pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isHardBlock) Color(0x33F43F5E) else Color(0x338A2BE2))
                        .border(
                            1.dp,
                            if (isHardBlock) Color(0xFFF43F5E) else Color(0xFF8A2BE2),
                            RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isHardBlock) "Hard Daily Limit Reached" else "Mindful Warning $warningNumber of 2",
                        color = if (isHardBlock) Color(0xFFFB7185) else Color(0xFF22E3F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isHardBlock) "Time to step away." else "Is this what you came for?",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isHardBlock)
                        "You have reached your daily boundary for this app. Give your mind space to recharge."
                    else
                        "You're nearing or past your planned limit. Pause for a second before continuing.",
                    color = Color(0xFFA1A1AA),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Primary Action: Close app
                Button(
                    onClick = onCloseApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8A2BE2)
                    )
                ) {
                    Text(
                        text = "Close App & Step Away",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Action: Take a Breather
                OutlinedButton(
                    onClick = { showBreather = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF22E3F0)
                    )
                ) {
                    Text(
                        text = "Take a 30s Breather",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!isHardBlock) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Tertiary Action: Ask for 5 more minutes (AI negotiation)
                    TextButton(
                        onClick = { showNegotiation = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ask AI Coach for 5 more minutes",
                            color = Color(0xFFA1A1AA),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreatherDialog(onFinished: () -> Unit) {
    var phase by remember { mutableStateOf("Breathe In") }
    var secondsLeft by remember { mutableStateOf(20) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
            phase = when ((20 - secondsLeft) % 8) {
                in 0..3 -> "Breathe In..."
                in 4..6 -> "Hold..."
                else -> "Release..."
            }
        }
        onFinished()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0x228A2BE2))
                .border(2.dp, Color(0xFF22E3F0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = phase,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${secondsLeft}s",
                    color = Color(0xFF22E3F0),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onFinished) {
            Text("Done breathing", color = Color(0xFFA1A1AA))
        }
    }
}

@Composable
fun NegotiationDialog(
    packageName: String,
    onAccepted: () -> Unit,
    onDeclined: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "State Your Reason",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enak's coach accepts specific, on-purpose reasons (e.g., 'finishing the chapter').",
                color = Color(0xFFA1A1AA),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                placeholder = { Text("What are you finishing?", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            val currentFeedback = feedback
            if (currentFeedback != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentFeedback,
                    color = Color(0xFF22E3F0),
                    fontSize = 14.sp
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDeclined) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        isChecking = true
                        // Pure logic evaluation: specific reasons accepted
                        if (reason.trim().length > 10 && !reason.contains("just", ignoreCase = true)) {
                            feedback = "ACCEPT: Purpose verified. Take your 5 minutes consciously."
                            onAccepted()
                        } else {
                            feedback = "CHALLENGE: That sounds a bit automatic. Take a short pause instead?"
                            isChecking = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2)),
                    enabled = reason.isNotBlank() && !isChecking
                ) {
                    Text("Submit to Coach")
                }
            }
        }
    }
}
