package com.gramaurja.presentation.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramaurja.data.model.StatusUpdate
import com.gramaurja.data.model.Zone
import com.gramaurja.data.repository.ZoneRepository
import com.gramaurja.domain.usecase.GetZoneStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import javax.inject.Inject

data class MapUiState(
    val zones: List<Zone> = emptyList(),
    val zoneStatuses: Map<String, StatusUpdate> = emptyMap(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: ZoneRepository,
    private val getZoneStatus: GetZoneStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadAllZonesAndStatuses()
    }

    private fun loadAllZonesAndStatuses() {
        val zones = repository.getAllZones()
        _uiState.update { it.copy(zones = zones, isLoading = true) }

        // Observe all zones for the map
        zones.forEach { zone ->
            viewModelScope.launch {
                try {
                    getZoneStatus(zone.id)
                        .timeout(10.seconds)
                        .catch { 
                            _uiState.update { state -> state.copy(isLoading = false) }
                        }
                        .collect { update ->
                            _uiState.update { state ->
                                state.copy(
                                    zoneStatuses = state.zoneStatuses + (zone.id to update),
                                    isLoading = false
                                )
                            }
                        }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
