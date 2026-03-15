package com.amos_tech_code.smartattend.data.local.shared_prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.ui.feature.settings.SettingsState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClassTrackProSession(context: Context) : SessionProvider {

    private val prefs = context.getSharedPreferences("class_track_pro_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_COMPLETE = "profile_complete"
        private const val KEY_PROFILE_CREATED_AT = "profile_created_at"
        private const val KEY_TOKEN_CREATED_AT = "token_created_at"
        private const val TOKEN_VALIDITY_DAYS = 10

        private const val KEY_FCM_TOKEN = "fcm_token"

        // --- SYNC KEYS ---
        private const val KEY_ACADEMIC_SYNC_STATUS = "academic_sync_status"
        private const val KEY_ATTENDANCE_SESSION_HISTORY_SYNC_STATUS = "attendance_session_history_sync_status"
        private const val KEY_EXPORT_RECORD_SYNC_STATUS = "export_record_sync_status"

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
        isProfileComplete: Boolean,
        createdAt: String? = null
    ) {
        prefs.edit {
            putString(KEY_TOKEN, token)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_PROFILE_COMPLETE, isProfileComplete)
                .putString(KEY_PROFILE_CREATED_AT, createdAt)
                .putLong(KEY_TOKEN_CREATED_AT, System.currentTimeMillis())
        }
    }

    fun saveName(name: String) {
        prefs.edit { putString(KEY_NAME, name) }
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

    fun setAttendanceSessionHistorySyncStatus(isSynced: Boolean) {
        prefs.edit {
            putBoolean(KEY_ATTENDANCE_SESSION_HISTORY_SYNC_STATUS, isSynced)
            apply()
        }
    }

    fun getAttendanceSessionHistorySyncStatus(): Boolean {
        return prefs.getBoolean(KEY_ATTENDANCE_SESSION_HISTORY_SYNC_STATUS, false)
    }

    fun setExportRecordSyncStatus(isSynced: Boolean) {
        prefs.edit {
            putBoolean(KEY_EXPORT_RECORD_SYNC_STATUS, isSynced)
            apply()
        }
    }

    override fun saveFcmToken(token: String) {
        prefs.edit {
            putString(KEY_FCM_TOKEN, token)
        }
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
    fun getProfileCreatedAt(): String? = prefs.getString(KEY_PROFILE_CREATED_AT, null)
    fun getNameFlow(): Flow<String> = callbackFlow {
        // Emit current value immediately
        trySend(prefs.getString(KEY_NAME, "") ?: "")

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                if (key == KEY_NAME) {
                    trySend(sharedPrefs.getString(KEY_NAME, "") ?: "")
                }
            }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // Check if user is logged in (token exists & not expired)
    fun isLoggedIn(): Boolean = getValidToken() != null


    /**
     * Settings related methods
     */
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
