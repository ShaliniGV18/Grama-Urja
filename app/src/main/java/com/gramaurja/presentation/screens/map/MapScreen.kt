package com.gramaurja.presentation.screens.map

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.gramaurja.R
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.data.model.Zone
import com.gramaurja.presentation.components.BottomNavBar
import com.gramaurja.presentation.components.InfoChip
import com.gramaurja.presentation.theme.FolkOrangeDark
import com.gramaurja.presentation.theme.PowerBlueDark
import com.gramaurja.presentation.theme.PowerBlueMedium
import com.gramaurja.presentation.theme.PowerRedMedium
import com.gramaurja.presentation.theme.UnknownGrey

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(15.3173, 75.7139), 6f)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                properties = MapProperties(isMyLocationEnabled = false)
            ) {
                uiState.zones.forEach { zone ->
                    val status = uiState.zoneStatuses[zone.id]?.status ?: PowerStatus.UNKNOWN
                    val markerColor = when (status) {
                        PowerStatus.ON -> BitmapDescriptorFactory.HUE_GREEN
                        PowerStatus.OFF -> BitmapDescriptorFactory.HUE_RED
                        PowerStatus.UNKNOWN -> BitmapDescriptorFactory.HUE_ORANGE
                    }

                    Marker(
                        state = MarkerState(position = LatLng(zone.lat, zone.lng)),
                        title = zone.name,
                        snippet = "Status: ${status.name}",
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                        onClick = {
                            selectedZone = zone
                            true
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(FolkOrangeDark.copy(alpha = 0.92f), Color.Transparent)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Map, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Live Power Map",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Tap any marker to view the village and transformer status.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            selectedZone?.let { zone ->
                val status = uiState.zoneStatuses[zone.id]?.status ?: PowerStatus.UNKNOWN
                val statusColor = when (status) {
                    PowerStatus.ON -> PowerBlueMedium
                    PowerStatus.OFF -> PowerRedMedium
                    PowerStatus.UNKNOWN -> UnknownGrey
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = statusColor.copy(alpha = 0.14f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (status) {
                                            PowerStatus.ON -> Icons.Filled.ElectricBolt
                                            PowerStatus.OFF -> Icons.Filled.PowerOff
                                            PowerStatus.UNKNOWN -> Icons.Filled.QuestionMark
                                        },
                                        contentDescription = null,
                                        tint = statusColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(zone.name, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = "${zone.district} • ${zone.transformerNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { selectedZone = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close")
                            }
                        }

                        InfoChip(
                            text = when (status) {
                                PowerStatus.ON -> stringResource(R.string.power_on)
                                PowerStatus.OFF -> stringResource(R.string.power_off)
                                PowerStatus.UNKNOWN -> stringResource(R.string.unknown_status)
                            },
                            containerColor = statusColor.copy(alpha = 0.14f),
                            contentColor = statusColor
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PowerBlueDark)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Use this map to compare nearby zones before irrigation planning.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { selectedZone = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PowerBlueDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Close details", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
