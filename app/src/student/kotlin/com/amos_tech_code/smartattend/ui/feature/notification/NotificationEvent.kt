package com.amos_tech_code.smartattend.ui.feature.notification

sealed class StudentNotificationEvent {
    data class ShowErrorMessage(val message: String) : StudentNotificationEvent()
    data class ShowSuccessMessage(val message: String) : StudentNotificationEvent()
}

sealed class StudentNotificationUiEvent {
    data class FilterChanged(val filter: StudentNotificationFilter) : StudentNotificationUiEvent()
    object MarkAllAsRead : StudentNotificationUiEvent()
    object ClearAll : StudentNotificationUiEvent()
    data class DismissNotification(val notificationId: String) : StudentNotificationUiEvent()
    data class PerformAction(val notificationId: String, val action: StudentNotificationAction) : StudentNotificationUiEvent()
    object Refresh : StudentNotificationUiEvent()
    object LoadMore : StudentNotificationUiEvent()

    object Retry : StudentNotificationUiEvent()
}

sealed class StudentNotificationAction {
    data class MarkAsRead(val notificationId: String) : StudentNotificationAction()
    object ViewDetails : StudentNotificationAction()
}