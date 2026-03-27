package com.amos_tech_code.smartattend.ui.feature.student_lookup

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.ActivityType
import com.amos_tech_code.smartattend.domain.response.AttendanceSummary
import com.amos_tech_code.smartattend.domain.response.DeviceLookupInfo
import com.amos_tech_code.smartattend.domain.response.EnrollmentInfo
import com.amos_tech_code.smartattend.domain.response.PendingDeviceChangeInfo
import com.amos_tech_code.smartattend.domain.response.RecentActivityInfo
import com.amos_tech_code.smartattend.domain.response.StudentBasicInfo
import com.amos_tech_code.smartattend.domain.response.StudentLookupUnitInfo
import com.amos_tech_code.smartattend.ui.navigation.DeviceChangeRoute
import com.amos_tech_code.smartattend.ui.theme.Warning40
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import com.amos_tech_code.smartattend.utils.formatDate
import com.amos_tech_code.smartattend.utils.formatDateTime
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLookupScreen(
    navController: NavController,
    viewModel: StudentLookupViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is StudentLookupEvent.ShowError -> {
                scope.launch { snackbarHostState.showSnackbar(event.message) }
            }
            is StudentLookupEvent.ShowSuccess -> {
                scope.launch { snackbarHostState.showSnackbar(event.message) }
            }
            is StudentLookupEvent.ClearData -> {
                focusManager.clearFocus()
            }
            is StudentLookupEvent.NavigateToDeviceApproval -> {
                navController.navigate(DeviceChangeRoute)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Student Lookup",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Input Section
                item {
                    SearchInputCard(
                        registrationNumber = state.registrationNumber,
                        onValueChange = { viewModel.updateRegistrationNumber(it) },
                        onClear = { viewModel.clearSearch() },
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.searchStudent()
                        },
                        isEnabled = state.isSearchEnabled,
                        isLoading = state.isLoading
                    )
                }

                // Loading State
                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Error State
                if (state.error != null && state.studentData == null) {
                    item {
                        ErrorCard(
                            message = state.error ?: "An error occurred",
                            onRetry = { viewModel.searchStudent() }
                        )
                    }
                }

                // Student Data Display
                state.studentData?.let { data ->
                    // Student Basic Info
                    item {
                        StudentInfoCard(studentInfo = data.studentInfo)
                    }

                    // Device Info
                    item {
                        DeviceInfoCard(deviceInfo = data.deviceInfo)
                    }

                    // Sign Attendance Button (Only for successful search)
                    item {
                        SignAttendanceButton(
                            studentName = data.studentInfo.fullName,
                            onClick = { viewModel.showSignAttendanceSheet() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Pending Device Change Warning
                    data.pendingDeviceChange?.let { pending ->
                        item {
                            PendingDeviceChangeCard(
                                pendingRequest = pending,
                                onApproveClick = { viewModel.onApproveDeviceClicked() }
                            )
                        }
                    }

                    // Attendance Summary
                    item {
                        AttendanceSummaryCard(summary = data.attendanceSummary)
                    }

                    // Enrollment Information
                    if (data.enrollmentInfo.isNotEmpty()) {
                        item {
                            Text(
                                text = "Academic Information",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(data.enrollmentInfo) { enrollment ->
                            EnrollmentCard(enrollment = enrollment)
                        }
                    }

                    // Recent Activity
                    if (data.recentActivity.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(data.recentActivity) { activity ->
                            ActivityItemCard(activity = activity)
                        }
                    }
                }
            }

            // Sign Attendance Bottom Sheet
            if (state.showSignAttendanceSheet && state.studentData != null) {
                SignAttendanceBottomSheet(
                    studentName = state.studentData!!.studentInfo.fullName,
                    studentRegNo = state.studentData!!.studentInfo.registrationNumber,
                    sessionCode = state.signAttendanceSessionCode,
                    unitCode = state.signAttendanceUnitCode,
                    onSessionCodeChange = { viewModel.updateSignAttendanceData(it, state.signAttendanceUnitCode) },
                    onUnitCodeChange = { viewModel.updateSignAttendanceData(state.signAttendanceSessionCode, it) },
                    onSubmit = { viewModel.signAttendanceForStudent() },
                    onDismiss = { viewModel.hideSignAttendanceSheet() },
                    isSubmitting = state.isSubmittingAttendance
                )
            }
        }
    }
}

@Composable
fun SearchInputCard(
    registrationNumber: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Search Student",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Enter the student's registration number to view their details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = registrationNumber,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., SC211/0483/2022") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (registrationNumber.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() }
                ),
                shape = RoundedCornerShape(12.dp),
                isError = registrationNumber.isNotEmpty() && registrationNumber.length < 8
            )

            if (registrationNumber.isNotEmpty() && registrationNumber.length < 8) {
                Text(
                    text = "Registration number seems too short. Please check the format.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching...")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lookup Student")
                }
            }
        }
    }
}

