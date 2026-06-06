package com.gramaurja.presentation.screens.pumptimer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gramaurja.R
import com.gramaurja.presentation.components.AppHeaderCard
import com.gramaurja.presentation.components.BottomNavBar
import com.gramaurja.presentation.components.SectionCard
import com.gramaurja.presentation.theme.AmberLight
import com.gramaurja.presentation.theme.AmberWarning
import com.gramaurja.presentation.theme.PowerBlueDark
import com.gramaurja.presentation.theme.PowerBlueLight
import com.gramaurja.presentation.theme.PowerBlueMedium
import com.gramaurja.presentation.theme.PowerBlueSurface
import com.gramaurja.presentation.theme.PowerRedMedium

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun PumpTimerScreen(
    navController: NavController,
    viewModel: PumpTimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeaderCard(
                title = stringResource(R.string.pump_timer),
                subtitle = "Estimate irrigation duration from crop, field area, and pump size.",
                icon = Icons.Filled.Timer
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(title = "Field details", subtitle = "Enter your crop and pump details for a quick estimate.") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedCrop.displayName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(16.dp),
                                label = { Text(stringResource(R.string.select_crop)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                CropType.values().forEach { crop ->
                                    DropdownMenuItem(
                                        text = { Text(crop.displayName) },
                                        onClick = {
                                            viewModel.selectCrop(crop)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = uiState.areaText,
                                onValueChange = viewModel::onAreaChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Area (acres)") },
                                placeholder = { Text("2.5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = uiState.error != null && uiState.areaText.isBlank(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Filled.Landscape, contentDescription = null, tint = PowerBlueDark) }
                            )
                            OutlinedTextField(
                                value = uiState.pumpHpText,
                                onValueChange = viewModel::onPumpHpChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Pump (HP)") },
                                placeholder = { Text("5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = uiState.error != null && uiState.pumpHpText.isBlank(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        uiState.error?.let {
                            Text(it, color = PowerRedMedium, style = MaterialTheme.typography.bodyMedium)
                        }

                        Button(
                            onClick = viewModel::calculate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PowerBlueDark),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Filled.Calculate, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stringResource(R.string.calculate), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.showResult,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    uiState.resultHours?.let { hours ->
                        SectionCard(title = stringResource(R.string.recommended_duration), subtitle = "Estimated run time for the selected crop.") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                color = PowerBlueSurface
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.hours_format, hours),
                                        style = MaterialTheme.typography.displayLarge,
                                        color = PowerBlueDark,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val mins = ((hours % 1) * 60).toInt()
                                    val hrs = hours.toInt()
                                    Text(
                                        text = "$hrs h $mins min",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PowerBlueMedium
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = PowerBlueLight.copy(alpha = 0.35f))
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Info, contentDescription = null, tint = PowerBlueDark, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val hp = uiState.pumpHpText.toDoubleOrNull() ?: 5.0
                                        Text(
                                            text = "Assumes a ${hp.toInt()} HP pump at about ${(hp * 6000).toInt()} L/hr.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = AmberLight.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.pump_tip),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            color = AmberWarning,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
