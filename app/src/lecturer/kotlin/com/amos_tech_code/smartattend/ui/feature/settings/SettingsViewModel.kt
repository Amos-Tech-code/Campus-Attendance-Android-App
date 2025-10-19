package com.amos_tech_code.smartattend.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _event = Channel<SettingsEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadSettings()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.LocationAccuracyChanged -> {
                updateLocationAccuracy(event.accuracy)
            }
            is SettingsUiEvent.DefaultDurationChanged -> {
                updateDefaultDuration(event.duration)
            }
            is SettingsUiEvent.DefaultRadiusChanged -> {
                updateDefaultRadius(event.radius)
            }
            is SettingsUiEvent.PushNotificationsChanged -> {
                updatePushNotifications(event.enabled)
            }
            is SettingsUiEvent.EmailNotificationsChanged -> {
                updateEmailNotifications(event.enabled)
            }
            is SettingsUiEvent.DeviceChangeAlertsChanged -> {
                updateDeviceChangeAlerts(event.enabled)
            }
            is SettingsUiEvent.DeviceVerificationChanged -> {
                updateDeviceVerification(event.enabled)
            }
            is SettingsUiEvent.LocationRequirementChanged -> {
                updateLocationRequirement(event.enabled)
            }
            SettingsUiEvent.ExportData -> {
                exportData()
            }
            SettingsUiEvent.ClearCache -> {
                clearCache()
            }
            SettingsUiEvent.SaveSettings -> {
                saveSettings()
            }
            SettingsUiEvent.ResetToDefaults -> {
                resetToDefaults()
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Simulate loading settings from local storage or backend
                delay(800)

                // Load default settings (in real app, load from SharedPreferences or backend)
                val defaultSettings = getDefaultSettings()
                _state.update { defaultSettings.copy(isLoading = false) }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _event.send(SettingsEvent.ShowErrorMessage("Failed to load settings: ${e.message}"))
            }
        }
    }

    private fun updateLocationAccuracy(accuracy: Int) {
        _state.update { it.copy(locationAccuracy = accuracy) }
    }

    private fun updateDefaultDuration(duration: Int) {
        _state.update { it.copy(defaultDuration = duration) }
    }

    private fun updateDefaultRadius(radius: Int) {
        _state.update { it.copy(defaultRadius = radius) }
    }

    private fun updatePushNotifications(enabled: Boolean) {
        _state.update { it.copy(pushNotifications = enabled) }
    }

    private fun updateEmailNotifications(enabled: Boolean) {
        _state.update { it.copy(emailNotifications = enabled) }
    }

    private fun updateDeviceChangeAlerts(enabled: Boolean) {
        _state.update { it.copy(deviceChangeAlerts = enabled) }
    }

    private fun updateDeviceVerification(enabled: Boolean) {
        _state.update { it.copy(requireDeviceVerification = enabled) }
    }

    private fun updateLocationRequirement(enabled: Boolean) {
        _state.update { it.copy(requireLocation = enabled) }
    }

    private fun exportData() {
        viewModelScope.launch {
            _state.update { it.copy(isExportingData = true) }

            try {
                // Simulate export process
                delay(2000)

                // In real app, this would:
                // 1. Generate CSV/PDF report
                // 2. Save to device storage
                // 3. Share via intent

                _state.update { it.copy(isExportingData = false) }
                _event.send(SettingsEvent.DataExported("attendance_report_${System.currentTimeMillis()}.csv"))
                _event.send(SettingsEvent.ShowSuccessMessage("Data exported successfully"))

            } catch (e: Exception) {
                _state.update { it.copy(isExportingData = false) }
                _event.send(SettingsEvent.ShowErrorMessage("Failed to export data: ${e.message}"))
            }
        }
    }

    private fun clearCache() {
        viewModelScope.launch {
            _state.update { it.copy(isClearingCache = true) }

            try {
                // Simulate cache clearing process
                delay(1500)

                // In real app, this would:
                // 1. Clear image cache
                // 2. Clear temporary files
                // 3. Clear local database cache (but not user data)

                _state.update { it.copy(isClearingCache = false) }
                _event.send(SettingsEvent.ShowSuccessMessage("Cache cleared successfully"))

            } catch (e: Exception) {
                _state.update { it.copy(isClearingCache = false) }
                _event.send(SettingsEvent.ShowErrorMessage("Failed to clear cache: ${e.message}"))
            }
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            try {
                // Simulate saving to backend/local storage
                delay(1000)

                val currentState = _state.value

                // In real app, save to SharedPreferences or backend API
                saveSettingsToStorage(currentState)

                _state.update { it.copy(isSaving = false) }
                _event.send(SettingsEvent.SettingsUpdated)
                _event.send(SettingsEvent.ShowSuccessMessage("Settings saved successfully"))

            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                _event.send(SettingsEvent.ShowErrorMessage("Failed to save settings: ${e.message}"))
            }
        }
    }

    private fun resetToDefaults() {
        viewModelScope.launch {
            _state.update { it.copy(isResetting = true) }

            try {
                // Simulate reset process
                delay(800)

                val defaultSettings = getDefaultSettings()
                _state.update { defaultSettings.copy(isResetting = false) }

                _event.send(SettingsEvent.ShowSuccessMessage("Settings reset to defaults"))

            } catch (e: Exception) {
                _state.update { it.copy(isResetting = false) }
                _event.send(SettingsEvent.ShowErrorMessage("Failed to reset settings: ${e.message}"))
            }
        }
    }

    // Helper functions
    private fun getDefaultSettings(): SettingsState {
        return SettingsState(
            locationAccuracy = 1, // Medium (25m)
            defaultDuration = 1, // 30 minutes
            defaultRadius = 50,
            pushNotifications = true,
            emailNotifications = true,
            deviceChangeAlerts = true,
            requireDeviceVerification = true,
            requireLocation = true,
            appVersion = "2.1.0",
            isLoading = false,
            isSaving = false,
            isExportingData = false,
            isClearingCache = false,
            isResetting = false
        )
    }

    private fun saveSettingsToStorage(settings: SettingsState) {
        // In real app, save to SharedPreferences or backend
        // For now, we'll just simulate the save operation
    }
}

