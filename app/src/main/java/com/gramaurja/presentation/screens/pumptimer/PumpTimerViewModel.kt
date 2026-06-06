package com.gramaurja.presentation.screens.pumptimer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class CropType(val displayName: String, val literPerAcrePerHour: Double) {
    RICE("Rice / Paddy", 1800.0),
    WHEAT("Wheat", 900.0),
    VEGETABLES("Vegetables", 600.0),
    SUGARCANE("Sugarcane", 2200.0),
    GROUNDNUT("Groundnut", 700.0),
    COTTON("Cotton", 800.0),
    MAIZE("Maize / Corn", 1200.0),
    MILLETS("Millets", 500.0),
    TOMATO("Tomato", 650.0),
    SUNFLOWER("Sunflower", 550.0);

    /** Pump hours needed for given acres and pump HP (approx 6000 L/hr per HP) */
    fun calculateHours(acres: Double, pumpHp: Double): Double {
        val totalLiters = literPerAcrePerHour * acres
        val pumpFlowLitersPerHour = pumpHp * 6000.0
        return totalLiters / pumpFlowLitersPerHour
    }
}

data class PumpTimerUiState(
    val selectedCrop: CropType = CropType.RICE,
    val areaText: String = "",
    val pumpHpText: String = "5", // Default to 5 HP
    val resultHours: Double? = null,
    val showResult: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PumpTimerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PumpTimerUiState())
    val uiState: StateFlow<PumpTimerUiState> = _uiState.asStateFlow()

    fun selectCrop(crop: CropType) {
        _uiState.update { it.copy(selectedCrop = crop, showResult = false) }
    }

    fun onAreaChange(text: String) {
        _uiState.update { it.copy(areaText = text, showResult = false, error = null) }
    }

    fun onPumpHpChange(text: String) {
        _uiState.update { it.copy(pumpHpText = text, showResult = false, error = null) }
    }

    fun calculate() {
        val area = _uiState.value.areaText.toDoubleOrNull()
        val hp = _uiState.value.pumpHpText.toDoubleOrNull()
        
        if (area == null || area <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid area") }
            return
        }
        if (hp == null || hp <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid Pump HP") }
            return
        }
        
        val hours = _uiState.value.selectedCrop.calculateHours(area, hp)
        _uiState.update { it.copy(resultHours = hours, showResult = true, error = null) }
    }
}
