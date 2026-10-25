package com.enak.app.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enak.app.ui.InstalledAppItem
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.onboarding.GlassCard
import com.enak.app.ui.theme.*

@Composable
fun FocusScreen(
    viewModel: MainViewModel,
    onOpenPermissionsCenter: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsState()
    val apps by viewModel.installedApps.collectAsState()
    val vibe = LocalVibe.current
    val colors = LocalEnakColors.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "blocked", "distractions"

    val blockedCount = apps.count { it.isBlocked }
    val distractionsCount = apps.count { it.isDistracting }

    val filteredApps = remember(apps, searchQuery, selectedFilter) {
        apps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "blocked" -> app.isBlocked
                "distractions" -> app.isDistracting
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Digital Wellbeing",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "App Block List & Mindful Shield",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }

                IconButton(onClick = onOpenPermissionsCenter) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Permissions",
                        tint = vibe.primary
                    )
                }
            }
        }

        item {
            // Master Focus Shield Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (settings.focusModeActive) vibe.accent else GlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Distraction Blocker Shield",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (settings.focusModeActive) SuccessGreen.copy(alpha = 0.2f) else DarkSurface)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (settings.focusModeActive) "ACTIVE" else "OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (settings.focusModeActive) SuccessGreen else TextMuted
                                )
                            }
                        }
                        Text(
                            text = "Actively monitors and intercepts blocked apps with mindful intervention overlay.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Switch(
                        checked = settings.focusModeActive,
                        onCheckedChange = { viewModel.toggleFocusMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = vibe.accent
                        )
                    )
                }
            }
        }

        item {
            // Strict Mode Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (settings.strictModeEnabled) vibe.primary else GlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Strict Mode (Anti-Tamper)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Prevents turning off timers or force-quitting Enak during active sessions.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Switch(
                        checked = settings.strictModeEnabled,
                        onCheckedChange = { viewModel.setStrictMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = vibe.primary
                        )
                    )
                }
            }
        }

        item {
            // Permissions Center Shortcut Banner
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPermissionsCenter() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AdminPanelSettings,
                        contentDescription = null,
                        tint = vibe.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Permissions Center",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Usage access, overlays, and accessibility status",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }
        }

        item {
            // Hero App Block List Section Header
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = if (blockedCount > 0) SoftPink else vibe.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App Block List",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                if (blockedCount > 0) SoftPink.copy(alpha = 0.2f)
                                else vibe.primary.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (blockedCount > 0) SoftPink else vibe.primary,
                                RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (blockedCount > 0) "$blockedCount Locked" else "0 Blocked",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (blockedCount > 0) SoftPink else vibe.accent
                        )
                    }
                }

                Text(
                    text = "Apps in this block list will NOT open under any circumstances until you unblock them here in Enak.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Quick Batch Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.blockSuggestedDistractions() },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = vibe.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Block All Distractions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.unblockAllApps() },
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LockOpen,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Unblock All", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps...", fontSize = 13.sp, color = colors.textSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Clear",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = vibe.accent,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text("All (${apps.size})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "blocked",
                        onClick = { selectedFilter = "blocked" },
                        label = { Text("Blocked ($blockedCount)", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "distractions",
                        onClick = { selectedFilter = "distractions" },
                        label = { Text("Distractions ($distractionsCount)", fontSize = 12.sp) }
                    )
                }
            }
        }

        if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedFilter == "blocked") "No apps are blocked" else "No matching apps found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                AppBlockListItemCard(
                    app = app,
                    onToggleBlock = { shouldBlock ->
                        viewModel.toggleAppBlocked(app.packageName, shouldBlock)
                    },
                    onLimitChange = { newLimit ->
                        viewModel.updateAppLimit(app.packageName, newLimit)
                    }
                )
            }
        }
    }
}

@Composable
fun AppBlockListItemCard(
    app: InstalledAppItem,
    onToggleBlock: (Boolean) -> Unit,
    onLimitChange: (Int) -> Unit
) {
    val vibe = LocalVibe.current
    val colors = LocalEnakColors.current
    val isNearLimit = app.usedMinutes >= (app.dailyLimitMinutes * 0.8)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (app.isBlocked) SoftPink.copy(alpha = 0.8f) else if (isNearLimit) SoftPink.copy(alpha = 0.4f) else GlassBorder
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (app.isBlocked) SoftPink.copy(alpha = 0.2f)
                                else vibe.primary.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (app.isBlocked) SoftPink else vibe.primary.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = if (app.isBlocked) SoftPink else vibe.primary,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.appName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            if (app.isBlocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SoftPink.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LOCKED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftPink
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (app.isBlocked) "Locked • Will not open until unblocked"
                            else "${app.usedMinutes} min used today • ${app.dailyLimitMinutes}m boundary",
                            fontSize = 12.sp,
                            color = if (app.isBlocked) SoftPink else if (isNearLimit) SoftPink else colors.textSecondary
                        )
                    }
                }

                // Block / Unblock Toggle Button
                Button(
                    onClick = { onToggleBlock(!app.isBlocked) },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (app.isBlocked) DarkSurface else vibe.primary
                    ),
                    border = if (app.isBlocked) androidx.compose.foundation.BorderStroke(1.dp, SoftPink) else null,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (app.isBlocked) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = if (app.isBlocked) SoftPink else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (app.isBlocked) "Unblock" else "Block",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (app.isBlocked) SoftPink else Color.White
                    )
                }
            }

            // Daily Boundary Limits (only active when not hard blocked)
            if (!app.isBlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Daily limit: ", fontSize = 11.sp, color = colors.textSecondary)
                    val limits = listOf(15, 30, 45, 60)
                    limits.forEach { limit ->
                        val isSel = app.dailyLimitMinutes == limit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) vibe.primary else DarkSurface)
                                .clickable { onLimitChange(limit) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${limit}m",
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
