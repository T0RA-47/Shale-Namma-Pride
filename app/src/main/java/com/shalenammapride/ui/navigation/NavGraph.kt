package com.shalenammapride.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shalenammapride.ui.components.BottomNavBar
import com.shalenammapride.ui.screens.achievement.AchievementScreen
import com.shalenammapride.ui.screens.admin.AdminLoginScreen
import com.shalenammapride.ui.screens.admin.AdminScreen
import com.shalenammapride.ui.screens.facility.FacilityScreen
import com.shalenammapride.ui.screens.feedback.FeedbackScreen
import com.shalenammapride.ui.screens.home.HomeScreen
import com.shalenammapride.ui.screens.info.SchoolInfoScreen
import com.shalenammapride.ui.screens.meal.MealScreen
import com.shalenammapride.ui.screens.reports.ReportsScreen
import com.shalenammapride.ui.screens.splash.SplashScreen
import com.shalenammapride.ui.theme.Saffron
import com.shalenammapride.util.AppStrings
import com.shalenammapride.util.EnglishStrings
import com.shalenammapride.util.KannadaStrings

val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Meals.route,
    Screen.Facilities.route,
    Screen.Achievements.route,
    Screen.Feedback.route
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShaleNavGraph(
    isKannada: Boolean,
    isDarkMode: Boolean,
    onLanguageToggle: () -> Unit,
    onDarkModeToggle: () -> Unit
) {
    val navController = rememberNavController()
    val strings: AppStrings = if (isKannada) KannadaStrings else EnglishStrings
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isAdmin by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        topBar = {
            if (currentRoute != Screen.Splash.route) {
                TopAppBar(
                    title = { Text(strings.appName, style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Saffron,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = onLanguageToggle) {
                            Icon(Icons.Filled.Translate, contentDescription = strings.language)
                        }
                        IconButton(onClick = onDarkModeToggle) {
                            Icon(
                                if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = strings.darkMode
                            )
                        }
                        if (isAdmin) {
                            IconButton(onClick = { navController.navigate(Screen.Admin.route) }) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = strings.admin)
                            }
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(strings.schoolInfo) },
                                onClick = { showMenu = false; navController.navigate(Screen.SchoolInfo.route) },
                                leadingIcon = { Icon(Icons.Filled.School, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.reports) },
                                onClick = { showMenu = false; navController.navigate(Screen.Reports.route) },
                                leadingIcon = { Icon(Icons.Filled.Assessment, contentDescription = null) }
                            )
                            if (!isAdmin) {
                                DropdownMenuItem(
                                    text = { Text(strings.adminLogin) },
                                    onClick = { showMenu = false; navController.navigate(Screen.AdminLogin.route) },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Logout Admin") },
                                    onClick = { showMenu = false; isAdmin = false },
                                    leadingIcon = { Icon(Icons.Filled.ExitToApp, contentDescription = null) }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    strings = strings,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(strings = strings, onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                })
            }
            composable(Screen.Meals.route) { MealScreen(strings = strings, isAdmin = isAdmin) }
            composable(Screen.Facilities.route) { FacilityScreen(strings = strings, isAdmin = isAdmin) }
            composable(Screen.Achievements.route) { AchievementScreen(strings = strings, isAdmin = isAdmin) }
            composable(Screen.Feedback.route) { FeedbackScreen(strings = strings, isAdmin = isAdmin) }
            composable(Screen.Reports.route) { ReportsScreen(strings = strings) }
            composable(Screen.SchoolInfo.route) { SchoolInfoScreen(strings = strings) }
            composable(Screen.AdminLogin.route) {
                AdminLoginScreen(
                    strings = strings,
                    onLoginSuccess = { isAdmin = true; navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Admin.route) {
                AdminScreen(strings = strings, onLogout = { isAdmin = false; navController.popBackStack() })
            }
        }
    }
}
