package com.enak.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.enak.app.ui.MainViewModel
import com.enak.app.ui.coach.CoachScreen
import com.enak.app.ui.focus.FocusScreen
import com.enak.app.ui.focus.PermissionsCenterScreen
import com.enak.app.ui.insights.InsightsScreen
import com.enak.app.ui.intent.IntentScreen
import com.enak.app.ui.lab.LabScreen
import com.enak.app.ui.onboarding.OnboardingScreen
import com.enak.app.ui.recap.RecapScreen
import com.enak.app.ui.session.SessionScreen
import com.enak.app.ui.settings.SettingsScreen
import com.enak.app.ui.theme.*
import com.enak.app.ui.today.TodayScreen
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Outlined.Home)
    object Session : Screen("session", "Session", Icons.Outlined.PlayCircle)
    object Insights : Screen("insights", "Insights", Icons.Outlined.Analytics)
    object Focus : Screen("focus", "Focus", Icons.Outlined.Shield)
    object Coach : Screen("coach", "Coach", Icons.Outlined.SmartToy)
    object Lab : Screen("lab", "Lab", Icons.Outlined.Science)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Outlined.Star)
    object Intent : Screen("intent", "Set Intent", Icons.Outlined.Bolt)
    object Recap : Screen("recap", "Recap", Icons.Outlined.Done)
    object PermissionsCenter : Screen("permissions_center", "Permissions", Icons.Outlined.Security)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.enak.tracking.DistractionBlocker.init(this)
        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")
        setContent {
            val settings by viewModel.userSettings.collectAsState()
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = when (settings.themeMode) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }
            EnakTheme(vibeId = settings.vibe, isDarkTheme = isDarkTheme) {
                EnakAppRoot(
                    viewModel = viewModel,
                    isOnboarded = settings.isOnboarded,
                    initialRoute = navigateTo
                )
            }
        }
    }
}

@Composable
fun EnakAppRoot(
    viewModel: MainViewModel,
    isOnboarded: Boolean,
    initialRoute: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrEmpty() && isOnboarded) {
            navController.navigate(initialRoute) {
                launchSingleTop = true
            }
        }
    }

    val bottomNavItems = listOf(
        Screen.Today,
        Screen.Session,
        Screen.Insights,
        Screen.Focus,
        Screen.Coach,
        Screen.Lab
    )

    val showBottomNav = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                EnakPillBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = LocalEnakColors.current.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboarded) Screen.Today.route else Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onComplete = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Today.route) {
                TodayScreen(
                    viewModel = viewModel,
                    onStartIntent = { navController.navigate(Screen.Intent.route) },
                    onOpenFocus = { navController.navigate(Screen.Focus.route) }
                )
            }

            composable(Screen.Intent.route) {
                IntentScreen(
                    viewModel = viewModel,
                    onBegin = { navController.navigate(Screen.Session.route) }
                )
            }

            composable(Screen.Session.route) {
                SessionScreen(
                    viewModel = viewModel,
                    onNavigateToIntent = { navController.navigate(Screen.Intent.route) },
                    onFinish = { navController.navigate(Screen.Recap.route) }
                )
            }

            composable(Screen.Recap.route) {
                RecapScreen(
                    viewModel = viewModel,
                    onDone = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Today.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Insights.route) {
                InsightsScreen()
            }

            composable(Screen.Focus.route) {
                FocusScreen(
                    viewModel = viewModel,
                    onOpenPermissionsCenter = { navController.navigate(Screen.PermissionsCenter.route) }
                )
            }

            composable(Screen.PermissionsCenter.route) {
                PermissionsCenterScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Coach.route) {
                CoachScreen(viewModel = viewModel)
            }

            composable(Screen.Lab.route) {
                LabScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onResetOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EnakPillBottomBar(
    items: List<Screen>,
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val vibe = LocalVibe.current
    val colors = LocalEnakColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(colors.cardSurface.copy(alpha = if (colors.isDark) 0.95f else 0.98f))
                .border(1.dp, colors.border, RoundedCornerShape(32.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route
                val buttonModifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (selected) {
                            Modifier.background(Brush.horizontalGradient(vibe.gradient))
                        } else {
                            Modifier.background(Color.Transparent)
                        }
                    )
                    .clickable { onNavigate(screen) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)

                Row(
                    modifier = buttonModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (selected) Color.White else colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = screen.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
