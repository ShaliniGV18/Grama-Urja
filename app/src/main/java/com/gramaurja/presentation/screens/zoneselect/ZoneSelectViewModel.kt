package com.gramaurja.presentation.screens.zoneselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramaurja.data.local.PreferencesManager
import com.gramaurja.data.model.Zone
import com.gramaurja.data.repository.ZoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.*

data class ZoneSelectUiState(
    val zones: List<Zone> = emptyList(),
    val filteredZones: List<Zone> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val locationError: String? = null,
    val detectedNearestZone: Zone? = null
)

@HiltViewModel
class ZoneSelectViewModel @Inject constructor(
    private val repository: ZoneRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZoneSelectUiState())
    val uiState: StateFlow<ZoneSelectUiState> = _uiState.asStateFlow()

    init {
        loadZones()
    }

    private fun loadZones() {
        val zones = repository.getAllZones()
        _uiState.update { it.copy(zones = zones, filteredZones = zones) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredZones = if (query.isBlank()) state.zones
                else state.zones.filter { zone ->
                    zone.name.contains(query, ignoreCase = true) ||
                            zone.district.contains(query, ignoreCase = true)
                }
            )
        }
    }

    fun detectNearestZone(lat: Double, lng: Double) {
        val zones = _uiState.value.zones
        val nearest = zones.minByOrNull { zone ->
            haversineDistance(lat, lng, zone.lat, zone.lng)
        }
        _uiState.update { it.copy(detectedNearestZone = nearest, locationError = null) }
    }

    fun onLocationError(message: String) {
        _uiState.update { it.copy(locationError = message) }
    }

    fun selectZone(zone: Zone, onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.saveSelectedZone(zone.id, zone.name)
            Timber.d("Zone selected: ${zone.name}")
            onDone()
        }
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
