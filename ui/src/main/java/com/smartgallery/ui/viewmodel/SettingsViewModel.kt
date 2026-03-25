package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgallery.data.repository.PeopleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val peopleRepository: PeopleRepository
) : ViewModel() {
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

    fun rescanGallery() {
        viewModelScope.launch {
            peopleRepository.rescanGallery()
        }
    }

    fun clearAiData() {
        viewModelScope.launch {
            peopleRepository.clearAiData()
        }
    }
}
