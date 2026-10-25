package com.enak.app.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.onboarding.PermissionCard
import com.enak.app.ui.theme.*
import com.enak.app.util.PermissionUtils

@Composable
fun PermissionsCenterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val vibe = LocalVibe.current
    val colors = LocalEnakColors.current

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

    fun safeLaunch(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            try {
                context.startActivity(PermissionUtils.getAppSettingsIntent(context))
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Permissions Center",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Live device connection & security allowance",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Android 13+ Restricted Settings Guidance Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = vibe.accent.copy(alpha = 0.5f)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = vibe.accent,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Setting Disabled or Restricted?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "On Android 13/14/15, sideloaded apps require permission unlock: Tap 'Open App Settings' $\rightarrow$ tap the ⋮ 3 dots in the top right $\rightarrow$ tap 'Allow restricted settings'.",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { safeLaunch { context.startActivity(PermissionUtils.getAppSettingsIntent(context)) } },
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open App Settings", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            title = "Usage Stats Access",
            description = "Enables calculating foreground minutes and computing intentionality scores.",
            isGranted = hasUsage,
            onGrant = { safeLaunch { context.startActivity(PermissionUtils.getUsageStatsIntent()) } }
        )

        PermissionCard(
            title = "Display Over Other Apps",
            description = "Renders mindful reminder overlays and breathing pauses over distracting apps.",
            isGranted = hasOverlay,
            onGrant = { safeLaunch { context.startActivity(PermissionUtils.getOverlayIntent(context)) } }
        )

        PermissionCard(
            title = "Unrestricted Battery",
            description = "Prevents Android battery saver from killing timer and foreground guard services.",
            isGranted = hasBattery,
            onGrant = { safeLaunch { context.startActivity(PermissionUtils.getBatteryOptimizationIntent(context)) } }
        )

        PermissionCard(
            title = "Notification Listener",
            description = "Allows muting alerts from distracting apps during deep focus sessions.",
            isGranted = hasNotifListener,
            onGrant = { safeLaunch { context.startActivity(PermissionUtils.getNotificationListenerIntent()) } }
        )

        PermissionCard(
            title = "Accessibility Service",
            description = "Provides instant window transition detection. Never reads screen content.",
            isGranted = hasAccessibility,
            onGrant = { safeLaunch { context.startActivity(PermissionUtils.getAccessibilityIntent()) } }
        )

        PermissionCard(
            title = "Device Administrator",
            description = "Strict Mode: Prevents impulsively uninstalling or force-stopping during focus.",
            isGranted = hasDeviceAdmin,
            onGrant = { safeLaunch { context.startActivity(PermissionUtils.getDeviceAdminIntent(context)) } }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
