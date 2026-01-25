package com.amos_tech_code.smartattend.data.local.shared_prefs

import android.content.Context
import androidx.core.content.edit
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.ui.feature.settings.SettingsState

class ClassTrackProSession(context: Context) : SessionProvider {

    private val prefs = context.getSharedPreferences("class_track_pro_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_COMPLETE = "profile_complete"
        private const val KEY_ACADEMIC_SYNC_STATUS = "academic_sync_status"
        private const val KEY_TOKEN_CREATED_AT = "token_created_at"
        private const val TOKEN_VALIDITY_DAYS = 10

        // ---  SETTINGS KEYS ---
        private const val KEY_LOCATION_ACCURACY = "setting_location_accuracy"
        private const val KEY_DEFAULT_DURATION = "setting_default_duration"
        private const val KEY_DEFAULT_RADIUS = "setting_default_radius"
        private const val KEY_PUSH_NOTIFICATIONS = "setting_push_notifications"
        private const val KEY_DEVICE_CHANGE_ALERTS = "setting_device_change_alerts"
        private const val KEY_REQUIRE_DEVICE_VERIFICATION = "setting_require_device_verification"
        private const val KEY_REQUIRE_LOCATION = "setting_require_location"

    }

    fun saveLecturerSession(
        token: String,
        name: String,
        email: String,
        isProfileComplete: Boolean
    ) {
        prefs.edit {
            putString(KEY_TOKEN, token)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_PROFILE_COMPLETE, isProfileComplete)
                .putLong(KEY_TOKEN_CREATED_AT, System.currentTimeMillis())
        }
    }

    override fun saveName(name: String) {
        prefs.edit { putString(KEY_NAME, name) }
    }

    override fun saveRegistrationNumber(registrationNo: String) {
        /**
         * No saving registrationNumber
         * Just overriding a method for the interface
         */
    }

    fun setSetupComplete(isSetupComplete: Boolean) {
        prefs.edit {
            putBoolean(KEY_PROFILE_COMPLETE, isSetupComplete)
            apply()
        }
    }

    fun setAcademicSyncStatus(isSynced: Boolean) {
        prefs.edit {
            putBoolean(KEY_ACADEMIC_SYNC_STATUS, isSynced)
            apply()
        }
    }

    fun getAcademicSyncStatus(): Boolean {
        return prefs.getBoolean(KEY_ACADEMIC_SYNC_STATUS, false)
    }

    override fun getValidToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val createdAt = prefs.getLong(KEY_TOKEN_CREATED_AT, 0L)

        val expiryTime = createdAt + (TOKEN_VALIDITY_DAYS * 24 * 60 * 60 * 1000)
        return if (System.currentTimeMillis() <= expiryTime) {
            token
        } else {
            null // expired
        }
    }

    fun clearSession() {
        prefs.edit { clear() }
    }

    // Extra helper getters for lecturer session
    fun getName(): String? = prefs.getString(KEY_NAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun isProfileComplete(): Boolean = prefs.getBoolean(KEY_PROFILE_COMPLETE, false)

    // Check if user is logged in (token exists & not expired)
    fun isLoggedIn(): Boolean = getValidToken() != null

    fun saveSettings(settings: SettingsState) {
        prefs.edit {
            putInt(KEY_LOCATION_ACCURACY, settings.locationAccuracy)
            putInt(KEY_DEFAULT_DURATION, settings.defaultDuration)
            putInt(KEY_DEFAULT_RADIUS, settings.defaultRadius)
            putBoolean(KEY_PUSH_NOTIFICATIONS, settings.pushNotifications)
            putBoolean(KEY_DEVICE_CHANGE_ALERTS, settings.deviceChangeAlerts)
            putBoolean(KEY_REQUIRE_DEVICE_VERIFICATION, settings.requireDeviceVerification)
            putBoolean(KEY_REQUIRE_LOCATION, settings.requireLocation)
        }
    }

    fun getLocationAccuracy(): Int = prefs.getInt(KEY_LOCATION_ACCURACY, 1) // Default: Medium
    fun getDefaultDuration(): Int = prefs.getInt(KEY_DEFAULT_DURATION, 1) // Default: 30 min
    fun getDefaultRadius(): Int = prefs.getInt(KEY_DEFAULT_RADIUS, 50) // Default: 50m
    fun getPushNotifications(): Boolean = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
    fun getDeviceChangeAlerts(): Boolean = prefs.getBoolean(KEY_DEVICE_CHANGE_ALERTS, true)
    fun getRequireDeviceVerification(): Boolean = prefs.getBoolean(KEY_REQUIRE_DEVICE_VERIFICATION, true)
    fun getRequireLocation(): Boolean = prefs.getBoolean(KEY_REQUIRE_LOCATION, false)


}
