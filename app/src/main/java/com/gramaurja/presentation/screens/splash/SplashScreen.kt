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
