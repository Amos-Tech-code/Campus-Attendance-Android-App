package com.amos_tech_code.smartattend.ui.feature.attendance

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.EmptyState
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonSize
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.feature.home.AttendanceStatus
import com.amos_tech_code.smartattend.ui.feature.home.RecentAttendance
import com.amos_tech_code.smartattend.ui.feature.home.Session
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = koinViewModel()
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.event) { event ->
        when(event) {
            is AttendanceEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
            is AttendanceEvent.AttendanceMarkedSuccessfully -> {
                // Show success and reset state after delay
//                scope.launch {
//                    delay(3000)
//                    viewModel.onEvent(AttendanceUiEvent.ResetState)
//                }
            }
            AttendanceEvent.NavigateToQRScanner -> {
                // Handled by state
            }
            AttendanceEvent.NavigateToCodeEntry -> {
                // Handled by state
            }
        }
    }

    // Handle QR Scanner
    if (state.showQRScanner) {
        QRScannerScreen(
            viewModel = viewModel,
            onBack = {
                viewModel.onEvent(AttendanceUiEvent.ResetState)
            }
        )
        return
    }

    // Handle Code Entry
    if (state.showCodeEntry) {
        CodeEntryScreen(
            viewModel = viewModel,
            onBack = {
                viewModel.onEvent(AttendanceUiEvent.ResetState)
            }
        )
        return
    }

    // Handle Programme Selection
    if (state.showProgrammeSelection) {
        state.verificationResult?.availableProgrammes?.let { programmes ->
            ProgrammeSelectionDialog(
                programmes = programmes,
                onProgrammeSelected = { programmeId ->
                    viewModel.onEvent(AttendanceUiEvent.ProgrammeSelected(programmeId))
                },
                onDismiss = {
                    viewModel.onEvent(AttendanceUiEvent.ResetState)
                }
            )
        }
    }

    // Handle Success Screen
    if (state.showSuccess) {
        state.attendanceResult?.let { result ->
            AttendanceSuccessScreen(
                result = result,
                onBack = {
                    viewModel.onEvent(AttendanceUiEvent.ResetState)
                }
            )
        }
        return
    }

    // Main Attendance Screen
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mark Attendance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        floatingActionButton = {
            if (state.activeSessions.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.onEvent(AttendanceUiEvent.ResetState)
                        // This will trigger QR scanner through state change
                    },
                    icon = {
                        Icon(Icons.Default.QrCode, "Scan QR")
                    },
                    text = {
                        Text("Quick Scan")
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading State
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Error Message
            state.errorMessage?.let { error ->
                item {
                    ErrorMessageCard(
                        message = error,
                        onRetry = { /* Handle retry if needed */ }
                    )
                }
            }

            // Active Sessions
            item {
                if (state.activeSessions.isNotEmpty()) {
                    ActiveSessionsSection(
                        sessions = state.activeSessions,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Attendance Methods
            item {
                AttendanceMethodsSection(
                    onScanQR = {
                        viewModel.onEvent(AttendanceUiEvent.ResetState)
                        // This will trigger QR scanner
                        viewModel.showQrScanner()
                    },
                    onEnterCode = {
                        viewModel.onEvent(AttendanceUiEvent.ResetState)
                        // This will trigger code entry
                        viewModel.showCodeEntry()
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Recent Attendance
            item {
                RecentAttendanceSection(
                    recentAttendance = state.recentAttendance,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AttendanceMethodsSection(
    onScanQR: () -> Unit,
    onEnterCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Mark Attendance",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(16.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AttendanceMethodCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.QrCode2,
                title = "Scan QR",
                description = "Scan lecturer's QR code",
                onClick = onScanQR
            )

            AttendanceMethodCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Pin,
                title = "Enter Code",
                description = "Use lecturer's code",
                onClick = onEnterCode
            )
        }
    }
}


@Composable
fun ActiveSessionsSection(
    sessions: List<Session>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Active Sessions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        SmartAttendHeightSpacer(12.dp)

        LazyRow (
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions) { session ->
                ActiveSessionCard(session = session)
            }
        }
    }
}

/*
@Composable
private fun AttendanceMethodsSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Mark Attendance",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(16.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AttendanceMethodCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.QrCode2,
                title = "Scan QR",
                description = "Scan lecturer's QR code",
                onClick = { /* Navigate to QR scanner */ }
            )

            AttendanceMethodCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Pin,
                title = "Enter Code",
                description = "Use lecturer's code",
                onClick = { /* Navigate to code input */ }
            )
        }
    }
}
*/
@Composable
private fun AttendanceMethodCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun ActiveSessionCard(session: Session) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.courseName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = session.courseCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = PresentColor,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconText(
                    icon = Icons.Default.Schedule,
                    text = session.time,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                IconText(
                    icon = Icons.Default.LocationOn,
                    text = session.location,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            SmartAttendPrimaryButton(
                text = "Mark Attendance",
                onClick = { /* Handle attendance */ },
                size = SmartAttendButtonSize.Medium
            )
        }
    }
}

@Composable
private fun RecentAttendanceSection(
    recentAttendance: List<RecentAttendance>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recent Attendance",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(12.dp)

        if (recentAttendance.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                title = "No recent attendance",
                description = "Your attendance records will appear here"
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                recentAttendance.take(5).forEach { attendance ->
                    RecentAttendanceItem(attendance = attendance)
                }
            }
        }
    }
}

@Composable
private fun RecentAttendanceItem(attendance: RecentAttendance) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Icon(
                imageVector = when (attendance.status) {
                    AttendanceStatus.PRESENT -> Icons.Default.CheckCircle
                    AttendanceStatus.ABSENT -> Icons.Default.Cancel
                    AttendanceStatus.LATE -> Icons.Default.Schedule
                    AttendanceStatus.PENDING -> Icons.Default.Pending
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when (attendance.status) {
                    AttendanceStatus.PRESENT -> PresentColor
                    AttendanceStatus.ABSENT -> AbsentColor
                    AttendanceStatus.LATE -> PendingColor
                    AttendanceStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = attendance.courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Via ${attendance.method.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (attendance.location != null) {
                    Text(
                        text = attendance.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = attendance.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun IconText(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}


@Composable
fun ErrorMessageCard(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}