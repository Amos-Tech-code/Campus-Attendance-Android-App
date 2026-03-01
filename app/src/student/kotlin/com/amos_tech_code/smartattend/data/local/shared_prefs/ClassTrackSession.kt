package com.amos_tech_code.smartattend.data.local.shared_prefs

import android.content.Context
import androidx.core.content.edit
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import com.amos_tech_code.smartattend.domain.models.StudentFlow
import com.amos_tech_code.smartattend.domain.request.DeviceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClassTrackSession(context: Context) : SessionProvider {

    private val prefs = context.getSharedPreferences("class_track_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_NAME = "name"
        private const val KEY_REG_NO = "reg_no"
        private const val KEY_TOKEN_CREATED_AT = "token_created_at"
        private const val TOKEN_VALIDITY_DAYS = 10
        // Added key for student device info
        private const val KEY_DEVICE_STATUS = "device_status"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_MODEL = "device_model"
        private const val KEY_DEVICE_OS = "device_os"

        // SYNC STATUS
        private const val KEY_ATTENDANCE_SYNC_STATUS = "attendance_sync_status"
        private const val KEY_ENROLLMENT_SYNC_STATUS = "enrollment_sync_status"
        private const val KEY_ATTENDANCE_STATS_SYNC_STATUS = "attendance_stats_sync_status"
        private const val KEY_ATTENDANCE_STATS_SYNC_TIMESTAMP = "attendance_stats_sync_timestamp"

    }

    fun saveStudentSession(
        token: String,
        name: String,
        regNo: String,
        deviceStatus: DeviceStatus,
        deviceInfo: DeviceInfo
    ) {
        prefs.edit {
            putString(KEY_TOKEN, token)
                .putString(KEY_NAME, name)
                .putString(KEY_REG_NO, regNo)
                .putLong(KEY_TOKEN_CREATED_AT, System.currentTimeMillis())
                .putString(KEY_DEVICE_STATUS, deviceStatus.name)
                .putString(KEY_DEVICE_ID, deviceInfo.deviceId)
                .putString(KEY_DEVICE_MODEL, deviceInfo.model)
                .putString(KEY_DEVICE_OS, deviceInfo.os)
                .apply()
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

    fun saveName(name: String) {
        prefs.edit {
            putString(KEY_NAME, name)
        }
    }

    fun saveRegistrationNumber(registrationNo: String) {
        prefs.edit {
            putString(KEY_REG_NO, registrationNo)
        }
    }

    fun getName(): String? = prefs.getString(KEY_NAME, null)
    fun getRegNo() : String? = prefs.getString(KEY_REG_NO, null)

    fun getStudentFlow() : Flow<StudentFlow> = callbackFlow {
        trySend(StudentFlow(
            name = getName() ?: "",
            regNo = getRegNo() ?: ""
        ))

        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (key == KEY_NAME || key == KEY_REG_NO) {
                trySend(StudentFlow(
                    name = sharedPrefs.getString(KEY_NAME, "") ?: "",
                    regNo = sharedPrefs.getString(KEY_REG_NO, "") ?: ""
                ))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }

    }

    fun getDeviceId() : String? = prefs.getString(KEY_DEVICE_ID, null)
    fun getDeviceModel() : String? = prefs.getString(KEY_DEVICE_MODEL, null)
    private fun getDeviceOs() : String? = prefs.getString(KEY_DEVICE_OS, null)

    fun getDeviceStatus() : DeviceStatus {
        return when (prefs.getString(KEY_DEVICE_STATUS, null)) {
            DeviceStatus.ACTIVE.name -> DeviceStatus.ACTIVE
            DeviceStatus.PENDING.name -> DeviceStatus.PENDING
            DeviceStatus.REJECTED.name -> DeviceStatus.REJECTED
            else -> DeviceStatus.ACTIVE
        }
    }

    fun getStudentDeviceInfo() : DeviceInfo = DeviceInfo(
        deviceId = getDeviceId() ?: "",
        model = getDeviceModel() ?: "",
        os = getDeviceOs() ?: ""
    )

    // Check if user is logged in (token exists & not expired)
    fun isLoggedIn(): Boolean = getValidToken() != null

    fun setAttendanceSyncStatus(status: Boolean) {
        prefs.edit {
            putBoolean(KEY_ATTENDANCE_SYNC_STATUS, status)
        }

    }

    fun setEnrollmentSyncStatus(status: Boolean) {
        prefs.edit {
            putBoolean(KEY_ENROLLMENT_SYNC_STATUS, status)
        }
    }

    fun isEnrolmentSynced(): Boolean {
        return prefs.getBoolean(KEY_ENROLLMENT_SYNC_STATUS, false)
    }

    fun isAttendanceSynced(): Boolean {
        return prefs.getBoolean(KEY_ATTENDANCE_SYNC_STATUS, false)
    }

    fun setAttendanceStatsSyncStatus(status: Boolean) {
        prefs.edit {
            putBoolean(KEY_ATTENDANCE_STATS_SYNC_STATUS, status)

            if (status) {
                putLong(KEY_ATTENDANCE_STATS_SYNC_TIMESTAMP, System.currentTimeMillis())
            }
        }

    }

    // Return true if timestamp not old than 5 days and attendance status is true
    fun isAttendanceStatsSynced(): Boolean {
        val lastSyncTimestamp = prefs.getLong(KEY_ATTENDANCE_STATS_SYNC_TIMESTAMP,
            System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000))
        val currentTimestamp = System.currentTimeMillis()

        return (currentTimestamp - lastSyncTimestamp) < (5 * 24 * 60 * 60 * 1000) &&
                prefs.getBoolean(KEY_ATTENDANCE_STATS_SYNC_STATUS, false)
    }

    fun clearSession() {
        prefs.edit { clear() }
    }

}
