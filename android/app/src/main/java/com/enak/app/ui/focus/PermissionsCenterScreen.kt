package com.enak.app.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.enak.app.ui.onboarding.PermissionCard
import com.enak.app.ui.theme.JetBlack
import com.enak.app.ui.theme.LocalVibe
import com.enak.app.ui.theme.TextPrimary
import com.enak.app.ui.theme.TextSecondary
import com.enak.app.util.PermissionUtils

@Composable
fun PermissionsCenterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val vibe = LocalVibe.current

    var hasUsage by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionUtils.hasOverlayPermission(context)) }
    var hasBattery by remember { mutableStateOf(PermissionUtils.isBatteryOptimizationIgnored(context)) }
    var hasNotifListener by remember { mutableStateOf(PermissionUtils.isNotificationListenerEnabled(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(context)) }
    var hasDeviceAdmin by remember { mutableStateOf(PermissionUtils.isDeviceAdminEnabled(context)) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Permissions Center",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Live system security & access status",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PermissionCard(
            title = "Usage Stats Access",
            description = "Enables calculating foreground minutes and computing intentionality scores.",
            isGranted = hasUsage,
            onGrant = { context.startActivity(PermissionUtils.getUsageStatsIntent()) }
        )

        PermissionCard(
            title = "Display Over Other Apps",
            description = "Renders mindful reminder overlays and breathing pauses over distracting apps.",
            isGranted = hasOverlay,
            onGrant = { context.startActivity(PermissionUtils.getOverlayIntent(context)) }
        )

        PermissionCard(
            title = "Unrestricted Battery",
            description = "Prevents Android battery saver from killing timer and foreground guard services.",
            isGranted = hasBattery,
            onGrant = { context.startActivity(PermissionUtils.getBatteryOptimizationIntent(context)) }
        )

        PermissionCard(
            title = "Notification Listener",
            description = "Allows muting alerts from distracting apps during deep focus sessions.",
            isGranted = hasNotifListener,
            onGrant = { context.startActivity(PermissionUtils.getNotificationListenerIntent()) }
        )

        PermissionCard(
            title = "Accessibility Service",
            description = "Provides instant window transition detection. Never reads screen content.",
            isGranted = hasAccessibility,
            onGrant = { context.startActivity(PermissionUtils.getAccessibilityIntent()) }
        )

        PermissionCard(
            title = "Device Administrator",
            description = "Strict Mode: Prevents impulsively uninstalling or force-stopping during focus.",
            isGranted = hasDeviceAdmin,
            onGrant = { context.startActivity(PermissionUtils.getDeviceAdminIntent(context)) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
