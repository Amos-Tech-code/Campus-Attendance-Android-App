package com.amos_tech_code.smartattend.ui.feature.notification

import androidx.compose.ui.graphics.vector.ImageVector
import com.amos_tech_code.smartattend.domain.models.NotificationType

// State and Event Classes for Student
data class StudentNotificationState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val isClearingAll: Boolean = false,
    val isPerformingAction: Boolean = false,
    val allNotifications: List<StudentNotification> = emptyList(),
    val filteredNotifications: List<StudentNotification> = emptyList(),
    val selectedFilter: StudentNotificationFilter = StudentNotificationFilter.ALL,
    val totalCount: Int = 0,
    val unreadCount: Int = 0,
    val error: String? = null
)

enum class StudentNotificationFilter {
    ALL,
    UNREAD,
    ATTENDANCE,
    DEVICE,
    SYSTEM
}

// UI Model for Student Notifications
data class StudentNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean,
    val icon: ImageVector,
    val isPerformingAction: Boolean,
    val sessionId: String? = null
)