@Composable
fun StudentInfoCard(studentInfo: StudentBasicInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentInfo.fullName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = studentInfo.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = studentInfo.registrationNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                studentInfo.lastLoginAt?.let {
                    Text(
                        text = "Last login: ${it.formatDate()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (studentInfo.isActive)
                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                else
                    Color(0xFFF44336).copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (studentInfo.isActive) "Active" else "Inactive",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (studentInfo.isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun DeviceInfoCard(deviceInfo: DeviceLookupInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Device Information",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = deviceInfo.deviceModel ?: "No device registered",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                deviceInfo.deviceStatus?.let {
                    Text(
                        text = "Status: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = when (it) {
                            "ACTIVE" -> Color(0xFF4CAF50)
                            "PENDING" -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SignAttendanceButton(
    studentName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Attendance for ${studentName.take(15)}${if (studentName.length > 15) "..." else ""}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PendingDeviceChangeCard(
    pendingRequest: PendingDeviceChangeInfo,
    onApproveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Warning40
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pending Device Change Request",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "New device: ${pendingRequest.newDeviceModel} (${pendingRequest.newDeviceOS})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Requested: ${pendingRequest.requestedAt.formatDate()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onApproveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = "Review & Approve Request",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AttendanceSummaryCard(summary: AttendanceSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${summary.sessionsAttended}/${summary.totalSessions}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.overallAttendance >= 80)
                        Color(0xFF4CAF50)
                    else if (summary.overallAttendance >= 60)
                        Color(0xFFFF9800)
                    else
                        Color(0xFFF44336)
                )
            }

            LinearProgressIndicator(
                progress = (summary.overallAttendance / 100).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (summary.overallAttendance >= 80)
                    Color(0xFF4CAF50)
                else if (summary.overallAttendance >= 60)
                    Color(0xFFFF9800)
                else
                    Color(0xFFF44336)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Overall Attendance: ${String.format("%.1f", summary.overallAttendance)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Suspicious Activities: ${summary.suspiciousActivities}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (summary.suspiciousActivities > 0)
                        Color(0xFFF44336)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            summary.lastAttendanceDate?.let {
                Text(
                    text = "Last attendance: ${it.formatDate()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EnrollmentCard(enrollment: EnrollmentInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = enrollment.programmeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Year ${enrollment.yearOfStudy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = enrollment.academicTerm,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider()

            Text(
                text = "Units",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )

            enrollment.units.forEach { unit ->
                UnitProgressRow(unit = unit)
            }
        }
    }
}

@Composable
fun UnitProgressRow(unit: StudentLookupUnitInfo) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (unit.isTeaching) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${unit.unitCode} - ${unit.unitName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (unit.isTeaching) FontWeight.Medium else FontWeight.Normal
                )
            }
            Text(
                text = "${unit.sessionsAttended}/${unit.totalSessions}",
                style = MaterialTheme.typography.labelSmall
            )
        }

        LinearProgressIndicator(
            progress = (unit.attendancePercentage / 100).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                unit.attendancePercentage >= 80 -> Color(0xFF4CAF50)
                unit.attendancePercentage >= 60 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
        )
    }
}

@Composable
fun ActivityItemCard(activity: RecentActivityInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = activity.activityType.getColor().copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.activityType.getIcon(),
                    contentDescription = null,
                    tint = activity.activityType.getColor(),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.timestamp.formatDateTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (activity.activityType == ActivityType.SUSPICIOUS_ACTIVITY_DETECTED) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF44336).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Alert",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Try Again")
            }
        }
    }
}

// Extension functions for ActivityType
fun ActivityType.getIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (this) {
        ActivityType.ATTENDANCE_MARKED -> Icons.Default.CheckCircle
        ActivityType.ATTENDANCE_REVOKED -> Icons.Default.Cancel
        ActivityType.DEVICE_CHANGE_REQUESTED -> Icons.Default.Pending
        ActivityType.DEVICE_CHANGE_APPROVED -> Icons.Default.Verified
        ActivityType.DEVICE_CHANGE_REJECTED -> Icons.Default.Error
        ActivityType.DEVICE_CHANGE_CANCELLED -> Icons.Default.Cancel
        ActivityType.SESSION_STARTED -> Icons.Default.PlayArrow
        ActivityType.SESSION_ENDED -> Icons.Default.Stop
        ActivityType.PROFILE_UPDATED -> Icons.Default.Person
        ActivityType.SUSPICIOUS_ACTIVITY_DETECTED -> Icons.Default.Warning
    }
}

fun ActivityType.getColor(): Color {
    return when (this) {
        ActivityType.ATTENDANCE_MARKED -> Color(0xFF4CAF50)
        ActivityType.ATTENDANCE_REVOKED -> Color(0xFFF44336)
        ActivityType.DEVICE_CHANGE_APPROVED -> Color(0xFF4CAF50)
        ActivityType.DEVICE_CHANGE_REJECTED -> Color(0xFFF44336)
        ActivityType.DEVICE_CHANGE_REQUESTED -> Color(0xFFFF9800)
        ActivityType.DEVICE_CHANGE_CANCELLED -> Color(0xFF9E9E9E)
        ActivityType.SESSION_STARTED -> Color(0xFF2196F3)
        ActivityType.SESSION_ENDED -> Color(0xFF9E9E9E)
        ActivityType.PROFILE_UPDATED -> Color(0xFF9C27B0)
        ActivityType.SUSPICIOUS_ACTIVITY_DETECTED -> Color(0xFFF44336)
    }
}