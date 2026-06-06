package com.gramaurja.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gramaurja.presentation.screens.admin.AdminDashboardScreen
import com.gramaurja.presentation.screens.history.HistoryScreen
import com.gramaurja.presentation.screens.home.HomeScreen
import com.gramaurja.presentation.screens.map.MapScreen
import com.gramaurja.presentation.screens.profile.ProfileScreen
import com.gramaurja.presentation.screens.pumptimer.PumpTimerScreen
import com.gramaurja.presentation.screens.splash.SplashScreen
import com.gramaurja.presentation.screens.zoneselect.ZoneSelectScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.ZoneSelect.route) {
            ZoneSelectScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.PumpTimer.route) {
            PumpTimerScreen(navController = navController)
        }

        composable(Screen.Map.route) {
            MapScreen(navController = navController)
        }

        composable(Screen.Admin.route) {
            AdminDashboardScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

    }
}
