package com.amos_tech_code.smartattend.ui.feature.notification
//
//sealed class NotificationState {
//    data object Nothing : NotificationState()
//    data object Loading : NotificationState()
//    data object Success : NotificationState()
//    data class Error(val message: String) : NotificationState()
//}

data class NotificationState(
    // Notifications Data
    val allNotifications: List<LecturerNotification> = emptyList(),
    val filteredNotifications: List<LecturerNotification> = emptyList(),

    // UI State
    val isLoading: Boolean = false,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,

    // Action States
    val isMarkingAllRead: Boolean = false,
    val isClearingAll: Boolean = false,
    val isPerformingAction: Boolean = false,

    // Error State
    val errorMessage: String? = null
)

enum class NotificationFilter {
    ALL, UNREAD, DEVICE_REQUESTS, SYSTEM
}