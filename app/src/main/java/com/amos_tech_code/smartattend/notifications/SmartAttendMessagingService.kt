package com.amos_tech_code.smartattend.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import com.amos_tech_code.smartattend.MainActivity
import com.amos_tech_code.smartattend.SmartAttendApplication
import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import com.amos_tech_code.smartattend.domain.models.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SmartAttendMessagingService : FirebaseMessagingService() {

    private lateinit var notificationManager: SmartAttendNotificationManager

    override fun onCreate() {
        super.onCreate()
        try {
            val application = application as? SmartAttendApplication
            notificationManager = application?.notificationManager
                ?: error("SmartAttendApplication not found or notificationManager not initialized")
            Log.d("FCMService", "NotificationManager initialized successfully")
        } catch (e: Exception) {
            Log.e("FCMService", "Failed to initialize NotificationManager", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        //Log.d("FCMService", "New token generated: $token")
        notificationManager.updateFCMToken(token)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        //Log.d("FCMService", "Message received: ${message.messageId}")
        //Log.d("FCM_DEBUG", "Notification payload: ${message.data}")

        // Extract notification data
//        val title = message.notification?.title ?: message.data["title"] ?: "Smart Attend"
//        val messageText = message.notification?.body ?: message.data["body"] ?: ""
        val title = message.data["title"] ?: "Smart Attend"
        val messageText = message.data["body"] ?: ""
        val data = message.data

        // Determine notification type from data
        val notificationType = data["type"]?.let {
            try {
                NotificationType.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                NotificationType.SYSTEM_ALERT
            }
        } ?: NotificationType.SYSTEM_ALERT

        // Create intent for when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtras(message.data.toBundle())
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            generateNotificationId(notificationType),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Map notification type to channel
        val channelType = notificationType.toChannelType()

        // Show notification
        notificationManager.showNotification(
            title = title,
            message = messageText,
            notificationId = generateNotificationId(notificationType),
            pendingIntent = pendingIntent,
            channelType = channelType
        )

        // For student on device approval or rejection
        if (notificationType == NotificationType.DEVICE_APPROVED) {
            notificationManager.updateDeviceStatus(DeviceStatus.ACTIVE)
        }

        if (notificationType == NotificationType.DEVICE_REJECTED) {
            notificationManager.updateDeviceStatus(DeviceStatus.REJECTED)
        }
    }

    private fun generateNotificationId(notificationType: NotificationType): Int {
        return when (notificationType) {
            NotificationType.ATTENDANCE_MARKED -> 1001
            NotificationType.ATTENDANCE_REVOKED -> 1002
            NotificationType.DEVICE_APPROVED -> 2001
            NotificationType.DEVICE_REJECTED -> 2002
            NotificationType.DEVICE_REQUEST -> 2003
            NotificationType.SESSION_STARTED -> 3001
            NotificationType.SESSION_ENDED -> 3002
            NotificationType.SUSPICIOUS_ACTIVITY -> 4001
            NotificationType.SUPPORT_RESPONSE -> 4002
            NotificationType.SYSTEM_ALERT -> 4003
        }
    }

    private fun Map<String, String>.toBundle(): Bundle {
        return Bundle().apply {
            forEach { (key, value) -> putString(key, value) }
        }
    }
}

// Extension function to map NotificationType to NotificationChannelType
fun NotificationType.toChannelType(): NotificationChannelType {
    return when (this) {
        NotificationType.ATTENDANCE_MARKED,
        NotificationType.ATTENDANCE_REVOKED -> NotificationChannelType.ATTENDANCE

        NotificationType.DEVICE_APPROVED,
        NotificationType.DEVICE_REJECTED,
        NotificationType.DEVICE_REQUEST -> NotificationChannelType.DEVICE

        NotificationType.SESSION_STARTED,
        NotificationType.SESSION_ENDED -> NotificationChannelType.SESSION

        NotificationType.SUSPICIOUS_ACTIVITY,
        NotificationType.SUPPORT_RESPONSE,
        NotificationType.SYSTEM_ALERT -> NotificationChannelType.ALERT
    }
}