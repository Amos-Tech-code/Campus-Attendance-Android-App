package com.amos_tech_code.smartattend.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val session: ClassTrackProSession
) : ViewModel() {

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
                // Load settings directly from the session
                _state.update {
                    it.copy(
                        isLoading = false,
                        locationAccuracy = session.getLocationAccuracy(),
                        defaultDuration = session.getDefaultDuration(),
                        defaultRadius = session.getDefaultRadius(),
                        pushNotifications = session.getPushNotifications(),
                        deviceChangeAlerts = session.getDeviceChangeAlerts(),
                        requireDeviceVerification = session.getRequireDeviceVerification(),
                        requireLocation = session.getRequireLocation()
                    )
                }
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
                // Save the current state to the session
                session.saveSettings(_state.value)
                delay(500) // Simulate save delay
                _state.update { it.copy(isSaving = false) }
                _event.send(SettingsEvent.SettingsUpdated)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                _event.send(SettingsEvent.ShowErrorMessage("Failed to save settings: ${e.message}"))
            }
        }
    }


    private fun resetToDefaults() {
        viewModelScope.launch {
            _state.update { it.copy(isResetting = true) }
            // Create a default state, save it, then update the UI
            val defaultSettings = SettingsState()
            session.saveSettings(defaultSettings)
            delay(500)
            _state.update { defaultSettings.copy(isResetting = false) }
            _event.send(SettingsEvent.ShowSuccessMessage("Settings reset to defaults"))
        }
    }

}

