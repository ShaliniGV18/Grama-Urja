# Grama-Urja Core Code Snippets

This file contains the core code snippets that drive the Grama-Urja app (Smart Village Power Monitor). 

## 1. `MainActivity.kt`
The main entry point of the app, setting up edge-to-edge display and launching the main Navigation Graph.

```kotlin
package com.gramaurja

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.gramaurja.presentation.navigation.NavGraph
import com.gramaurja.presentation.theme.GramaUrjaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GramaUrjaTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
```

## 2. `NavGraph.kt`
Defines the routing for all screens in the application, including the Splash, Home, and Admin screens.

```kotlin
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
```

## 3. `SplashScreen.kt`
The startup screen with animations that displays the Grama-Urja branding.

```kotlin
package com.gramaurja.presentation.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gramaurja.presentation.navigation.Screen
import com.gramaurja.presentation.theme.FolkOrange
import com.gramaurja.presentation.theme.FolkOrangeDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "splash_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 900),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        
        // Parallelize delay and zone check
        val isSelected = withTimeoutOrNull(2000) {
            try {
                viewModel.isZoneSelected()
            } catch (e: Exception) {
                Timber.e(e, "Error checking zone selection")
                false
            }
        } ?: false

        delay(1200) // Ensure splash is visible for at least 1.2s total
        
        if (isSelected) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.ZoneSelect.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FolkOrangeDark, FolkOrange)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        ) {
            Icon(
                imageVector = Icons.Filled.ElectricBolt,
                contentDescription = "Power Icon",
                tint = Color.White,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "ಗ್ರಾಮ ಊರ್ಜಾ",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Grama Urja",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Smart Village Power Monitor",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.70f)
            )
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
```

## 4. `BottomNavBar` (From `HomeScreen.kt`)
The bottom navigation bar logic using Compose.

```kotlin
@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar {
        val items = listOf(
            Triple(Screen.Home, Icons.Filled.Home, R.string.nav_home),
            Triple(Screen.History, Icons.Filled.BarChart, R.string.nav_history),
            Triple(Screen.PumpTimer, Icons.Filled.Timer, R.string.nav_pump),
            Triple(Screen.Map, Icons.Filled.Map, R.string.nav_map),
            Triple(Screen.Profile, Icons.Filled.Person, R.string.nav_profile)
        )

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { (screen, icon, labelRes) ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    if (currentDestination?.route != screen.route) {
                        navationToTab(navController, screen.route)
                    }
                },
                icon = { Icon(icon, null) },
                label = { Text(stringResource(labelRes), fontSize = 11.sp) }
            )
        }
    }
}

private fun navationToTab(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
```
