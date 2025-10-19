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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.EmptyState
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                //navController.navigate()
            }
            is NotificationEvent.NavigateToSession -> {
                // Navigate to session details
                //navController.navigate()
            }
            is NotificationEvent.DeviceRequestApproved -> {
                // Handle device request approval success
                // Could show a confirmation or refresh data
            }
        }
    }

    // Convert filter index to enum
    val selectedFilterIndex = when (state.selectedFilter) {
        NotificationFilter.ALL -> 0
        NotificationFilter.UNREAD -> 1
        NotificationFilter.DEVICE_REQUESTS -> 2
        NotificationFilter.SYSTEM -> 3
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Mark All as Read Button
                    IconButton(
                        onClick = { viewModel.onEvent(NotificationUiEvent.MarkAllAsRead) },
                        enabled = !state.isMarkingAllRead && state.allNotifications.any { !it.isRead }
                    ) {
                        if (state.isMarkingAllRead) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.DoneAll, "Mark All as Read")
                        }
                    }

                    // Clear All Button
                    IconButton(
                        onClick = { viewModel.onEvent(NotificationUiEvent.ClearAll) },
                        enabled = !state.isClearingAll && state.allNotifications.isNotEmpty()
                    ) {
                        if (state.isClearingAll) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Delete, "Clear All")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // Notification Filter Chips
            val filters = listOf("All", "Unread", "Device Requests", "System")

            ScrollableTabRow(
                selectedTabIndex = selectedFilterIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
            ) {
                filters.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilterIndex == index,
                        onClick = {
                            val filter = when (index) {
                                0 -> NotificationFilter.ALL
                                1 -> NotificationFilter.UNREAD
                                2 -> NotificationFilter.DEVICE_REQUESTS
                                3 -> NotificationFilter.SYSTEM
                                else -> NotificationFilter.ALL
                            }
                            viewModel.onEvent(NotificationUiEvent.FilterChanged(filter))
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // Notifications List
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading notifications...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                state.filteredNotifications.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Notifications,
                        title = "No Notifications",
                        description = when (state.selectedFilter) {
                            NotificationFilter.UNREAD -> "No unread notifications"
                            NotificationFilter.DEVICE_REQUESTS -> "No device change requests"
                            NotificationFilter.SYSTEM -> "No system notifications"
                            else -> "You're all caught up!"
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredNotifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                onAction = { action ->
                                    viewModel.onEvent(NotificationUiEvent.PerformAction(notification.id, action))
                                },
                                onDismiss = {
                                    viewModel.onEvent(NotificationUiEvent.DismissNotification(notification.id))
                                },
                                isLoading = state.isPerformingAction
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: LecturerNotification,
    onAction: (NotificationAction) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
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
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        } else {
            null
        }
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
                                color = when (notification.type) {
                                    NotificationType.DEVICE_REQUEST -> MaterialTheme.colorScheme.tertiaryContainer
                                    NotificationType.SYSTEM_ALERT -> MaterialTheme.colorScheme.errorContainer
                                    NotificationType.ATTENDANCE_ALERT -> MaterialTheme.colorScheme.secondaryContainer
                                    NotificationType.INFO -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = notification.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = when (notification.type) {
                                NotificationType.DEVICE_REQUEST -> MaterialTheme.colorScheme.onTertiaryContainer
                                NotificationType.SYSTEM_ALERT -> MaterialTheme.colorScheme.onErrorContainer
                                NotificationType.ATTENDANCE_ALERT -> MaterialTheme.colorScheme.onSecondaryContainer
                                NotificationType.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
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
                    enabled = !isLoading
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
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            // Actions (if any)
            if (notification.actions.isNotEmpty() && !isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notification.actions.forEach { action ->
                        when (action) {
                            is NotificationAction.ApproveDevice -> {
                                FilledTonalButton(
                                    onClick = { onAction(action) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = PresentColor.copy(alpha = 0.2f),
                                        contentColor = PresentColor
                                    )
                                ) {
                                    Text("Approve Device")
                                }
                            }
                            is NotificationAction.ViewStudent -> {
                                OutlinedButton(
                                    onClick = { onAction(action) }
                                ) {
                                    Text("View Student")
                                }
                            }
                            is NotificationAction.ViewSession -> {
                                OutlinedButton(
                                    onClick = { onAction(action) }
                                ) {
                                    Text("View Session")
                                }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                // Show loading indicator when performing actions
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}