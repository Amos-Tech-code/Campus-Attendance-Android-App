package com.amos_tech_code.smartattend.ui.feature.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "SmartAttendance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        BadgedBox(
                            badge = {
                                if (state.unreadNotifications > 0) {
                                    Badge {
                                        Text(state.unreadNotifications.toString())
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

            // Today's Sessions
            item {
                TodaySessionsSection(
                    sessions = state.todaySessions,
                    onSessionClick = { },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Quick Stats
            item {
                AttendanceStatsSection(
                    stats = state.attendanceStats,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Recent Activity
            item {
                RecentActivitySection(
                    activities = state.recentActivities,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
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
private fun TodaySessionsSection(
    sessions: List<Session>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Sessions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "All",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        SmartAttendHeightSpacer(12.dp)

        if (sessions.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Schedule,
                title = "No sessions today",
                description = "You have no classes scheduled for today"
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                sessions.forEach {
                        SessionCard(
                            session = it,
                            onClick = { onSessionClick(it.id) }
                        )
                }
            }
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
private fun AttendanceStatsSection(
    stats: AttendanceStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Attendance Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(16.dp)

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Attendance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${stats.overallPercentage}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Circular Progress
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { stats.overallPercentage / 100f },
                            modifier = Modifier.size(60.dp),
                            color = when {
                                stats.overallPercentage >= 75 -> PresentColor
                                stats.overallPercentage >= 50 -> PendingColor
                                else -> AbsentColor
                            },
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "${stats.totalSessions}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        count = stats.presentCount,
                        label = "Present",
                        color = PresentColor
                    )
                    StatItem(
                        count = stats.absentCount,
                        label = "Absent",
                        color = AbsentColor
                    )
                    StatItem(
                        count = stats.lateCount,
                        label = "Late",
                        color = PendingColor
                    )
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

@Composable
private fun SessionCard(
    session: Session,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (session.status) {
                            SessionStatus.ACTIVE -> PresentColor
                            SessionStatus.UPCOMING -> PendingColor
                            SessionStatus.COMPLETED -> MaterialTheme.colorScheme.outline
                            SessionStatus.MISSED -> AbsentColor
                        },
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.courseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${session.time} • ${session.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = session.status.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (session.status) {
                        SessionStatus.ACTIVE -> PresentColor
                        SessionStatus.UPCOMING -> MaterialTheme.colorScheme.primary
                        SessionStatus.COMPLETED -> MaterialTheme.colorScheme.outline
                        SessionStatus.MISSED -> AbsentColor
                    },
                    fontWeight = FontWeight.Medium
                )
            }

            if (session.status == SessionStatus.ACTIVE) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "Mark Attendance",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}