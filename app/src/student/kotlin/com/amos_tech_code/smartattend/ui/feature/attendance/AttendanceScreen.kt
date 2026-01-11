package com.amos_tech_code.smartattend.ui.feature.attendance

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.feature.home.AttendanceStatus
import com.amos_tech_code.smartattend.ui.feature.home.RecentAttendance
import com.amos_tech_code.smartattend.ui.feature.home.Session
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun AttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = koinViewModel()
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is AttendanceEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Base screen content
        when {
            state.showQRScanner -> {
                QRScannerScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.onEvent(AttendanceUiEvent.ResetState) }
                )
            }
            state.showCodeEntry -> {
                CodeEntryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.onEvent(AttendanceUiEvent.ResetState) }
                )
            }
            state.showSuccess -> {
                state.attendanceResult?.let { result ->
                    AttendanceSuccessScreen(
                        result = result,
                        onBack = { viewModel.onEvent(AttendanceUiEvent.ResetState) }
                    )
                }
            }
            // Add location capture screen when needed
            state.showLocationCapture -> {
                LocationCaptureScreen(
                    viewModel = viewModel,
                    isFromQR = state.showQRScanner,
                    onBack = { viewModel.onEvent(AttendanceUiEvent.ResetState) }
                )
            }
            else -> {
                MainAttendanceScaffold(navController = navController, viewModel = viewModel, state = state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAttendanceScaffold(
    navController: NavController,
    viewModel: AttendanceViewModel,
    state: StudentAttendanceState
) {
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
                        viewModel.showQrScanner()
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
            if (state.isLoading && state.activeSessions.isEmpty()) {
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
            state.errorMessage?.let {
                item {
                    ErrorMessageCard(
                        message = it,
                        onRetry = { /* Handle retry if needed */ }
                    )
                }
            }

            // Active Sessions or Empty State
            if (state.activeSessions.isNotEmpty()) {
                item {
                    ActiveSessionsSection(
                        sessions = state.activeSessions,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }


            // Attendance Methods
            item {
                AttendanceMethodsSection(
                    onScanQR = { viewModel.showQrScanner() },
                    onEnterCode = { viewModel.showCodeEntry() },
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
            text = "How to Mark Attendance",
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
                description = "Scan the lecturer's QR code.",
                onClick = onScanQR
            )

            AttendanceMethodCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Pin,
                title = "Enter Code",
                description = "Use the lecturer's code.",
                onClick = onEnterCode
            )
        }
    }
}

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            SmartAttendHeightSpacer(8.dp)
            Text(text = title, fontWeight = FontWeight.Bold)
            SmartAttendHeightSpacer(4.dp)
            Text(text = description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
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

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sessions) {
                // Replace with your ActiveSessionCard
            }
        }
    }
}

@Composable
fun RecentAttendanceSection(recentAttendance: List<RecentAttendance>, modifier: Modifier = Modifier) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCaptureScreen(
    viewModel: AttendanceViewModel,
    isFromQR: Boolean,
    onBack: () -> Unit
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Location Verification",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LocationCaptureOverlay(
            locationState = state.locationState,
            location = state.studentLocation,
            locationError = state.locationError,
            onCaptureLocation = { viewModel.onEvent(AttendanceUiEvent.RequestLocation) },
            onRecapture = { viewModel.onEvent(AttendanceUiEvent.RetryLocationCapture) },
            onProceed = {
                // Determine which flow to return to
                if (isFromQR) {
                    state.currentSessionCode?.let { sessionCode ->
                        state.currentUnitCode?.let { unitCode ->
                            viewModel.markAttendanceDirectlyFromQR(sessionCode, unitCode)
                        }
                    }
                } else {
                    state.currentSessionCode?.let { sessionCode ->
                        state.currentUnitCode?.let { unitCode ->
                            viewModel.markAttendanceDirectlyFromCode(sessionCode, unitCode)
                        }
                    }
                }
            },
            isProceedEnabled = state.studentLocation != null,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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