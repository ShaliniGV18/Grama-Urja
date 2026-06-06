package com.gramaurja.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramaurja.data.local.PreferencesManager
import com.gramaurja.data.model.PowerHistory
import com.gramaurja.domain.usecase.GetPowerHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val history: List<PowerHistory> = emptyList(),
    val isLoading: Boolean = false,
    val zoneName: String = ""
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getPowerHistory: GetPowerHistoryUseCase,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            prefs.selectedZoneId.collectLatest { zoneId ->
                if (zoneId.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoading = false, history = emptyList()) }
                    return@collectLatest
                }

                val zoneName = prefs.selectedZoneName.first() ?: ""
                _uiState.update { it.copy(isLoading = true, zoneName = zoneName) }

                // Start background sync from RTDB (pipes to Room via repository)
                launch { 
                    getPowerHistory.observeLive(zoneId).collect { /* Repository handles Room update */ }
                }

                // Observe local Room exclusively - this is the "Continuous Presence" source
                getPowerHistory(zoneId).collect { history ->
                    _uiState.update { 
                        it.copy(
                            history = history, 
                            isLoading = false
                        ) 
                    }
                }
            }
        }
    }
}
