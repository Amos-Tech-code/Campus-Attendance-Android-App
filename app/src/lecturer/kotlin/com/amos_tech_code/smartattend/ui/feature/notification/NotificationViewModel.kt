package com.amos_tech_code.smartattend.ui.feature.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repositories.NotificationRepository
import com.amos_tech_code.smartattend.domain.models.NotificationType
import com.amos_tech_code.smartattend.domain.response.NotificationDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state = _state.asStateFlow()

    private val _event = Channel<NotificationEvent>()
    val event = _event.receiveAsFlow()

    private var currentPage = 0
    private var hasMorePages = true
    private val pageSize = 20

    init {
        loadNotifications()
        loadNotificationCounts()
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
                refreshNotifications()
            }
            NotificationUiEvent.LoadMore -> {
                loadMoreNotifications()
            }
            NotificationUiEvent.Retry -> {
                _state.update { it.copy(error = null) }
                refreshNotifications()
            }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = notificationRepository.getUnreadNotifications()) {
                is ApiResult.Success -> {
                    val notifications = result.data.map { it.toUiNotification() }
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            allNotifications = notifications,
                            filteredNotifications = filterNotificationsByType(notifications, state.selectedFilter)
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val errorMessage = result.error.extractApiErrorMessage()
                    _state.update { it.copy(isLoading = false, error = errorMessage) }
                    _event.send(NotificationEvent.ShowErrorMessage(errorMessage))
                }
            }
        }
    }

    private fun loadNotificationCounts() {
        viewModelScope.launch {
            when (val result = notificationRepository.getNotificationCounts()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(
                        totalCount = result.data.total,
                        unreadCount = result.data.unread
                    )}
                }
                is ApiResult.Failure -> {
                    // Silently fail for counts
                }
            }
        }
    }

    private fun loadMoreNotifications() {
        if (!hasMorePages || state.value.isLoadingMore) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            currentPage++

            when (val result = notificationRepository.getNotificationHistory(currentPage, pageSize)) {
                is ApiResult.Success -> {
                    val newNotifications = result.data.notifications.map { it.toUiNotification() }
                    hasMorePages = result.data.hasMore

                    val allNotifications = _state.value.allNotifications + newNotifications
                    _state.update { state ->
                        state.copy(
                            isLoadingMore = false,
                            allNotifications = allNotifications,
                            filteredNotifications = filterNotificationsByType(allNotifications, state.selectedFilter)
                        )
                    }
                }
                is ApiResult.Failure -> {
                    currentPage-- // Revert page increment on failure
                    _state.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    private fun refreshNotifications() {
        currentPage = 0
        hasMorePages = true
        loadNotifications()
        loadNotificationCounts()
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
        notifications: List<UiNotification>,
        filter: NotificationFilter
    ): List<UiNotification> {
        return when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilter.DEVICE_REQUESTS -> notifications.filter {
                it.type == NotificationType.DEVICE_REQUEST ||
                        it.type == NotificationType.DEVICE_APPROVED ||
                        it.type == NotificationType.DEVICE_REJECTED
            }
            NotificationFilter.ATTENDANCE -> notifications.filter {
                it.type == NotificationType.ATTENDANCE_MARKED ||
                        it.type == NotificationType.ATTENDANCE_REVOKED ||
                        it.type == NotificationType.SESSION_STARTED ||
                        it.type == NotificationType.SESSION_ENDED ||
                        it.type == NotificationType.SUSPICIOUS_ACTIVITY
            }
            NotificationFilter.SYSTEM -> notifications.filter {
                it.type == NotificationType.SYSTEM_ALERT ||
                        it.type == NotificationType.SUPPORT_RESPONSE
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            _state.update { it.copy(isMarkingAllRead = true) }

            when (val result = notificationRepository.markAllNotificationsAsRead()) {
                is ApiResult.Success -> {
                    val updatedNotifications = _state.value.allNotifications.map { notification ->
                        notification.copy(isRead = true)
                    }

                    _state.update { state ->
                        state.copy(
                            isMarkingAllRead = false,
                            allNotifications = updatedNotifications,
                            filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter),
                            unreadCount = 0
                        )
                    }

                    _event.send(NotificationEvent.ShowSuccessMessage("All notifications marked as read"))
                    loadNotificationCounts()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(isMarkingAllRead = false) }
                    val errorMessage = result.error.extractApiErrorMessage()
                    _event.send(NotificationEvent.ShowErrorMessage(errorMessage))
                }
            }
        }
    }

    private fun clearAllNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isClearingAll = true) }

            // Delete all notifications
            val deleteJobs = _state.value.allNotifications.map { notification ->
                async { notificationRepository.deleteNotification(notification.id) }
            }

            val results = deleteJobs.awaitAll()
            val allSucceeded = results.all { it is ApiResult.Success }

            if (allSucceeded) {
                _state.update { state ->
                    state.copy(
                        isClearingAll = false,
                        allNotifications = emptyList(),
                        filteredNotifications = emptyList(),
                        totalCount = 0,
                        unreadCount = 0
                    )
                }
                _event.send(NotificationEvent.ShowSuccessMessage("All notifications cleared"))
            } else {
                _state.update { it.copy(isClearingAll = false) }
                _event.send(NotificationEvent.ShowErrorMessage("Failed to clear some notifications"))
            }

            loadNotificationCounts()
        }
    }

    private fun dismissNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                // Optimistic update
                val updatedNotifications = _state.value.allNotifications.filter { it.id != notificationId }

                _state.update { state ->
                    state.copy(
                        allNotifications = updatedNotifications,
                        filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter)
                    )
                }

                // Actual API call
                when (val result = notificationRepository.deleteNotification(notificationId)) {
                    is ApiResult.Success -> {
                        loadNotificationCounts()
                    }
                    is ApiResult.Failure -> {
                        // Revert on failure
                        loadNotifications()
                        val errorMessage = result.error.extractApiErrorMessage()
                        _event.send(NotificationEvent.ShowErrorMessage(errorMessage))
                    }
                }
            } catch (_: Exception) {
                _event.send(NotificationEvent.ShowErrorMessage("Failed to dismiss notification"))
            }
        }
    }

    private fun performAction(notificationId: String, action: NotificationAction) {
        viewModelScope.launch {
            _state.update { it.copy(isPerformingAction = true) }

            when (action) {
                is NotificationAction.ApproveDevice -> {
                    handleDeviceApproval()
                }
                is NotificationAction.ViewStudent -> {
                    _event.send(NotificationEvent.NavigateToStudent(action.studentId))
                    markNotificationAsRead(notificationId)
                }
                is NotificationAction.ViewSession -> {
                    _event.send(NotificationEvent.NavigateToSession(action.sessionId))
                    markNotificationAsRead(notificationId)
                }
                is NotificationAction.ViewDetails -> {
                    // Generic view details action
                    markNotificationAsRead(notificationId)
                }
            }

            _state.update { it.copy(isPerformingAction = false) }
        }
    }

    private fun handleDeviceApproval() {
        _event.trySend(NotificationEvent.NavigateToDeviceApproval)
    }

    private suspend fun markNotificationAsRead(notificationId: String) {
        when (val result = notificationRepository.markNotificationAsRead(notificationId)) {
            is ApiResult.Success -> {
                _state.update { state ->
                    val updatedNotifications = state.allNotifications.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    }
                    state.copy(
                        allNotifications = updatedNotifications,
                        filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter),
                        unreadCount = maxOf(0, state.unreadCount - 1)
                    )
                }
            }
            is ApiResult.Failure -> {
                // Silently fail for read status
            }
        }
    }

    // Extension function to convert DTO to UI model using the actual NotificationType
    private fun NotificationDto.toUiNotification(): UiNotification {
        return UiNotification(
            id = id,
            type = type, // Use the actual NotificationType from domain
            title = title,
            message = message,
            timestamp = formatTimestamp(createdAt),
            isRead = isRead,
            icon = getIconForType(type),
            actions = getActionsForType(type),
            studentId = extractStudentId(message, type),
            sessionId = extractSessionId(message, type),
            requestId = extractRequestId(message, type)
        )
    }

    private fun formatTimestamp(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate) ?: Date()

            val outputFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault()
            outputFormat.format(date)
        } catch (e: Exception) {
            isoDate.removeSuffix("Z")
        }
    }

    private fun getIconForType(type: NotificationType): ImageVector {
        return when (type) {
            NotificationType.DEVICE_REQUEST -> Icons.Default.Android
            NotificationType.DEVICE_APPROVED -> Icons.Default.CheckCircle
            NotificationType.DEVICE_REJECTED -> Icons.Default.Warning
            NotificationType.ATTENDANCE_MARKED -> Icons.Default.CheckCircle
            NotificationType.ATTENDANCE_REVOKED -> Icons.Default.Warning
            NotificationType.SESSION_STARTED -> Icons.Default.PlayArrow
            NotificationType.SESSION_ENDED -> Icons.Default.Stop
            NotificationType.SUSPICIOUS_ACTIVITY -> Icons.Default.Warning
            NotificationType.SUPPORT_RESPONSE -> Icons.Default.Info
            NotificationType.SYSTEM_ALERT -> Icons.Default.Warning
        }
    }

    private fun getActionsForType(type: NotificationType): List<NotificationAction> {
        return when (type) {
            NotificationType.DEVICE_REQUEST ->
                listOf(NotificationAction.ApproveDevice(""))
            NotificationType.ATTENDANCE_REVOKED,
            NotificationType.ATTENDANCE_MARKED,
            NotificationType.SUSPICIOUS_ACTIVITY ->
                listOf(
                    NotificationAction.ViewStudent(""),
                    NotificationAction.ViewSession("")
                )
            NotificationType.SESSION_STARTED,
            NotificationType.SESSION_ENDED ->
                listOf(NotificationAction.ViewSession(""))
            NotificationType.DEVICE_APPROVED,
            NotificationType.DEVICE_REJECTED,
            NotificationType.SUPPORT_RESPONSE,
            NotificationType.SYSTEM_ALERT ->
                listOf(NotificationAction.ViewDetails)
        }
    }

    private fun extractStudentId(message: String, type: NotificationType): String? {
        return when (type) {
            NotificationType.ATTENDANCE_REVOKED,
            NotificationType.ATTENDANCE_MARKED,
            NotificationType.SUSPICIOUS_ACTIVITY -> {
                // Extract student ID from message (implementation depends on your message format)
                // Example: "Student John (ID: 123) - message"
                Regex("ID: (\\w+)").find(message)?.groupValues?.get(1)
            }
            else -> null
        }
    }

    private fun extractSessionId(message: String, type: NotificationType): String? {
        return when (type) {
            NotificationType.ATTENDANCE_REVOKED,
            NotificationType.ATTENDANCE_MARKED,
            NotificationType.SESSION_STARTED,
            NotificationType.SESSION_ENDED,
            NotificationType.SUSPICIOUS_ACTIVITY -> {
                // Extract session ID from message
                Regex("Session: (\\w+)").find(message)?.groupValues?.get(1)
            }
            else -> null
        }
    }

    private fun extractRequestId(message: String, type: NotificationType): String? {
        return when (type) {
            NotificationType.DEVICE_REQUEST,
            NotificationType.DEVICE_APPROVED,
            NotificationType.DEVICE_REJECTED -> {
                // Extract request ID from message
                Regex("Request: (\\w+)").find(message)?.groupValues?.get(1)
            }
            else -> null
        }
    }
}