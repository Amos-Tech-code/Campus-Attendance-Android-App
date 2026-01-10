package com.amos_tech_code.smartattend.data.local.shared_prefs

import android.content.Context
import androidx.core.content.edit
import com.amos_tech_code.smartattend.domain.request.DeviceInfo

class SmartAttendSession(context: Context) {

    private val prefs = context.getSharedPreferences("smart_attend_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_COMPLETE = "profile_complete"

        private const val KEY_ACADEMIC_SYNC_STATUS = "academic_sync_status"

        // ✅ Added key for student session
        private const val KEY_REG_NO = "reg_no"
        private const val KEY_TOKEN_CREATED_AT = "token_created_at"
        private const val TOKEN_VALIDITY_DAYS = 10
        // ✅ Added key for student device info
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_MODEL = "device_model"
        private const val KEY_DEVICE_OS = "device_os"
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


    fun saveStudentSession(
        token: String,
        name: String,
        regNo: String,
        deviceInfo: DeviceInfo
    ) {
        prefs.edit {
            putString(KEY_TOKEN, token)
                .putString(KEY_NAME, name)
                .putString(KEY_REG_NO, regNo)
                .putLong(KEY_TOKEN_CREATED_AT, System.currentTimeMillis())
                .putString(KEY_DEVICE_ID, deviceInfo.deviceId)
                .putString(KEY_DEVICE_MODEL, deviceInfo.model)
                .putString(KEY_DEVICE_OS, deviceInfo.os)
                .apply()
        }
    }

    fun getValidToken(): String? {
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

    // ✅ Extra helper getters for lecturer session
    fun getName(): String? = prefs.getString(KEY_NAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun isProfileComplete(): Boolean = prefs.getBoolean(KEY_PROFILE_COMPLETE, false)

    // Extra helper getters for student session
    fun getRegNo() : String? = prefs.getString(KEY_REG_NO, null)
    fun getDeviceId() : String? = prefs.getString(KEY_DEVICE_ID, null)
    fun getDeviceModel() : String? = prefs.getString(KEY_DEVICE_MODEL, null)
    fun getDeviceOs() : String? = prefs.getString(KEY_DEVICE_OS, null)


    // ✅ Check if user is logged in (token exists & not expired)
    fun isLoggedIn(): Boolean = getValidToken() != null

}
