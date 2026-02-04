package com.amos_tech_code.smartattend.ui.feature.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.EmptyState
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is HomeEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val state by viewModel.homeState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HomeScreenTopAppBar(
                unreadNotifications = state.unreadNotifications,
                onNotificationClick = { }
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        },
        //contentColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Welcome Card
            item {
                WelcomeCard(
                    studentName = state.studentName,
                    registrationNo = state.registrationNo,
                    modifier = Modifier.padding(16.dp)
                )
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopAppBar(
    unreadNotifications: Int,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    text = "Class Track",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Student",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        if (unreadNotifications > 0) {
                            Badge {
                                Text(unreadNotifications.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
        }
    )
}

@Composable
private fun WelcomeCard(
    studentName: String,
    registrationNo: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Welcome back, $studentName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = registrationNo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = "Ready to mark your attendance for today's sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    activities: List<Activity>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        SmartAttendHeightSpacer(12.dp)

        if (activities.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Notifications,
                title = "No recent activity",
                description = "Your recent activities will appear here"
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activities.take(5).forEach {
                    ActivityItem(activity = it)
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: Activity) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (activity.status) {
                            ActivityStatus.SUCCESS -> PresentColor.copy(alpha = 0.1f)
                            ActivityStatus.WARNING -> PendingColor.copy(alpha = 0.1f)
                            ActivityStatus.ERROR -> AbsentColor.copy(alpha = 0.1f)
                            ActivityStatus.INFO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (activity.type) {
                        ActivityType.ATTENDANCE_MARKED -> Icons.Default.CheckCircle
                        ActivityType.ATTENDANCE_FAILED -> Icons.Default.Error
                        ActivityType.NEW_SESSION -> Icons.Default.Schedule
                        ActivityType.DEVICE_CHANGE -> Icons.Default.PhoneAndroid
                        ActivityType.SYSTEM_ALERT -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when (activity.status) {
                        ActivityStatus.SUCCESS -> PresentColor
                        ActivityStatus.WARNING -> PendingColor
                        ActivityStatus.ERROR -> AbsentColor
                        ActivityStatus.INFO -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                if (activity.courseName != null) {
                    Text(
                        text = activity.courseName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = activity.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
