package com.gramaurja.presentation.screens.history

import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.gramaurja.R
import com.gramaurja.data.model.PowerHistory
import com.gramaurja.presentation.components.AppHeaderCard
import com.gramaurja.presentation.components.BottomNavBar
import com.gramaurja.presentation.components.InfoChip
import com.gramaurja.presentation.components.SectionCard
import com.gramaurja.presentation.theme.PowerBlueMedium
import com.gramaurja.presentation.theme.PowerBlueSurface
import com.gramaurja.presentation.theme.PowerRedMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
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
                title = stringResource(R.string.power_history),
                subtitle = if (uiState.zoneName.isBlank()) "Track recent outages and recoveries." else uiState.zoneName,
                icon = Icons.Filled.BarChart
            )

            when {
                uiState.isLoading && uiState.history.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PowerBlueMedium)
                    }
                }

                !uiState.isLoading && uiState.history.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        SectionCard(title = "No updates yet", subtitle = "Status changes will appear here as users report them.") {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.BarChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionCard(
                                title = stringResource(R.string.last_24h),
                                subtitle = "A stepped view of recent electricity changes."
                            ) {
                                PowerLineChart(history = uiState.history)
                            }
                        }

                        item {
                            SectionCard(title = "Recent events", subtitle = "Latest reported power changes in your zone.") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    uiState.history.take(30).forEach { entry ->
                                        HistoryEntryRow(entry = entry)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerLineChart(history: List<PowerHistory>) {
    val entries = remember(history) {
        history.sortedBy { it.timestamp }.mapIndexed { index, entry ->
            Entry(index.toFloat(), if (entry.status == "ON") 1f else 0f)
        }
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(false)
                setScaleEnabled(false)
                legend.isEnabled = false
                setViewPortOffsets(24f, 16f, 24f, 24f)
                setBackgroundColor(AndroidColor.TRANSPARENT)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = AndroidColor.GRAY
                xAxis.setDrawGridLines(false)

                axisLeft.apply {
                    textColor = AndroidColor.GRAY
                    granularity = 1f
                    axisMinimum = -0.1f
                    axisMaximum = 1.1f
                    setDrawGridLines(true)
                }
                axisRight.isEnabled = false

                val dataSet = LineDataSet(entries, "Power").apply {
                    color = PowerBlueMedium.toArgb()
                    setCircleColor(PowerBlueMedium.toArgb())
                    circleHoleColor = AndroidColor.WHITE
                    circleRadius = 3.6f
                    lineWidth = 2.8f
                    mode = LineDataSet.Mode.STEPPED
                    valueTextSize = 0f
                    fillColor = PowerBlueSurface.toArgb()
                    setDrawFilled(true)
                    fillAlpha = 100
                }

                data = LineData(dataSet)
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}

@Composable
private fun HistoryEntryRow(entry: PowerHistory) {
    val isOn = entry.status == "ON"
    val color = if (isOn) PowerBlueMedium else PowerRedMedium
    val timestampFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOn) PowerBlueSurface else color.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                color = color.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isOn) Icons.Filled.ElectricBolt else Icons.Filled.PowerOff,
                        contentDescription = null,
                        tint = color
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOn) stringResource(R.string.power_on) else stringResource(R.string.power_off),
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timestampFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            InfoChip(
                text = if (isOn) stringResource(R.string.power_on_label) else stringResource(R.string.power_off_label),
                containerColor = color.copy(alpha = 0.14f),
                contentColor = color
            )
        }
    }
}
