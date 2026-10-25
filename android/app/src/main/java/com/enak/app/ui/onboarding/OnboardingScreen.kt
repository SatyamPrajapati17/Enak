package com.enak.app.ui.onboarding

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.theme.*
import com.enak.app.util.PermissionUtils

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var step by remember { mutableIntStateOf(1) }
    var userName by remember { mutableStateOf("") }
    var selectedIntention by remember { mutableStateOf("Reduce doomscrolling") }
    var selectedPersonality by remember { mutableStateOf("calm") }

    // Live permission states
    var hasUsage by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionUtils.hasOverlayPermission(context)) }
    var hasBattery by remember { mutableStateOf(PermissionUtils.isBatteryOptimizationIgnored(context)) }
    var hasNotifListener by remember { mutableStateOf(PermissionUtils.isNotificationListenerEnabled(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(context)) }
    var hasDeviceAdmin by remember { mutableStateOf(PermissionUtils.isDeviceAdminEnabled(context)) }

    // Recheck permissions onResume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsage = PermissionUtils.hasUsageStatsPermission(context)
                hasOverlay = PermissionUtils.hasOverlayPermission(context)
                hasBattery = PermissionUtils.isBatteryOptimizationIgnored(context)
                hasNotifListener = PermissionUtils.isNotificationListenerEnabled(context)
                hasAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
                hasDeviceAdmin = PermissionUtils.isDeviceAdminEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val vibe = LocalVibe.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (step == i) 32.dp else 16.dp)
                            .clip(CircleShape)
                            .background(if (step >= i) vibe.primary else Color(0x33FFFFFF))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            when (step) {
                1 -> {
                    // Step 1: Name
                    Text(
                        text = "enak",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = vibe.primary
                    )
                    Text(
                        text = "Scroll with a reason.",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "What should your mindful coach call you?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            placeholder = { Text("Enter your name", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = vibe.primary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f, fill = false))
                    Spacer(modifier = Modifier.height(40.dp))

                    PrimaryGradientButton(
                        text = "Continue",
                        enabled = userName.isNotBlank(),
                        onClick = { step = 2 }
                    )
                }

                2 -> {
                    // Step 2: Intentions & Goals
                    Text(
                        text = "Your Mindful Goal",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "What brings you to Enak today, $userName?",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    val intentions = listOf(
                        "Reduce doomscrolling" to Icons.Outlined.PhoneAndroid,
                        "Deep work & study focus" to Icons.Outlined.Lightbulb,
                        "Sleep earlier (Night Guard)" to Icons.Outlined.Bedtime,
                        "Conscious social media" to Icons.Outlined.Spa
                    )

                    intentions.forEach { (title, icon) ->
                        val selected = selectedIntention == title
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { selectedIntention = title },
                            borderColor = if (selected) vibe.primary else GlassBorder
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) vibe.primary else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) TextPrimary else TextSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = vibe.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryGradientButton(text = "Next", onClick = { step = 3 })
                }

                3 -> {
                    // Step 3: Coach Vibe
                    Text(
                        text = "Choose Coach Persona",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "How would you like your coach to speak with you?",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    val personalities = listOf(
                        Triple("calm", "Calm Zen", "Gentle, non-judgmental prompts and mindful pauses."),
                        Triple("strict", "Direct & Strict", "Honest reality checks when you drift away from your intent."),
                        Triple("friendly", "Encouraging Friend", "Warm, uplifting nudges celebrating small daily wins."),
                        Triple("monk", "Philosophical Monk", "Deep reflections on intentionality, focus, and time.")
                    )

                    personalities.forEach { (id, title, desc) ->
                        val selected = selectedPersonality == id
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { selectedPersonality = id },
                            borderColor = if (selected) vibe.primary else GlassBorder
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selected) vibe.primary else TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = vibe.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = desc, fontSize = 13.sp, color = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryGradientButton(text = "Continue to Permissions", onClick = { step = 4 })
                }

                4 -> {
                    // Step 4: System Permissions Walkthrough
                    Text(
                        text = "System Permissions",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Enak works locally on your device to protect your attention without ever reading private screen contents.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // 1. Usage Stats
                    PermissionCard(
                        title = "1. Screen Time Usage Access",
                        description = "Required to track daily app minutes and compute your intentionality score.",
                        isGranted = hasUsage,
                        onGrant = { context.startActivity(PermissionUtils.getUsageStatsIntent()) }
                    )

                    // 2. Overlay
                    PermissionCard(
                        title = "2. Display Over Other Apps",
                        description = "Required to show mindful pause screens and breathing exercises when limits are reached.",
                        isGranted = hasOverlay,
                        onGrant = { context.startActivity(PermissionUtils.getOverlayIntent(context)) }
                    )

                    // 3. Battery Optimization
                    PermissionCard(
                        title = "3. Unrestricted Battery",
                        description = "Required so Android Doze doesn't kill background session timers or focus mode.",
                        isGranted = hasBattery,
                        onGrant = { context.startActivity(PermissionUtils.getBatteryOptimizationIntent(context)) }
                    )

                    // 4. Notification Access
                    PermissionCard(
                        title = "4. Notification Silencing (Optional)",
                        description = "Allows Enak to mute distracting alerts during deep focus sessions.",
                        isGranted = hasNotifListener,
                        onGrant = { context.startActivity(PermissionUtils.getNotificationListenerIntent()) }
                    )

                    // 5. Accessibility Service
                    PermissionCard(
                        title = "5. Instant App Blocker (Optional)",
                        description = "Instant window detection without polling lag. Never reads screen text or messages.",
                        isGranted = hasAccessibility,
                        onGrant = { context.startActivity(PermissionUtils.getAccessibilityIntent()) }
                    )

                    // 6. Device Admin
                    PermissionCard(
                        title = "6. Strict Mode (Optional)",
                        description = "Prevents impulsively bypassing or force-stopping limits during focus.",
                        isGranted = hasDeviceAdmin,
                        onGrant = { context.startActivity(PermissionUtils.getDeviceAdminIntent(context)) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryGradientButton(
                        text = "Enter Enak Dashboard",
                        onClick = {
                            viewModel.completeOnboarding(
                                name = userName.ifBlank { "Traveler" },
                                personality = selectedPersonality,
                                language = "en",
                                weeklyGoalHours = 14,
                                quietStart = 22,
                                quietEnd = 7
                            )
                            onComplete()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    val vibe = LocalVibe.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        borderColor = if (isGranted) SuccessGreen.copy(alpha = 0.5f) else GlassBorder
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isGranted) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.2f))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Granted",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onGrant,
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Grant", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun PrimaryGradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val vibe = LocalVibe.current

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) Brush.horizontalGradient(vibe.gradient)
                    else Brush.horizontalGradient(listOf(Color(0xFF2A2A38), Color(0xFF2A2A38)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else TextMuted
            )
        }
    }
}
