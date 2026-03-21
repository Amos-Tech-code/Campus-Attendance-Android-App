package com.amos_tech_code.smartattend.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amos_tech_code.smartattend.BuildConfig
import com.amos_tech_code.smartattend.R
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import com.amos_tech_code.smartattend.domain.request.FCMTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartAttendNotificationManager(
    private val apiService: ApiService,
    private val session: SessionProvider,
    private val context: Context
) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val job = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun createChannels() {
        NotificationChannelType.entries.forEach { channelType ->
            val channel = NotificationChannelCompat.Builder(
                channelType.id,
                channelType.importance
            )
                .setName(channelType.channelName)
                .setDescription(channelType.channelDescription)
                .setVibrationEnabled(true)
                .setVibrationPattern(longArrayOf(100, 200, 100, 200))
                .setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    android.app.Notification.AUDIO_ATTRIBUTES_DEFAULT
                )
                .build()

            notificationManager.createNotificationChannel(channel)
            //Log.d("NotificationManager", "Created channel: ${channelType.channelName}")
        }
    }

    fun getAndStoreToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                //Log.d("FCM_TOKEN", "Token retrieved: $token")
                session.saveFcmToken(token)
                updateFCMToken(token)
            } else {
                //Log.e("FCM_TOKEN", "Failed to get token", task.exception)
            }
        }
    }

    fun updateFCMToken(token: String) {
        job.launch {
            // Determine which API to call based on flavor
            val response = when (BuildConfig.FLAVOR) {
                "student" -> safeApiCall {
                    apiService.updateStudentFCMToken(FCMTokenRequest(token))
                }
                "lecturer" -> safeApiCall {
                    apiService.updateLecturerFCMToken(FCMTokenRequest(token))
                }
                else -> {
                    Log.e("FCM_REQUEST", "Unknown flavor: ${BuildConfig.FLAVOR}")
                    return@launch
                }
            }

            when (response) {
                is ApiResult.Success -> {
                    session.setFCMUpdated(true)
                    Log.d("FCM_REQUEST", "Token updated successfully: ${response.data.message}")
                }
                is ApiResult.Failure -> {
                    session.setFCMUpdated(false)
                    Log.e("FCM_REQUEST", "Failed to update token: ${response.error.extractApiErrorMessage()}")
                }
            }
        }
    }

    fun updateDeviceStatus(deviceStatus: DeviceStatus) {
        session.updateDeviceStatus(deviceStatus)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(
        title: String,
        message: String,
        notificationId: Int,
        pendingIntent: PendingIntent,
        channelType: NotificationChannelType
    ) {
        try {
            val notification = NotificationCompat.Builder(context, channelType.id)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(getNotificationIcon(channelType))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(100, 200, 100, 200))
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

            notificationManager.notify(notificationId, notification)
            Log.d("NotificationManager", "Notification shown: $title")
        } catch (e: Exception) {
            Log.e("NotificationManager", "Failed to show notification", e)
        }
    }

    private fun getNotificationIcon(channelType: NotificationChannelType): Int {
        return when (channelType) {
            NotificationChannelType.ATTENDANCE -> R.drawable.ic_notification_attendance
            NotificationChannelType.DEVICE -> R.drawable.ic_notification_device
            NotificationChannelType.SESSION -> R.drawable.ic_notification_session
            NotificationChannelType.ALERT -> R.drawable.ic_notification_alert
        }
    }
}