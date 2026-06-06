package com.gramaurja.presentation.screens.splash

import androidx.lifecycle.ViewModel
import com.gramaurja.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {
    suspend fun isZoneSelected(): Boolean {
        return !prefs.selectedZoneId.first().isNullOrBlank()
    }
}
