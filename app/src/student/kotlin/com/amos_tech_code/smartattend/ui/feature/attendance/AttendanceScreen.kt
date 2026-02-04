package com.amos_tech_code.smartattend.ui.feature.attendance

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.response.SessionInfo
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonSize
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = koinViewModel()
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is AttendanceEvent.ShowErrorMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Base screen content - order matters here!
        when {
            // Success screen has highest priority
            state.showSuccess -> {
                state.attendanceResult?.let { result ->
                    AttendanceSuccessScreen(
                        result = result,
                        onBack = { viewModel.onEvent(AttendanceUiEvent.ResetState) }
                    )
                }
            }
            // Programme selection (after verification, before marking)
            state.showProgrammeSelection -> {
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
            // Location capture (after verification, before programme selection if needed)
            state.showLocationCapture -> {
                LocationCaptureScreen(
                    viewModel = viewModel,
                    context = context,
                    isFromQR = state.showQRScanner,
                    onBack = { viewModel.onEvent(AttendanceUiEvent.CancelLocationCapture) }
                )
            }
            // QR Scanner
            state.showQRScanner -> {
                QRScannerScreen(
                    viewModel = viewModel,
                    context = context,
                    onBack = { viewModel.onEvent(AttendanceUiEvent.ResetState) }
                )
            }
            // Code Entry
            state.showCodeEntry -> {
                CodeEntryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.onEvent(AttendanceUiEvent.ResetState) }
                )
            }
            // Default home screen
            else -> {
                MainAttendanceScaffold(navController = navController, viewModel = viewModel, state = state)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.Center)
        )
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )
        },
//        floatingActionButton = {
//            if (state.activeSessions.isNotEmpty()) {
//                ExtendedFloatingActionButton(
//                    onClick = {
//                        viewModel.showQrScanner()
//                    },
//                    icon = {
//                        Icon(Icons.Default.QrCode, "Scan QR")
//                    },
//                    text = {
//                        Text("Quick Scan")
//                    },
//                    containerColor = MaterialTheme.colorScheme.primary
//                )
//            }
//        },
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
//            if (state.isLoading && state.activeSessions.isEmpty()) {
//                item {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//            }

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
//            if (state.activeSessions.isNotEmpty()) {
//                item {
//                    ActiveSessionsSection(
//                        sessions = state.activeSessions,
//                        modifier = Modifier.padding(16.dp)
//                    )
//                }
//            }


            // Attendance Methods
            item {
                AttendanceMethodsSection(
                    onScanQR = { viewModel.showQrScanner() },
                    onEnterCode = { viewModel.showCodeEntry() },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Recent Attendance
//            item {
//                RecentAttendanceSection(
//                    recentAttendance = state.recentAttendance,
//                    modifier = Modifier.padding(16.dp)
//                )
//            }
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

/*
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
*/

@Composable
fun VerifiedSessionState(
    verificationResult: VerifyAttendanceResponse?,
    locationState: LocationState,
    studentLocation: LocationData?,
    onMarkAttendance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success icon
            Icon(
                Icons.Default.CheckCircle,
                "Verified",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Session Verified",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Session info
            verificationResult?.sessionInfo?.let { sessionInfo ->
                SessionInfoCard(sessionInfo = sessionInfo)
            }

            // Location info if required
            if (verificationResult?.requiresLocation == true) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Location Status:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    when (locationState) {
                        LocationState.CAPTURED -> {
                            Text(
                                text = "✓ Location captured",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Text(
                                text = "Waiting for location...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Mark attendance button
            SmartAttendPrimaryButton(
                text = "Mark Attendance",
                onClick = onMarkAttendance,
                size = SmartAttendButtonSize.Small,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun SessionInfoCard(
    sessionInfo: SessionInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    "Session",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Session Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Unit Information
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(
                    label = "Unit Code:",
                    value = sessionInfo.unitCode,
                    icon = Icons.Default.Code
                )

                InfoRow(
                    label = "Unit Name:",
                    value = sessionInfo.unitName,
                    icon = Icons.AutoMirrored.Filled.MenuBook
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Lecturer Information
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(
                    label = "Lecturer:",
                    value = sessionInfo.lecturerName,
                    icon = Icons.Default.Person
                )

                InfoRow(
                    label = "Session ID:",
                    value = sessionInfo.sessionId.take(8) + "...",
                    icon = Icons.Default.Fingerprint
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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