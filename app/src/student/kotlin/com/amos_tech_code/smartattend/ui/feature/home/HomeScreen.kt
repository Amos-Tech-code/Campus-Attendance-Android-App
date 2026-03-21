package com.amos_tech_code.smartattend.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import com.amos_tech_code.smartattend.ui.components.PullToRefreshIndicator
import com.amos_tech_code.smartattend.ui.feature.history.getMethodIcon
import com.amos_tech_code.smartattend.ui.navigation.AttendanceHistoryRoute
import com.amos_tech_code.smartattend.ui.navigation.AttendanceRoute
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.DeviceChangeRoute
import com.amos_tech_code.smartattend.ui.navigation.NotificationsRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Pull to refresh state
    val refreshState = rememberPullToRefreshState()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is HomeEvent.ShowErrorMessage -> {
                scope.launch { snackbarHostState.showSnackbar(event.message) }
            }

            is HomeEvent.ShowSuccessMessage -> {
                scope.launch { snackbarHostState.showSnackbar(event.message) }
            }

            is HomeEvent.NavigateToDeviceChange -> {
                navController.navigate(DeviceChangeRoute)
            }
        }
    }

    Scaffold(
        topBar = {
            HomeScreenTopAppBar(
                studentName = state.studentName,
                registrationNo = state.registrationNo,
                currentSemester = state.currentSemester,
                date = getCurrentFormattedDate(),
                //scrollBehavior = scrollBehavior
            )
        },
        bottomBar = { BottomNavigation(navController = navController) },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullToRefresh(
                    isRefreshing = state.isRefreshing,
                    state = refreshState,
                    onRefresh = { viewModel.refreshDashboard() }
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
                .padding(paddingValues)
                //.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            // Main Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp)
            ) {
                // Quick Stats Cards
                item {
                    QuickStatsRow(
                        totalSessions = state.totalSessions,
                        attendanceRate = state.attendanceRate,
                        currentStreak = state.currentStreak,
                        modifier = Modifier.animateItem()
                    )
                }

                // Today's Schedule Section
//                item {
//                    TodayScheduleSection(
//                        sessions = state.todaySessions,
//                        onSessionClick = { sessionId ->
//                            //navController.navigate("session_details/$sessionId")
//                        },
//                        modifier = Modifier.animateItem()
//                    )
//                }
                if (state.showDeviceWarning) {
                    item {
                        DeviceWarningCard(
                            deviceStatus = state.deviceStatus,
                            onClick = {
                                viewModel.onDeviceChangeClicked()
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                // Quick Actions Grid
                item {
                    QuickActionsGrid(
                        onQRScanClick = {
                            navController.navigate(AttendanceRoute("QRScanner"))
                        },
                        onManualCodeClick = {
                            navController.navigate(AttendanceRoute("CodeEntry"))
                        },
                        onNotificationClick = {
                            navController.navigate(NotificationsRoute)
                        },
                        modifier = Modifier.animateItem()
                    )
                }

                // Recent Activity
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {
                                navController.navigate(AttendanceHistoryRoute)
                            }) {
                                Text("View All")
                            }
                        }
                        RecentActivitySection(
                            recentAttendance = state.recentAttendance,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

            }

            // Pull to refresh indicator
            PullToRefreshIndicator(
                isRefreshing = state.isRefreshing,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopAppBar(
    studentName: String,
    registrationNo: String,
    currentSemester: Int,
    date: String,
    //scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        //scrollBehavior = scrollBehavior,
        title = {
            Row(
                modifier = modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Hello,",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = studentName.split(" ").firstOrNull() ?: "Student",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = registrationNo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Current Semester/Academic Year Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Current Semester",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Sem ${currentSemester}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun QuickStatsRow(
    totalSessions: Int,
    attendanceRate: Int,
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Sessions Card
        StatCard(
            title = "Total Sessions",
            value = totalSessions.toString(),
            icon = Icons.Default.History,
            gradient = listOf(
                Color(0xFF667EEA),
                Color(0xFF764BA2)
            ),
            modifier = Modifier.weight(1f)
        )

        // Attendance Rate Card
        StatCard(
            title = "Attendance",
            value = "$attendanceRate%",
            icon = Icons.Default.CheckCircle,
            gradient = listOf(
                Color(0xFF38A169),
                Color(0xFF2F855A)
            ),
            modifier = Modifier.weight(1f)
        )

        // Current Streak Card
        StatCard(
            title = "Streak",
            value = "$currentStreak",
            suffix = "d",
            icon = Icons.Default.LocalFireDepartment,
            gradient = listOf(
                Color(0xFFF6AD55),
                Color(0xFFDD6B20)
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    suffix: String = "",
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradient,
                        startY = 0f,
                        endY = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = value + suffix,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayScheduleSection(
    sessions: List<TodaySession>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${sessions.size} sessions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sessions.isEmpty()) {
                EmptyScheduleState()
            } else {
                sessions.forEachIndexed { index, session ->
                    SessionTimeCard(
                        session = session,
                        onClick = { onSessionClick(session.id) }
                    )
                    if (index < sessions.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionTimeCard(
    session: TodaySession,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(50.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = session.startTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = session.endTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Session Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = session.unitName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = session.unitCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(
                    label = session.type,
                    containerColor = getSessionTypeColor(session.type).copy(alpha = 0.1f),
                    labelColor = getSessionTypeColor(session.type)
                )
                if (session.isOngoing) {
                    Chip(
                        label = "LIVE",
                        containerColor = Color.Red.copy(alpha = 0.1f),
                        labelColor = Color.Red
                    )
                }
            }
        }

        // Status Icon
        if (session.status == "ATTENDED") {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Attended",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Available",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun Chip(
    label: String,
    containerColor: Color,
    labelColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyScheduleState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = "No sessions",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Text(
            text = "No sessions scheduled for today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Enjoy your day off!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun QuickActionsGrid(
    onQRScanClick: () -> Unit,
    onManualCodeClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // QR Scan Action
            QuickActionCard(
                title = "QR Scan",
                subtitle = "Fast & secure",
                icon = Icons.Default.QrCodeScanner,
                gradient = listOf(
                    Color(0xFF4158D0),
                    Color(0xFFC850C0)
                ),
                onClick = onQRScanClick,
                modifier = Modifier.weight(1f)
            )

            // Manual Code Action
            QuickActionCard(
                title = "Manual Code",
                subtitle = "Enter session code",
                icon = Icons.Default.Keyboard,
                gradient = listOf(
                    Color(0xFF0093E9),
                    Color(0xFF80D0C7)
                ),
                onClick = onManualCodeClick,
                modifier = Modifier.weight(1f)
            )

            // Notification Action
            QuickActionCard(
                title = "Notification",
                subtitle = "View notifications",
                icon = Icons.Default.Notifications,
                gradient = listOf(
                    Color(0xFF8E2DE2),
                    Color(0xFF4A00E0)
                ),
                onClick = onNotificationClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradient,
                        startY = 0f,
                        endY = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentActivitySection(
    recentAttendance: List<RecentAttendance>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            if (recentAttendance.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No recent attendance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                recentAttendance.take(3).forEach { attendance ->
                    RecentActivityItem(attendance = attendance)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityItem(
    attendance: RecentAttendance
) {

    val icon = remember { getMethodIcon(attendance.method) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = attendance.unitName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${attendance.date} • ${attendance.time}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun DeviceWarningCard(
    deviceStatus: DeviceStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = when (deviceStatus) {
        DeviceStatus.PENDING -> WarningCardConfig(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            icon = Icons.Default.HourglassEmpty,
            title = "Device Change Pending",
            message = "Your device change request is waiting for approval from your lecturer or admin."
        )
        DeviceStatus.REJECTED -> WarningCardConfig(
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            icon = Icons.Default.Error,
            title = "Device Change Rejected",
            message = "Your device change request was rejected. Please contact your lecturer or admin."
        )
        else -> WarningCardConfig(
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            icon = Icons.Default.Info,
            title = "New Device Detected",
            message = "You're using a new device. Please request a device change to continue."
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = config.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                //modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = config.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = config.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text =  if (deviceStatus == DeviceStatus.PENDING) "View Device Status" else "Request Change")
            }

        }
    }
}

data class WarningCardConfig(
    val backgroundColor: Color,
    val icon: ImageVector,
    val title: String,
    val message: String
)

// Helper function for gradient colors
@Composable
private fun getSessionTypeColor(type: String): Color {
    return when (type) {
        "REGULAR" -> MaterialTheme.colorScheme.primary
        "MAKEUP" -> MaterialTheme.colorScheme.secondary
        "SPECIAL" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun getCurrentFormattedDate(): String {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    return dateFormat.format(calendar.time)
}
