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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.EmptyState
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.feature.setup.ActivityStatus
import com.amos_tech_code.smartattend.ui.feature.setup.ActivityType
import com.amos_tech_code.smartattend.ui.feature.setup.LecturerActivity
import com.amos_tech_code.smartattend.ui.feature.setup.LecturerSession
import com.amos_tech_code.smartattend.ui.feature.setup.SessionStatus
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.NotificationsRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.ui.navigation.SettingsRoute
import com.amos_tech_code.smartattend.ui.navigation.StartSessionRoute
import com.amos_tech_code.smartattend.ui.navigation.StudentLookupRoute
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

            HomeEvent.CompleteProfile -> {
                navController.navigate(SetUpRoute)
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Lecturer Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { navController.navigate(NotificationsRoute) }) {
                            BadgedBox(
                                badge = {
                                    if (state.unreadNotifications > 0) {
                                        Badge { Text(state.unreadNotifications.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, "Notifications")
                            }
                        }
                        // Menu Icon with Dropdown
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Menu")
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                // Student Lookup
                                DropdownMenuItem(
                                    text = {
                                        MenuItemContent(
                                            icon = Icons.Default.Search,
                                            title = "Student Lookup",
                                            subtitle = "Find and manage students"
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate(StudentLookupRoute)
                                    }
                                )

                                HorizontalDivider()

                                // Notifications
                                DropdownMenuItem(
                                    text = {
                                        MenuItemContent(
                                            icon = Icons.Default.Notifications,
                                            title = "Notifications",
                                            subtitle = "${state.unreadNotifications} unread",
                                            showBadge = state.unreadNotifications > 0
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate(NotificationsRoute)
                                    }
                                )

                                // Settings
                                DropdownMenuItem(
                                    text = {
                                        MenuItemContent(
                                            icon = Icons.Default.Settings,
                                            title = "Settings",
                                            subtitle = "App preferences"
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate(SettingsRoute)
                                    }
                                )

                                HorizontalDivider()
                                // Session History
                                DropdownMenuItem(
                                    text = {
                                        MenuItemContent(
                                            icon = Icons.Default.History,
                                            title = "Session History",
                                            subtitle = "Past attendance sessions"
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        //navController.navigate("session_history")
                                    }
                                )

                                HorizontalDivider()

                                // Help & Support
                                DropdownMenuItem(
                                    text = {
                                        MenuItemContent(
                                            icon = Icons.AutoMirrored.Filled.Help,
                                            title = "Help & Support",
                                            subtitle = "Get assistance"
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        // Navigate to help or open support
                                        //navController.navigate("help_support")
                                    }
                                )

                                // About
                                DropdownMenuItem(
                                    text = {
                                        MenuItemContent(
                                            icon = Icons.Default.Info,
                                            title = "About",
                                            subtitle = "App information"
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        //navController.navigate("about")
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(StartSessionRoute) },
                icon = { Icon(Icons.Default.QrCode, "Start Session") },
                text = { Text("Start Session") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Welcome Card
            WelcomeCard(
                lecturerName = state.lecturerName,
                institution = state.institution,
                modifier = Modifier.padding(16.dp)
            )

            // Quick Stats
            QuickStatsSection(
                stats = state.quickStats,
                modifier = Modifier.padding(16.dp)
            )

            // Today's Sessions
            TodaySessionsSection(
                sessions = state.todaySessions,
                onSessionClick = { sessionId ->
                   // TODO: Navigate to session
                },
                modifier = Modifier.padding(16.dp)
            )

            // Recent Activity
            RecentActivitySection(
                activities = state.recentActivities,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun WelcomeCard(
    lecturerName: String,
    institution: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Lecturer",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Welcome back, $lecturerName!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = institution,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "Ready to start your next class session",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun QuickStatsSection(
    stats: LecturerQuickStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(16.dp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Active Sessions",
                value = stats.activeSessions.toString(),
                subtitle = "Today",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Students",
                value = stats.totalStudents.toString(),
                subtitle = "Enrolled",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Attendance Rate",
                value = "${stats.attendanceRate}%",
                subtitle = "Average",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun TodaySessionsSection(
    sessions: List<LecturerSession>,
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
    activities: List<LecturerActivity>,
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
private fun ActivityItem(activity: LecturerActivity) {
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
    session: LecturerSession,
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
private fun MenuItemContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showBadge: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon with optional badge
        Box {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            if (showBadge) {
                Badge(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Chevron icon
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}