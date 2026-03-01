package com.amos_tech_code.smartattend.notifications

enum class NotificationChannelType(
    val id: String,
    val channelName: String,
    val channelDescription: String,
    val importance: Int
) {
    ATTENDANCE(
        id = "attendance_channel",
        channelName = "Attendance Updates",
        channelDescription = "Notifications about attendance marking and updates",
        importance = android.app.NotificationManager.IMPORTANCE_HIGH
    ),
    DEVICE(
        id = "device_channel",
        channelName = "Device Management",
        channelDescription = "Notifications about device approvals and changes",
        importance = android.app.NotificationManager.IMPORTANCE_HIGH
    ),
    SESSION(
        id = "session_channel",
        channelName = "Session Updates",
        channelDescription = "Notifications about session start/end",
        importance = android.app.NotificationManager.IMPORTANCE_HIGH
    ),
    ALERT(
        id = "alert_channel",
        channelName = "System Alerts",
        channelDescription = "Important system alerts and notifications",
        importance = android.app.NotificationManager.IMPORTANCE_HIGH
    )
}