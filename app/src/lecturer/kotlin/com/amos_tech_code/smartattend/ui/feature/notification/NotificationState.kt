package com.amos_tech_code.smartattend.ui.feature.notification

import androidx.compose.ui.graphics.vector.ImageVector
import com.amos_tech_code.smartattend.domain.models.NotificationType


// State and Event Classes
data class NotificationState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val isClearingAll: Boolean = false,
    val isPerformingAction: Boolean = false,
    val allNotifications: List<UiNotification> = emptyList(),
    val filteredNotifications: List<UiNotification> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val totalCount: Int = 0,
    val unreadCount: Int = 0,
    val error: String? = null
)

enum class NotificationFilter {
    ALL,
    UNREAD,
    DEVICE_REQUESTS,
    ATTENDANCE,
    SYSTEM
}

// UI Model for Notifications
data class UiNotification(
    val id: String,
    val type: NotificationType, // Using the actual domain NotificationType
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean,
    val icon: ImageVector,
    val actions: List<NotificationAction>,
    val studentId: String? = null,
    val sessionId: String? = null,
    val requestId: String? = null
)
