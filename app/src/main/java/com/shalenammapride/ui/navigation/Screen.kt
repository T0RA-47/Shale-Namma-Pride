package com.shalenammapride.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Meals : Screen("meals")
    object Facilities : Screen("facilities")
    object Achievements : Screen("achievements")
    object Feedback : Screen("feedback")
    object Reports : Screen("reports")
    object AdminLogin : Screen("admin_login")
    object Admin : Screen("admin")
}
