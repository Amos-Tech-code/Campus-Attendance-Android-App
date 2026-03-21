package com.amos_tech_code.smartattend.ui.feature.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.repositories.NotificationRepository
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.domain.models.NotificationType
import com.amos_tech_code.smartattend.domain.response.NotificationDto
import com.amos_tech_code.smartattend.utils.formatDateTime
import com.amos_tech_code.smartattend.utils.toAmPmTime
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

class StudentNotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StudentNotificationState())
    val state = _state.asStateFlow()

    private val _event = Channel<StudentNotificationEvent>()
    val event = _event.receiveAsFlow()

    private var currentPage = 0
    private var hasMorePages = true
    private val pageSize = 20

    init {
        loadNotifications()
        loadNotificationCounts()
    }

    fun onEvent(event: StudentNotificationUiEvent) {
        when (event) {
            is StudentNotificationUiEvent.FilterChanged -> {
                filterNotifications(event.filter)
            }
            StudentNotificationUiEvent.MarkAllAsRead -> {
                markAllAsRead()
            }
            StudentNotificationUiEvent.ClearAll -> {
                clearAllNotifications()
            }
            is StudentNotificationUiEvent.DismissNotification -> {
                dismissNotification(event.notificationId)
            }
            is StudentNotificationUiEvent.PerformAction -> {
                performAction(event.notificationId, event.action)
            }
            StudentNotificationUiEvent.Refresh -> {
                refreshNotifications()
            }
            StudentNotificationUiEvent.LoadMore -> {
                loadMoreNotifications()
            }
            StudentNotificationUiEvent.Retry -> {
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
                    val notifications = result.data.map { it.toStudentNotification() }
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
                    _event.send(StudentNotificationEvent.ShowErrorMessage(errorMessage))
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
                    _event.send(StudentNotificationEvent.ShowErrorMessage("Failed to load notification counts"))
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
                    val newNotifications = result.data.notifications.map { it.toStudentNotification() }
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

    private fun filterNotifications(filter: StudentNotificationFilter) {
        _state.update { state ->
            val filtered = filterNotificationsByType(state.allNotifications, filter)
            state.copy(
                selectedFilter = filter,
                filteredNotifications = filtered
            )
        }
    }

    private fun filterNotificationsByType(
        notifications: List<StudentNotification>,
        filter: StudentNotificationFilter
    ): List<StudentNotification> {
        return when (filter) {
            StudentNotificationFilter.ALL -> notifications
            StudentNotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            StudentNotificationFilter.ATTENDANCE -> notifications.filter {
                it.type == NotificationType.ATTENDANCE_MARKED ||
                        it.type == NotificationType.ATTENDANCE_REVOKED
            }
            StudentNotificationFilter.DEVICE -> notifications.filter {
                it.type == NotificationType.DEVICE_APPROVED ||
                        it.type == NotificationType.DEVICE_REJECTED
            }
            StudentNotificationFilter.SYSTEM -> notifications.filter {
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

                    _event.send(StudentNotificationEvent.ShowSuccessMessage("All notifications marked as read"))
                    loadNotificationCounts()
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(isMarkingAllRead = false) }
                    val errorMessage = result.error.extractApiErrorMessage()
                    _event.send(StudentNotificationEvent.ShowErrorMessage(errorMessage))
                }
            }
        }
    }

    private fun clearAllNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isClearingAll = true) }

            // Delete all notifications one by one
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
                _event.send(StudentNotificationEvent.ShowSuccessMessage("All notifications cleared"))
            } else {
                _state.update { it.copy(isClearingAll = false) }
                _event.send(StudentNotificationEvent.ShowErrorMessage("Failed to clear some notifications"))
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
                        _event.send(StudentNotificationEvent.ShowErrorMessage(errorMessage))
                    }
                }
            } catch (e: Exception) {
                _event.send(StudentNotificationEvent.ShowErrorMessage("Failed to dismiss notification"))
            }
        }
    }

    private fun performAction(notificationId: String, action: StudentNotificationAction) {
        viewModelScope.launch {
            when (action) {
                is StudentNotificationAction.ViewDetails -> {
                    // Mark as read and maybe show details
                    markNotificationAsRead(notificationId)
                }
                is StudentNotificationAction.MarkAsRead -> {
                    markNotificationAsRead(notificationId)
                }
            }

            _state.update { it.copy(isPerformingAction = false) }
        }
    }

    private suspend fun markNotificationAsRead(notificationId: String) {
        _state.update { state ->
            val updatedNotifications = state.allNotifications.map {
                if (it.id == notificationId) it.copy(isPerformingAction = true) else it
            }
            state.copy(
                allNotifications = updatedNotifications,
                filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter),
            )
        }

        try {
            val result = notificationRepository.markNotificationAsRead(notificationId)

            when (result) {
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
                    _event.send(StudentNotificationEvent.ShowErrorMessage("Failed to mark notification as read"))
                }
            }
        } catch (e: Exception) {
            _event.send(StudentNotificationEvent.ShowErrorMessage("Failed to mark notification as read"))
        } finally {
            _state.update { state ->
                val updatedNotifications = state.allNotifications.map {
                    if (it.id == notificationId) it.copy(isPerformingAction = false) else it
                }
                state.copy(
                    allNotifications = updatedNotifications,
                    filteredNotifications = filterNotificationsByType(updatedNotifications, state.selectedFilter)
                )
            }
        }
    }

    // Extension function to convert DTO to Student UI model
    private fun NotificationDto.toStudentNotification(): StudentNotification {
        return StudentNotification(
            id = id,
            type = type,
            title = title,
            message = message,
            timestamp = createdAt.formatDateTime(),
            isRead = isRead,
            icon = getIconForType(type),
            isPerformingAction = false,
            sessionId = extractSessionId(message, type)
        )
    }

    private fun getIconForType(type: NotificationType): ImageVector {
        return when (type) {
            NotificationType.ATTENDANCE_MARKED -> Icons.Default.CheckCircle
            NotificationType.ATTENDANCE_REVOKED -> Icons.Default.Warning
            NotificationType.DEVICE_APPROVED -> Icons.Default.CheckCircle
            NotificationType.DEVICE_REJECTED -> Icons.Default.Warning
            NotificationType.SUPPORT_RESPONSE -> Icons.Default.Info
            NotificationType.SYSTEM_ALERT -> Icons.Default.Warning
            else -> Icons.Default.Notifications
        }
    }

    private fun extractSessionId(message: String, type: NotificationType): String? {
        return when (type) {
            NotificationType.ATTENDANCE_MARKED,
            NotificationType.ATTENDANCE_REVOKED -> {
                // Extract session ID from message based on your format
                Regex("Session: (\\w+)").find(message)?.groupValues?.get(1)
            }
            else -> null
        }
    }
}