package com.amos_tech_code.smartattend.ui.feature.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state = _state.asStateFlow()

    private val _event = Channel<NotificationEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadNotifications()
    }

    fun onEvent(event: NotificationUiEvent) {
        when (event) {
            is NotificationUiEvent.FilterChanged -> {
                filterNotifications(event.filter)
            }
            NotificationUiEvent.MarkAllAsRead -> {
                markAllAsRead()
            }
            NotificationUiEvent.ClearAll -> {
                clearAllNotifications()
            }
            is NotificationUiEvent.DismissNotification -> {
                dismissNotification(event.notificationId)
            }
            is NotificationUiEvent.PerformAction -> {
                performAction(event.notificationId, event.action)
            }
            NotificationUiEvent.Refresh -> {
                loadNotifications()
            }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Simulate API call delay
                delay(1500)

                // Load mock notifications
                val notifications = createMockNotifications()
                _state.update {
                    it.copy(
                        isLoading = false,
                        allNotifications = notifications,
                        filteredNotifications = filterNotificationsByType(notifications, _state.value.selectedFilter)
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load notifications: ${e.message}"
                    )
                }
                _event.send(NotificationEvent.ShowErrorMessage("Failed to load notifications"))
            }
        }
    }

    private fun filterNotifications(filter: NotificationFilter) {
        _state.update { state ->
            val filtered = filterNotificationsByType(state.allNotifications, filter)
            state.copy(
                selectedFilter = filter,
                filteredNotifications = filtered
            )
        }
    }

    private fun filterNotificationsByType(
        notifications: List<LecturerNotification>,
        filter: NotificationFilter
    ): List<LecturerNotification> {
        return when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilter.DEVICE_REQUESTS -> notifications.filter { it.type == NotificationType.DEVICE_REQUEST }
            NotificationFilter.SYSTEM -> notifications.filter { it.type == NotificationType.SYSTEM_ALERT || it.type == NotificationType.INFO }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            _state.update { it.copy(isMarkingAllRead = true) }

            try {
                // Simulate API call
                delay(500)

                val updatedNotifications = _state.value.allNotifications.map { notification ->
                    notification.copy(isRead = true)
                }

                _state.update { state ->
                    state.copy(
                        isMarkingAllRead = false,
                        allNotifications = updatedNotifications,
                        filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter)
                    )
                }

                _event.send(NotificationEvent.ShowSuccessMessage("All notifications marked as read"))

            } catch (e: Exception) {
                _state.update { it.copy(isMarkingAllRead = false) }
                _event.send(NotificationEvent.ShowErrorMessage("Failed to mark all as read"))
            }
        }
    }

    private fun clearAllNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isClearingAll = true) }

            try {
                // Simulate API call
                delay(500)

                _state.update { state ->
                    state.copy(
                        isClearingAll = false,
                        allNotifications = emptyList(),
                        filteredNotifications = emptyList()
                    )
                }

                _event.send(NotificationEvent.ShowSuccessMessage("All notifications cleared"))

            } catch (e: Exception) {
                _state.update { it.copy(isClearingAll = false) }
                _event.send(NotificationEvent.ShowErrorMessage("Failed to clear notifications"))
            }
        }
    }

    private fun dismissNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                // Remove notification from list
                val updatedNotifications = _state.value.allNotifications.filter { it.id != notificationId }

                _state.update { state ->
                    state.copy(
                        allNotifications = updatedNotifications,
                        filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter)
                    )
                }

            } catch (e: Exception) {
                _event.send(NotificationEvent.ShowErrorMessage("Failed to dismiss notification"))
            }
        }
    }

    private fun performAction(notificationId: String, action: NotificationAction) {
        viewModelScope.launch {
            _state.update { it.copy(isPerformingAction = true) }

            try {
                // Simulate API call
                delay(800)

                when (action) {
                    is NotificationAction.ApproveDevice -> {
                        // Handle device approval logic
                        _event.send(NotificationEvent.DeviceRequestApproved(action.requestId))

                        // Remove the notification after action
                        val updatedNotifications = _state.value.allNotifications.filter { it.id != notificationId }
                        _state.update { state ->
                            state.copy(
                                isPerformingAction = false,
                                allNotifications = updatedNotifications,
                                filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter)
                            )
                        }
                    }
                    is NotificationAction.ViewStudent -> {
                        _event.send(NotificationEvent.NavigateToStudent(action.studentId))
                    }
                    is NotificationAction.ViewSession -> {
                        _event.send(NotificationEvent.NavigateToSession(action.sessionId))
                    }
                }

            } catch (e: Exception) {
                _state.update { it.copy(isPerformingAction = false) }
                _event.send(NotificationEvent.ShowErrorMessage("Failed to perform action"))
            }
        }
    }

    // Mock data generator
    private fun createMockNotifications(): List<LecturerNotification> {
        return listOf(
            LecturerNotification(
                id = "notif_1",
                type = NotificationType.DEVICE_REQUEST,
                title = "Device Change Request",
                message = "John Doe (U123/2021) requested to change device to Samsung Galaxy S22 Ultra",
                timestamp = "2 hours ago",
                isRead = false,
                icon = Icons.Default.PhoneAndroid,
                actions = listOf(
                    NotificationAction.ApproveDevice("device_req_1"),
                    NotificationAction.ViewStudent("student_1")
                ),
                studentId = "student_1"
            ),
            LecturerNotification(
                id = "notif_2",
                type = NotificationType.SYSTEM_ALERT,
                title = "Low Attendance Alert",
                message = "CS401 - Mobile App Development has 65% attendance this week",
                timestamp = "5 hours ago",
                isRead = false,
                icon = Icons.Default.Warning,
                actions = listOf(
                    NotificationAction.ViewSession("session_1")
                ),
                sessionId = "session_1"
            ),
            LecturerNotification(
                id = "notif_3",
                type = NotificationType.DEVICE_REQUEST,
                title = "Device Change Request",
                message = "Jane Smith (U124/2021) requested to change device to Google Pixel 7",
                timestamp = "1 day ago",
                isRead = true,
                icon = Icons.Default.PhoneAndroid,
                actions = listOf(
                    NotificationAction.ApproveDevice("device_req_2"),
                    NotificationAction.ViewStudent("student_2")
                ),
                studentId = "student_2"
            ),
            LecturerNotification(
                id = "notif_4",
                type = NotificationType.ATTENDANCE_ALERT,
                title = "Suspicious Activity",
                message = "Multiple device logins detected for student Michael Brown",
                timestamp = "1 day ago",
                isRead = true,
                icon = Icons.Default.Security,
                actions = listOf(
                    NotificationAction.ViewStudent("student_3")
                ),
                studentId = "student_3"
            ),
            LecturerNotification(
                id = "notif_5",
                type = NotificationType.INFO,
                title = "System Update",
                message = "SmartAttendance app updated to version 2.1.0 with new features",
                timestamp = "2 days ago",
                isRead = true,
                icon = Icons.Default.Info,
                actions = emptyList()
            ),
            LecturerNotification(
                id = "notif_6",
                type = NotificationType.DEVICE_REQUEST,
                title = "Device Change Request",
                message = "Alice Johnson (U125/2021) requested to change device to iPhone 14 Pro",
                timestamp = "3 days ago",
                isRead = true,
                icon = Icons.Default.PhoneAndroid,
                actions = listOf(
                    NotificationAction.ApproveDevice("device_req_3"),
                    NotificationAction.ViewStudent("student_4")
                ),
                studentId = "student_4"
            )
        )
    }
}



// Notification Data Classes
data class LecturerNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean,
    val icon: ImageVector,
    val actions: List<NotificationAction>,
    val studentId: String? = null,
    val sessionId: String? = null
)

enum class NotificationType {
    DEVICE_REQUEST, SYSTEM_ALERT, ATTENDANCE_ALERT, INFO
}

sealed class NotificationAction {
    data class ApproveDevice(val requestId: String) : NotificationAction()
    data class ViewStudent(val studentId: String) : NotificationAction()
    data class ViewSession(val sessionId: String) : NotificationAction()
}