package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    private val detectionEnabledState = MutableStateFlow(true)
    private val privacyModeState = MutableStateFlow(true)

    val detectionEnabled: StateFlow<Boolean> = detectionEnabledState
    val privacyMode: StateFlow<Boolean> = privacyModeState

    fun setDetectionEnabled(enabled: Boolean) {
        detectionEnabledState.value = enabled
    }

    fun setPrivacyMode(enabled: Boolean) {
        privacyModeState.value = enabled
    }
}
