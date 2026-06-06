package com.gramaurja.presentation.screens.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gramaurja.presentation.components.AppHeaderCard
import com.gramaurja.presentation.components.BottomNavBar
import com.gramaurja.presentation.components.SectionCard
import com.gramaurja.presentation.navigation.Screen
import com.gramaurja.presentation.theme.PowerBlueDark
import com.gramaurja.presentation.theme.PowerBlueSurface
import com.gramaurja.presentation.theme.PowerRedMedium

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppHeaderCard(
                title = "Profile",
                subtitle = "Preferences, sharing, and zone controls.",
                icon = Icons.Filled.Person
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = PowerBlueSurface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = PowerBlueDark, modifier = Modifier.size(34.dp))
                            }
                        }
                        Spacer(modifier = Modifier.size(14.dp))
                        Column {
                            Text("Farmer account", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "ID: ${uiState.userId.take(8)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                SectionCard(title = "Settings", subtitle = "Manage your active zone and app preferences.") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileItem(
                            icon = Icons.Filled.LocationOn,
                            label = "Active Zone",
                            value = uiState.zoneName,
                            onClick = { navController.navigate(Screen.ZoneSelect.route) }
                        )
                        ProfileItem(
                            icon = Icons.Filled.Language,
                            label = "Language",
                            value = if (AppCompatDelegate.getApplicationLocales().toLanguageTags() == "kn") "Kannada" else "English",
                            onClick = {
                                val currentLocales = AppCompatDelegate.getApplicationLocales()
                                val isKannada = currentLocales.toLanguageTags() == "kn"
                                val newLocale = if (isKannada) "en" else "kn"
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                            }
                        )
                        ProfileItem(
                            icon = Icons.Filled.Info,
                            label = "About App",
                            value = "Grama Urja v1.0.0",
                            onClick = {}
                        )
                        ProfileItem(
                            icon = Icons.Filled.Share,
                            label = "Share App",
                            value = "Invite other farmers",
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Monitor live power status in your village with Grama Urja. Download: https://gramaurja.example.com"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            }
                        )
                        ProfileItem(
                            icon = Icons.Filled.AdminPanelSettings,
                            label = "Admin Access",
                            value = "Manage all zones",
                            onClick = { navController.navigate(Screen.Admin.route) }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.clearUserData()
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PowerRedMedium.copy(alpha = 0.12f),
                        contentColor = PowerRedMedium
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Clear Data and Reset", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(PowerBlueSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PowerBlueDark)
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
