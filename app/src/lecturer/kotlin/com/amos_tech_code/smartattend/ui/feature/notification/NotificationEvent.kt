package com.amos_tech_code.smartattend.ui.feature.notification

// UI Events (from user interactions)
sealed class NotificationUiEvent {
    data class FilterChanged(val filter: NotificationFilter) : NotificationUiEvent()
    object MarkAllAsRead : NotificationUiEvent()
    object ClearAll : NotificationUiEvent()
    data class DismissNotification(val notificationId: String) : NotificationUiEvent()
    data class PerformAction(val notificationId: String, val action: NotificationAction) : NotificationUiEvent()
    object Refresh : NotificationUiEvent()
}

// App Events (for navigation, toasts, etc.)
sealed class NotificationEvent {
    data class ShowErrorMessage(val message: String) : NotificationEvent()
    data class ShowSuccessMessage(val message: String) : NotificationEvent()
    data class NavigateToStudent(val studentId: String) : NotificationEvent()
    data class NavigateToSession(val sessionId: String) : NotificationEvent()
    data class DeviceRequestApproved(val requestId: String) : NotificationEvent()
}