package com.amos_tech_code.smartattend.ui.feature.live_attendance

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearbyError
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.feature.setup.AttendanceStatus
import com.amos_tech_code.smartattend.ui.feature.setup.Session
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveAttendanceScreen(
    sessionId: String = "0",
    navController: NavController,
    viewModel: LiveAttendanceViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Auto-refresh data
    LaunchedEffect(sessionId) {
        //viewModel.startLiveUpdates(sessionId)
    }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is LiveAttendanceEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_LONG).show()
            }
            LiveAttendanceEvent.SessionEnded -> {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Attendance",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = state.session?.courseName ?: "Session",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share session */ }) {
                        Icon(Icons.Default.Share, "Share Session")
                    }
                    IconButton(onClick = {
                        viewModel.endSession()
                    }) {
                        Icon(Icons.Default.Stop, "End Session", tint = MaterialTheme.colorScheme.error)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            SessionInfoBar(
                session = state.session,
                attendanceStats = state.attendanceStats,
                onShowCode = { /* Show code dialog */ },
                onShowQR = { /* Show QR dialog */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Attendance Summary
            AttendanceSummarySection(
                stats = state.attendanceStats,
                modifier = Modifier.padding(16.dp)
            )

            // Tab Layout for Present/Flagged
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabTitles = listOf(
                "Present (${state.presentStudents.size})",
                "Flagged (${state.flaggedStudents.size})"
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Students List
            when (selectedTab) {
                0 -> PresentStudentsList(
                    students = state.presentStudents,
                    modifier = Modifier.weight(1f)
                )
                1 -> FlaggedStudentsList(
                    students = state.flaggedStudents,
                    onResolve = { studentId ->
                        //viewModel.onEvent(LiveAttendanceUiEvent.ResolveFlag(studentId))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SessionInfoBar(
    session: Session?,
    attendanceStats: AttendanceStats,
    onShowCode: () -> Unit,
    onShowQR: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Session Code
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session?.sessionCode ?: "----",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Session Code",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onShowCode,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Show Code",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(
                    onClick = onShowQR,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Show QR",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun PresentStudentsList(
    students: List<StudentAttendance>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(students) { student ->
            StudentAttendanceItem(
                student = student,
                status = AttendanceStatus.PRESENT
            )
        }
    }
}

@Composable
private fun FlaggedStudentsList(
    students: List<StudentAttendance>,
    onResolve: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(students) { student ->
            FlaggedStudentItem(
                student = student,
                onResolve = { onResolve(student.student.registrationNo) }
            )
        }
    }
}

@Composable
private fun StudentAttendanceItem(
    student: StudentAttendance,
    status: AttendanceStatus
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (status) {
                            AttendanceStatus.PRESENT -> PresentColor
                            AttendanceStatus.ABSENT -> AbsentColor
                            AttendanceStatus.LATE -> PendingColor
                            AttendanceStatus.PENDING -> MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )

            // Student Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = student.student.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = student.student.registrationNo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Attendance Details
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = student.timestamp,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (student.distance != null) {
                    Text(
                        text = "${student.distance}m away",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
private fun AttendanceSummarySection(
    stats: AttendanceStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${stats.attendancePercentage}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        stats.attendancePercentage >= 80 -> PresentColor
                        stats.attendancePercentage >= 60 -> PendingColor
                        else -> AbsentColor
                    }
                )
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { stats.attendancePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    stats.attendancePercentage >= 80 -> PresentColor
                    stats.attendancePercentage >= 60 -> PendingColor
                    else -> AbsentColor
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    count = stats.presentCount,
                    label = "Present",
                    color = PresentColor,
                    icon = Icons.Default.CheckCircle
                )
                SummaryStatItem(
                    count = stats.absentCount,
                    label = "Absent",
                    color = AbsentColor,
                    icon = Icons.Default.Cancel
                )
                SummaryStatItem(
                    count = stats.totalStudents,
                    label = "Total",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.People
                )
            }

            // Additional Info
            if (stats.totalStudents > 0) {
                Text(
                    text = "${stats.presentCount} of ${stats.totalStudents} students marked present",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    count: Int,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun FlaggedStudentItem(
    student: StudentAttendance,
    onResolve: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Warning Icon
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Flagged",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )

                    // Student Info
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = student.student.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = student.student.registrationNo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Resolve Button
                FilledTonalButton(
                    onClick = onResolve,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Resolve",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Flag Reasons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Device Verification Status
                if (!student.deviceVerified) {
                    FlagReasonItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Device Mismatch",
                        description = "Student logged in from different device",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Location Verification Status
                if (!student.locationVerified) {
                    FlagReasonItem(
                        icon = Icons.Default.LocationOn,
                        title = "Location Issue",
                        description = "GPS location outside allowed radius",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Distance Warning
                if (student.distance != null && student.distance > 100) {
                    FlagReasonItem(
                        icon = Icons.Default.NearbyError,
                        title = "Distance Warning",
                        description = "Student ${student.distance}m from class",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Attendance Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Marked at ${student.timestamp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Method: ${student.method.name.replace("_", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Distance indicator
                if (student.distance != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = when {
                                    student.distance <= 50 -> PresentColor.copy(alpha = 0.2f)
                                    student.distance <= 100 -> PendingColor.copy(alpha = 0.2f)
                                    else -> AbsentColor.copy(alpha = 0.2f)
                                },
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${student.distance}m",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                student.distance <= 50 -> PresentColor
                                student.distance <= 100 -> PendingColor
                                else -> AbsentColor
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagReasonItem(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(18.dp),
            tint = color
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.labelSmall.lineHeight
            )
        }
    }
}
