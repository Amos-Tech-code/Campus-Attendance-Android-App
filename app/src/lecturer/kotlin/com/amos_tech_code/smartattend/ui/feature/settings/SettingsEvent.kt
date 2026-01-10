package com.amos_tech_code.smartattend.ui.feature.settings

// App Events (for navigation, toasts, etc.)
sealed class SettingsEvent {
    data class ShowErrorMessage(val message: String) : SettingsEvent()
    data class ShowSuccessMessage(val message: String) : SettingsEvent()
    object SettingsUpdated : SettingsEvent()
    data class DataExported(val fileName: String) : SettingsEvent()
    object CacheCleared : SettingsEvent()
}

// UI Events (from user interactions)
sealed class SettingsUiEvent {
    data class LocationAccuracyChanged(val accuracy: Int) : SettingsUiEvent()
    data class DefaultDurationChanged(val duration: Int) : SettingsUiEvent()
    data class DefaultRadiusChanged(val radius: Int) : SettingsUiEvent()
    data class PushNotificationsChanged(val enabled: Boolean) : SettingsUiEvent()
    data class DeviceChangeAlertsChanged(val enabled: Boolean) : SettingsUiEvent()
    data class DeviceVerificationChanged(val enabled: Boolean) : SettingsUiEvent()
    data class LocationRequirementChanged(val enabled: Boolean) : SettingsUiEvent()
    object ExportData : SettingsUiEvent()
    object ClearCache : SettingsUiEvent()
    object SaveSettings : SettingsUiEvent()
    object ResetToDefaults : SettingsUiEvent()
}