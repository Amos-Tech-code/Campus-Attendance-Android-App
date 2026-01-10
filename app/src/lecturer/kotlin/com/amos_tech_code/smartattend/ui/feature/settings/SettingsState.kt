package com.amos_tech_code.smartattend.ui.feature.settings

// Settings Data Classes
data class SettingsState(
    // Attendance Settings
    val locationAccuracy: Int = 1, // 0: High (10m), 1: Medium (25m), 2: Low (50m)
    val defaultDuration: Int = 1, // 0: 15min, 1: 30min, 2: 45min, 3: 60min
    val defaultRadius: Int = 50, // meters

    // Notification Settings
    val pushNotifications: Boolean = true,
    val deviceChangeAlerts: Boolean = false,

    // Security Settings
    val requireDeviceVerification: Boolean = true,
    val requireLocation: Boolean = true,

    // App Information
    val appVersion: String = "1.0.0",

    // Loading States
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isExportingData: Boolean = false,
    val isClearingCache: Boolean = false,
    val isResetting: Boolean = false,

    // Error State
    val errorMessage: String? = null
)