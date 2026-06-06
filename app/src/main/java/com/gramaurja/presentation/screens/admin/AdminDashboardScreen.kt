package com.gramaurja.presentation.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gramaurja.R
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.data.model.StatusUpdate
import com.gramaurja.data.model.Zone
import com.gramaurja.presentation.components.AppHeaderCard
import com.gramaurja.presentation.components.BottomNavBar
import com.gramaurja.presentation.theme.PowerBlueDark
import com.gramaurja.presentation.theme.PowerBlueMedium
import com.gramaurja.presentation.theme.PowerRedDark
import com.gramaurja.presentation.theme.PowerRedMedium
import com.gramaurja.presentation.theme.UnknownGrey

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = stringResource(R.string.admin_dashboard),
                subtitle = "Monitor and override status across all configured zones."
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val onCount = uiState.zoneStatuses.values.count { it.status == PowerStatus.ON }
                val offCount = uiState.zoneStatuses.values.count { it.status == PowerStatus.OFF }
                StatChip("ON", onCount.toString(), PowerBlueMedium)
                StatChip("OFF", offCount.toString(), PowerRedMedium)
                StatChip("Zones", uiState.zones.size.toString(), UnknownGrey)
            }

            if (uiState.isLoading && uiState.zoneStatuses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PowerBlueMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.zones) { zone ->
                        val status = uiState.zoneStatuses[zone.id]
                        AdminZoneCard(
                            zone = zone,
                            statusUpdate = status,
                            onOverride = { newStatus -> viewModel.overrideStatus(zone, newStatus) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = color)
        Text(label, fontSize = 12.sp, color = color.copy(alpha = 0.85f))
    }
}

@Composable
private fun AdminZoneCard(
    zone: Zone,
    statusUpdate: StatusUpdate?,
    onOverride: (PowerStatus) -> Unit
) {
    val status = statusUpdate?.status ?: PowerStatus.UNKNOWN
    val statusColor = when (status) {
        PowerStatus.ON -> PowerBlueMedium
        PowerStatus.OFF -> PowerRedMedium
        PowerStatus.UNKNOWN -> UnknownGrey
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(zone.district, fontSize = 12.sp, color = UnknownGrey)
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = statusColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        status.name,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (statusUpdate != null && statusUpdate.confirmCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${statusUpdate.confirmCount} confirmations",
                    fontSize = 12.sp,
                    color = UnknownGrey
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                stringResource(R.string.override_status),
                fontSize = 12.sp,
                color = UnknownGrey,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onOverride(PowerStatus.ON) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PowerBlueDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Set ON", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onOverride(PowerStatus.OFF) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PowerRedDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PowerOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Set OFF", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
