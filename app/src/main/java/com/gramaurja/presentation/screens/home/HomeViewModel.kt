package com.gramaurja.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gramaurja.data.local.PreferencesManager
import com.gramaurja.data.model.PowerHistory
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.data.model.StatusUpdate
import com.gramaurja.domain.usecase.GetPowerHistoryUseCase
import com.gramaurja.domain.usecase.GetZoneStatusUseCase
import com.gramaurja.domain.usecase.PredictPowerReturnUseCase
import com.gramaurja.domain.usecase.UpdatePowerStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val zoneId: String = "",
    val zoneName: String = "",
    val currentStatus: PowerStatus = PowerStatus.UNKNOWN,
    val timestamp: Long = 0L,
    val confirmCount: Int = 0,
    val freshnessText: String = "",
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val predictionText: String = "",
    val isOffline: Boolean = false,
    val userId: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getZoneStatus: GetZoneStatusUseCase,
    private val updatePowerStatus: UpdatePowerStatusUseCase,
    private val getPowerHistory: GetPowerHistoryUseCase,
    private val predictPowerReturn: PredictPowerReturnUseCase,
    private val prefs: PreferencesManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var observerJob: Job? = null
    private var freshnessJob: Job? = null
    private var historyList: List<PowerHistory> = emptyList()

    init {
        ensureAnonymousAuth()
        observeSelectedZone()
        startFreshnessUpdater()
    }

    private fun ensureAnonymousAuth() {
        viewModelScope.launch {
            val currentUid = auth.currentUser?.uid
            if (currentUid != null) {
                _uiState.update { it.copy(userId = currentUid) }
                prefs.saveUserId(currentUid)
            } else {
                auth.signInAnonymously()
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: return@addOnSuccessListener
                        viewModelScope.launch {
                            prefs.saveUserId(uid)
                            _uiState.update { it.copy(userId = uid) }
                        }
                    }
                    .addOnFailureListener { e -> 
                        Timber.e(e, "Anonymous auth failed")
                        // FALLBACK: Use a guest ID so the app still works!
                        _uiState.update { it.copy(userId = "guest_${System.currentTimeMillis() % 1000}") }
                    }
            }
        }
    }

    private fun observeSelectedZone() {
        viewModelScope.launch {
            var lastZoneId: String? = null
            combine(prefs.selectedZoneId, prefs.selectedZoneName) { id, name ->
                id to name
            }.collect { (zoneId, zoneName) ->
                if (zoneId.isNullOrBlank()) {
                    _uiState.update { 
                        it.copy(
                            zoneId = "", 
                            zoneName = "", 
                            currentStatus = PowerStatus.UNKNOWN,
                            errorMessage = null 
                        ) 
                    }
                    observerJob?.cancel()
                    lastZoneId = null
                    return@collect
                }

                val displayName = zoneName ?: zoneId
                _uiState.update { it.copy(zoneId = zoneId, zoneName = displayName) }

                // If zoneId hasn't changed, don't restart the observer
                if (zoneId == lastZoneId) return@collect
                lastZoneId = zoneId

                // Load cached status immediately
                val cachedStatus = prefs.cachedStatus.first()
                val cachedTs = prefs.cachedTimestamp.first()
                if (cachedStatus != PowerStatus.UNKNOWN) {
                    _uiState.update { it.copy(currentStatus = cachedStatus, timestamp = cachedTs) }
                }

                startObservingLiveStatus(zoneId)
                startObservingHistory(zoneId)
            }
        }
    }

    private fun startObservingLiveStatus(zoneId: String) {
        observerJob?.cancel()
        observerJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Watchdog: Hide error by default
            val watchdog = launch {
                delay(10000) 
                if (_uiState.value.isLoading) {
                    _uiState.update { it.copy(errorMessage = "Connecting... data is available all the time once synced.") }
                }
            }

            try {
                // Collect status with retry
                getZoneStatus(zoneId)
                    .catch { e ->
                        Timber.e(e, "Firebase flow failed, retrying...")
                        delay(2000)
                        startObservingLiveStatus(zoneId)
                    }
                    .collect { update ->
                        watchdog.cancel()
                        _uiState.update { state ->
                            // Continuous Presence: Only update if we have real data
                            // This prevents UI from clearing if RTDB briefly sends empty snapshot
                            if (update.status != PowerStatus.UNKNOWN) {
                                state.copy(
                                    currentStatus = update.status,
                                    timestamp = update.timestamp,
                                    confirmCount = update.confirmCount,
                                    isLoading = false,
                                    isOffline = false,
                                    errorMessage = null
                                )
                            } else {
                                state.copy(isLoading = false)
                            }
                        }
                        if (update.status != PowerStatus.UNKNOWN) {
                            prefs.cacheStatus(update.status, update.timestamp)
                        }
                    }
            } catch (e: Exception) {
                watchdog.cancel()
                Timber.e(e, "Error in status observation")
                delay(3000)
                startObservingLiveStatus(zoneId)
            }
        }
    }

    private fun startObservingHistory(zoneId: String) {
        viewModelScope.launch {
            getPowerHistory(zoneId).collect { history ->
                historyList = history
            }
        }
    }

    private fun startFreshnessUpdater() {
        freshnessJob = viewModelScope.launch {
            while (true) {
                val ts = _uiState.value.timestamp
                if (ts > 0) {
                    _uiState.update { it.copy(freshnessText = formatFreshness(ts)) }
                }
                delay(30_000)
            }
        }
    }

    fun updateStatus(newStatus: PowerStatus) {
        val state = _uiState.value
        
        if (state.zoneId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select a village first") }
            return
        }

        if (state.userId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Logging in... please try again in a moment") }
            ensureAnonymousAuth()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }

            val result = updatePowerStatus(
                zoneId = state.zoneId,
                zoneName = state.zoneName,
                status = newStatus,
                userId = state.userId
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            successMessage = "Status updated to ${newStatus.displayName()}"
                        )
                    }
                    if (newStatus == PowerStatus.OFF) {
                        fetchPrediction(newStatus)
                    }
                    clearSuccessAfterDelay()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = error.message ?: "Update failed"
                        )
                    }
                }
            )
        }
    }

    fun confirmStatus() {
        Timber.d("User confirmed current status")
    }

    fun askAI() {
        val status = _uiState.value.currentStatus
        if (status == PowerStatus.UNKNOWN) {
            _uiState.update { it.copy(predictionText = "Power status is unknown. Update it first to get an AI prediction.") }
            return
        }
        _uiState.update { it.copy(predictionText = "🤖 Analyzing power patterns...") }
        fetchPrediction(status)
    }

    private fun fetchPrediction(status: PowerStatus) {
        viewModelScope.launch {
            try {
                val prediction = predictPowerReturn(historyList, status, _uiState.value.zoneName)
                _uiState.update { it.copy(predictionText = prediction) }
            } catch (e: Exception) {
                Timber.w(e, "Prediction failed")
            }
        }
    }

    private fun clearSuccessAfterDelay() {
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    private fun formatFreshness(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = minutes / 60
        return when {
            diff < 60_000 -> "Updated just now"
            minutes < 60 -> "Updated ${minutes} min ago"
            hours < 24 -> "Updated ${hours}h ago"
            else -> {
                val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                "Updated ${sdf.format(Date(timestamp))}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observerJob?.cancel()
        freshnessJob?.cancel()
    }
}
