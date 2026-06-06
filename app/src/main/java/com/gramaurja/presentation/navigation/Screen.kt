package com.gramaurja.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object ZoneSelect : Screen("zone_select")
    object Home : Screen("home")
    object History : Screen("history")
    object PumpTimer : Screen("pump_timer")
    object Map : Screen("map")
    object Admin : Screen("admin")
    object Profile : Screen("profile")
}
