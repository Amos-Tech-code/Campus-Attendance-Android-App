package com.amos_tech_code.smartattend.ui.feature.notification

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.NotificationType
import com.amos_tech_code.smartattend.ui.components.ConfirmActionDialog
import com.amos_tech_code.smartattend.ui.components.EmptyState
import com.amos_tech_code.smartattend.ui.components.ErrorDialog
import com.amos_tech_code.smartattend.ui.navigation.AttendanceHistoryRoute
import com.amos_tech_code.smartattend.ui.navigation.DeviceChangeRoute
import com.amos_tech_code.smartattend.ui.navigation.StudentLookupRoute
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import kotlin.compareTo
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Dialog states
    var showMarkAllReadDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is NotificationEvent.ShowErrorMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
            is NotificationEvent.ShowSuccessMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
            is NotificationEvent.NavigateToStudent -> {
                // Navigate to student details
                navController.navigate(StudentLookupRoute)
            }
            is NotificationEvent.NavigateToSession -> {
                // Navigate to session details
                navController.navigate(AttendanceHistoryRoute)
            }
            is NotificationEvent.NavigateToDeviceApproval -> {
                navController.navigate(DeviceChangeRoute)
            }
        }
    }

    // Convert filter index to enum
    val selectedFilterIndex = when (state.selectedFilter) {
        NotificationFilter.ALL -> 0
        NotificationFilter.UNREAD -> 1
        NotificationFilter.DEVICE_REQUESTS -> 2
        NotificationFilter.ATTENDANCE -> 3
        NotificationFilter.SYSTEM -> 4
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                actions = {
                    // Mark All as Read Button
                    IconButton(
                        onClick = {
                            if (state.allNotifications.any { !it.isRead }) {
                                showMarkAllReadDialog = true
                            }
                        },
                        enabled = !state.isMarkingAllRead && state.allNotifications.any { !it.isRead }
                    ) {
                        if (state.isMarkingAllRead) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark All as Read",
                                tint = if (state.allNotifications.any { !it.isRead }) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                }
                            )
                        }
                    }

                    // Clear All Button
                    IconButton(
                        onClick = {
                            if (state.allNotifications.isNotEmpty()) {
                                showClearAllDialog = true
                            }
                        },
                        enabled = !state.isClearingAll && state.allNotifications.isNotEmpty()
                    ) {
                        if (state.isClearingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All",
                                tint = if (state.allNotifications.isNotEmpty()) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Notification Filter Tabs
            val filters = listOf("All", "Unread", "Device", "Attendance", "System")

            ScrollableTabRow(
                selectedTabIndex = selectedFilterIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                divider = {}
            ) {
                filters.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilterIndex == index,
                        onClick = {
                            val filter = when (index) {
                                0 -> NotificationFilter.ALL
                                1 -> NotificationFilter.UNREAD
                                2 -> NotificationFilter.DEVICE_REQUESTS
                                3 -> NotificationFilter.ATTENDANCE
                                4 -> NotificationFilter.SYSTEM
                                else -> NotificationFilter.ALL
                            }
                            viewModel.onEvent(NotificationUiEvent.FilterChanged(filter))
                        },
                        text = {
                            BadgedBox(
                                badge = {
                                    // Only show badge for the "Unread" tab (index 1) and if count > 0
                                    if (index == 1 && state.unreadCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text(state.unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Notifications List
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.filteredNotifications.isEmpty() -> {
                        LoadingState()
                    }
                    state.filteredNotifications.isEmpty() -> {
                        EmptyNotificationState(
                            filter = state.selectedFilter
                        )
                    }
                    else -> {
                        NotificationsList(
                            notifications = state.filteredNotifications,
                            isPerformingAction = state.isPerformingAction,
                            isLoadingMore = state.isLoadingMore,
                            onAction = { notificationId, action ->
                                viewModel.onEvent(NotificationUiEvent.PerformAction(notificationId, action))
                            },
                            onDismiss = { notificationId ->
                                viewModel.onEvent(NotificationUiEvent.DismissNotification(notificationId))
                            },
                            onLoadMore = {
                                viewModel.onEvent(NotificationUiEvent.LoadMore)
                            }
                        )
                    }
                }

                // Loading overlay for pull-to-refresh
                if (state.isLoading && state.filteredNotifications.isNotEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }

    state.error?.let {
        ErrorDialog(
            title = "Error",
            message = state.error!!,
            positiveButtonText = "Retry",
            onPositiveButtonClick = {
                viewModel.onEvent(NotificationUiEvent.Retry)
            },
            onDismiss = {
                navController.navigateUp()
            }
        )
    }

    // Mark All as Read Confirmation Dialog
    if (showMarkAllReadDialog) {
        val unreadCount = state.allNotifications.count { !it.isRead }
        ConfirmActionDialog(
            title = "Mark All as Read",
            message = "Are you sure you want to mark all $unreadCount unread notifications as read?",
            onDismiss = { showMarkAllReadDialog = false },
            onConfirm = {
                showMarkAllReadDialog = false
                viewModel.onEvent(NotificationUiEvent.MarkAllAsRead)
            },
            confirmText = "Mark as Read",
            dismissText = "Cancel"
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllDialog) {
        val totalCount = state.allNotifications.size
        ConfirmActionDialog(
            title = "Clear All Notifications",
            message = "Are you sure you want to delete all $totalCount notifications? This action cannot be undone.",
            onDismiss = { showClearAllDialog = false },
            onConfirm = {
                showClearAllDialog = false
                viewModel.onEvent(NotificationUiEvent.ClearAll)
            },
            confirmText = "Delete All",
            dismissText = "Cancel",
            isDestructive = true
        )
    }

}

@Composable
private fun NotificationsList(
    notifications: List<UiNotification>,
    isPerformingAction: Boolean,
    isLoadingMore: Boolean,
    onAction: (String, NotificationAction) -> Unit,
    onDismiss: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            NotificationItem(
                notification = notification,
                onAction = { action -> onAction(notification.id, action) },
                onDismiss = { onDismiss(notification.id) },
                isActionInProgress = isPerformingAction
            )
        }

        // Loading more indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        // Load more trigger
        item {
            LaunchedEffect(notifications.size) {
                if (notifications.isNotEmpty() && notifications.size % 20 == 0) {
                    onLoadMore()
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: UiNotification,
    onAction: (NotificationAction) -> Unit,
    onDismiss: () -> Unit,
    isActionInProgress: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            }
        ),
        border = if (!notification.isRead) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else {
            null
        },
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Notification Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = getBackgroundColorForType(notification.type),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = notification.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = getIconTintForType(notification.type)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold
                        )
                        Text(
                            text = notification.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Dismiss Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp),
                    enabled = !isActionInProgress
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Message
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            // Actions (if any)
            if (notification.actions.isNotEmpty() && !isActionInProgress) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notification.actions.forEach { action ->
                        when (action) {
                            is NotificationAction.ApproveDevice -> {
                                FilledTonalButton(
                                    onClick = {
                                        onAction(action.copy(requestId = notification.requestId ?: ""))
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = PresentColor.copy(alpha = 0.2f),
                                        contentColor = PresentColor
                                    ),
                                ) {
                                    Text("Approve Device")
                                }
                            }
                            is NotificationAction.ViewStudent -> {
                                OutlinedButton(
                                    onClick = {
                                        onAction(action.copy(studentId = notification.studentId ?: ""))
                                    },
                                ) {
                                    Text("View Student")
                                }
                            }
                            is NotificationAction.ViewSession -> {
                                OutlinedButton(
                                    onClick = {
                                        onAction(action.copy(sessionId = notification.sessionId ?: ""))
                                    },
                                ) {
                                    Text("View Session")
                                }
                            }
                            is NotificationAction.ViewDetails -> {
                                OutlinedButton(
                                    onClick = { onAction(action) }
                                ) {
                                    Text("View Details")
                                }
                            }
                        }
                    }
                }
            } else if (isActionInProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}


@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = "Loading notifications...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyNotificationState(filter: NotificationFilter) {
    EmptyState(
        icon = Icons.Default.Notifications,
        title = "No Notifications",
        description = when (filter) {
            NotificationFilter.UNREAD -> "You have no unread notifications"
            NotificationFilter.DEVICE_REQUESTS -> "No device change requests"
            NotificationFilter.ATTENDANCE -> "No attendance notifications"
            NotificationFilter.SYSTEM -> "No system notifications"
            else -> "You're all caught up!"
        },
        modifier = Modifier.fillMaxSize()
    )
}


// Helper functions for styling
@Composable
private fun getBackgroundColorForType(type: NotificationType): androidx.compose.ui.graphics.Color {
    return when (type) {
        NotificationType.DEVICE_REQUEST,
        NotificationType.DEVICE_APPROVED,
        NotificationType.DEVICE_REJECTED -> MaterialTheme.colorScheme.tertiaryContainer

        NotificationType.ATTENDANCE_MARKED,
        NotificationType.ATTENDANCE_REVOKED,
        NotificationType.SESSION_STARTED,
        NotificationType.SESSION_ENDED,
        NotificationType.SUSPICIOUS_ACTIVITY -> MaterialTheme.colorScheme.secondaryContainer

        NotificationType.SYSTEM_ALERT,
        NotificationType.SUPPORT_RESPONSE -> MaterialTheme.colorScheme.errorContainer
    }
}

@Composable
private fun getIconTintForType(type: NotificationType): androidx.compose.ui.graphics.Color {
    return when (type) {
        NotificationType.DEVICE_REQUEST,
        NotificationType.DEVICE_APPROVED,
        NotificationType.DEVICE_REJECTED -> MaterialTheme.colorScheme.onTertiaryContainer

        NotificationType.ATTENDANCE_MARKED,
        NotificationType.ATTENDANCE_REVOKED,
        NotificationType.SESSION_STARTED,
        NotificationType.SESSION_ENDED,
        NotificationType.SUSPICIOUS_ACTIVITY -> MaterialTheme.colorScheme.onSecondaryContainer

        NotificationType.SYSTEM_ALERT,
        NotificationType.SUPPORT_RESPONSE -> MaterialTheme.colorScheme.onErrorContainer
    }
}