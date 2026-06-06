package com.gramaurja.presentation.screens.home

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gramaurja.R
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.presentation.components.AppHeaderCard
import com.gramaurja.presentation.components.BottomNavBar
import com.gramaurja.presentation.components.InfoChip
import com.gramaurja.presentation.components.SectionCard
import com.gramaurja.presentation.navigation.Screen
import com.gramaurja.presentation.theme.AmberLight
import com.gramaurja.presentation.theme.AmberWarning
import com.gramaurja.presentation.theme.FolkOrange
import com.gramaurja.presentation.theme.PowerBlueDark
import com.gramaurja.presentation.theme.PowerBlueMedium
import com.gramaurja.presentation.theme.PowerBlueSurface
import com.gramaurja.presentation.theme.PowerRedDark
import com.gramaurja.presentation.theme.PowerRedMedium
import com.gramaurja.presentation.theme.PowerRedSurface
import com.gramaurja.presentation.theme.UnknownGrey

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = true) {}

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController) },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = uiState.errorMessage == data.visuals.message
                Snackbar(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (isError) PowerRedMedium else PowerBlueMedium,
                    contentColor = Color.White,
                    action = {
                        if (isError && data.visuals.actionLabel != null) {
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text(data.visuals.actionLabel ?: "OK", color = Color.White)
                            }
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeaderCard(
                title = stringResource(R.string.app_name),
                subtitle = uiState.zoneName.ifBlank {
                    if (uiState.isOffline) stringResource(R.string.offline_mode) else stringResource(R.string.app_tagline)
                },
                icon = Icons.Filled.ElectricBolt,
                actions = {
                    HeaderActionButtons(
                        onToggleLanguage = {
                            val currentLocales = AppCompatDelegate.getApplicationLocales()
                            val isKannada = currentLocales.toLanguageTags() == "kn"
                            val newLocale = if (isKannada) "en" else "kn"
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                        },
                        onChangeZone = {
                            navController.navigate(Screen.ZoneSelect.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    )
                }
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.zoneId.isBlank()) {
                    NoZoneSelectedContent(onSelect = { navController.navigate(Screen.ZoneSelect.route) })
                } else {
                    SectionCard(
                        title = stringResource(R.string.power_status),
                        subtitle = uiState.zoneName.ifBlank { stringResource(R.string.your_zone) }
                    ) {
                        if (uiState.isLoading && uiState.currentStatus == PowerStatus.UNKNOWN) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = FolkOrange)
                            }
                        } else {
                            PowerStatusCard(
                                status = uiState.currentStatus,
                                freshnessText = uiState.freshnessText,
                                confirmCount = uiState.confirmCount,
                                isUpdating = uiState.isUpdating || uiState.isLoading,
                                onPowerOn = { viewModel.updateStatus(PowerStatus.ON) },
                                onPowerOff = { viewModel.updateStatus(PowerStatus.OFF) },
                                onConfirm = { viewModel.confirmStatus() },
                                onAskAI = { viewModel.askAI() }
                            )
                        }
                    }

                    if (uiState.predictionText.isNotBlank()) {
                        PredictionBanner(prediction = uiState.predictionText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HeaderActionButtons(
    onToggleLanguage: () -> Unit,
    onChangeZone: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        IconButton(
            onClick = onToggleLanguage,
            modifier = Modifier.background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
        ) {
            Icon(Icons.Filled.Language, contentDescription = "Language", tint = Color.White)
        }
        IconButton(
            onClick = onChangeZone,
            modifier = Modifier.background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
        ) {
            Icon(Icons.Filled.EditLocation, contentDescription = stringResource(R.string.change_zone), tint = Color.White)
        }
    }
}

@Composable
private fun NoZoneSelectedContent(onSelect: () -> Unit) {
    SectionCard(title = stringResource(R.string.select_zone), subtitle = "Choose a village to start seeing live electricity updates.") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOff,
                    contentDescription = null,
                    tint = UnknownGrey,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Select your village to see current status, confirmations, and pump advice.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FolkOrange)
            ) {
                Text(text = "Select Village")
            }
        }
    }
}

@Composable
private fun PowerStatusCard(
    status: PowerStatus,
    freshnessText: String,
    confirmCount: Int,
    isUpdating: Boolean,
    onPowerOn: () -> Unit,
    onPowerOff: () -> Unit,
    onConfirm: () -> Unit,
    onAskAI: () -> Unit
) {
    val statusColor = when (status) {
        PowerStatus.ON -> PowerBlueMedium
        PowerStatus.OFF -> PowerRedMedium
        PowerStatus.UNKNOWN -> UnknownGrey
    }
    val statusSurface = when (status) {
        PowerStatus.ON -> PowerBlueSurface
        PowerStatus.OFF -> PowerRedSurface
        PowerStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
    }
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == PowerStatus.UNKNOWN) 1f else 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "statusScale"
    )

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = statusSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(pulseScale)
                        .background(statusColor.copy(alpha = 0.14f), CircleShape)
                        .border(2.dp, statusColor.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (status) {
                            PowerStatus.ON -> Icons.Filled.ElectricBolt
                            PowerStatus.OFF -> Icons.Filled.PowerOff
                            PowerStatus.UNKNOWN -> Icons.Filled.QuestionMark
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (status) {
                        PowerStatus.ON -> stringResource(R.string.power_on)
                        PowerStatus.OFF -> stringResource(R.string.power_off)
                        PowerStatus.UNKNOWN -> stringResource(R.string.unknown_status)
                    },
                    style = MaterialTheme.typography.displayLarge,
                    color = statusColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (freshnessText.isNotBlank()) {
                        InfoChip(text = freshnessText, containerColor = Color.White.copy(alpha = 0.6f), contentColor = statusColor)
                    }
                }
                if (confirmCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.confirmed_by, confirmCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.tap_to_update),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusButton(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.power_on),
                icon = Icons.Filled.ElectricBolt,
                containerColor = PowerBlueDark,
                enabled = !isUpdating && status != PowerStatus.ON,
                onClick = onPowerOn
            )
            StatusButton(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.power_off),
                icon = Icons.Filled.PowerOff,
                containerColor = PowerRedDark,
                enabled = !isUpdating && status != PowerStatus.OFF,
                onClick = onPowerOff
            )
        }

        OutlinedButton(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = statusColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.confirm_status), color = statusColor, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onAskAI,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Get Return Prediction", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusButton(
    modifier: Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(62.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PredictionBanner(prediction: String) {
    SectionCard(
        title = stringResource(R.string.prediction_label),
        subtitle = "Best estimate based on recent village updates."
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AmberLight.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = prediction,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = AmberWarning.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Use this as guidance, then confirm with nearby users.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
