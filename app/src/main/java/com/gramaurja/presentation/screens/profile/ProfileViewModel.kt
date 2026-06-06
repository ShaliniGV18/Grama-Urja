package com.gramaurja.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gramaurja.data.local.PreferencesManager
import com.gramaurja.data.repository.ZoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userId: String = "",
    val zoneName: String = "",
    val totalConfirmations: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val auth: FirebaseAuth,
    private val repository: ZoneRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Observe UID
            launch {
                val userId = auth.currentUser?.uid ?: ""
                _uiState.update { it.copy(userId = userId) }
            }

            // Observe Zone Name
            prefs.selectedZoneName.collect { zoneName ->
                _uiState.update { 
                    it.copy(
                        zoneName = zoneName ?: "No zone selected",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            prefs.clearAll()
            auth.signOut()
            // This should ideally trigger a navigation to splash or zone select
        }
    }
}
