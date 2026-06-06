package com.gramaurja.presentation.screens.zoneselect

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.gramaurja.R
import com.gramaurja.data.model.Zone
import com.gramaurja.presentation.components.AppHeaderCard
import com.gramaurja.presentation.components.SectionCard
import com.gramaurja.presentation.navigation.Screen
import com.gramaurja.presentation.theme.PowerBlueDark
import com.gramaurja.presentation.theme.PowerBlueSurface
import com.gramaurja.presentation.theme.PowerRedMedium
import com.gramaurja.presentation.theme.UnknownGrey

@Composable
fun ZoneSelectScreen(
    navController: NavController,
    viewModel: ZoneSelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            detectLocation(fusedLocationClient, viewModel)
        } else {
            viewModel.onLocationError("Location permission denied")
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeaderCard(
                title = stringResource(R.string.select_zone),
                subtitle = "Search villages or use your current location to find the nearest transformer zone.",
                icon = Icons.Filled.LocationOn
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(title = "Find your village", subtitle = "Search manually or detect your nearest zone.") {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_zones)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val hasLocation = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasLocation) {
                                detectLocation(fusedLocationClient, viewModel)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PowerBlueDark)
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.detect_location), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    uiState.detectedNearestZone?.let { zone ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = PowerBlueSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PowerBlueDark)
                                Spacer(modifier = Modifier.size(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.nearest_zone), style = MaterialTheme.typography.bodyMedium, color = PowerBlueDark)
                                    Text(zone.name, style = MaterialTheme.typography.titleMedium, color = PowerBlueDark)
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.selectZone(zone) {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.ZoneSelect.route) { inclusive = true }
                                            }
                                        }
                                    }
                                ) {
                                    Text(stringResource(R.string.confirm_zone), color = PowerBlueDark)
                                }
                            }
                        }
                    }

                    uiState.locationError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = PowerRedMedium, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                SectionCard(
                    title = "All zones",
                    subtitle = "${uiState.filteredZones.size} results available"
                ) {
                    LazyColumn(
                        modifier = Modifier.height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.filteredZones) { zone ->
                            ZoneCard(zone = zone) {
                                viewModel.selectZone(zone) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.ZoneSelect.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneCard(zone: Zone, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PowerBlueSurface),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ElectricBolt, contentDescription = null, tint = PowerBlueDark)
                    }
                }
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${zone.district} • ${zone.transformerNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UnknownGrey
                )
            }
            Text("Select", color = PowerBlueDark, fontWeight = FontWeight.SemiBold)
        }
    }
}

@SuppressLint("MissingPermission")
private fun detectLocation(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: ZoneSelectViewModel
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            viewModel.detectNearestZone(location.latitude, location.longitude)
        } else {
            viewModel.onLocationError("Could not get current location. Try again.")
        }
    }.addOnFailureListener {
        viewModel.onLocationError("Location service error: ${it.message}")
    }
}
