package com.amos_tech_code.smartattend.ui.feature.student_lookup

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.ui.components.EmptyState
import com.amos_tech_code.smartattend.ui.components.SmartAttendButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonStyle
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendSecondaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLookupScreen(
    navController: NavController,
    viewModel: StudentLookupViewModel = koinViewModel()
) {
    val focusManager = LocalFocusManager.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is StudentLookupEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_LONG).show()
            }
            is StudentLookupEvent.DeviceRequestUpdated -> {
                val actionText = when (event.action) {
                    "approved" -> "approved"
                    "rejected" -> "rejected"
                    else -> "updated"
                }
                Toast.makeText(navController.context, "Device request $actionText", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Student Lookup",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Find Student",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Search Input
                    SmartAttendTextField(
                        value = state.searchQuery,
                        onValueChange = { },
                        label = "Registration Number or Name",
                        placeholder = "Enter student registration number or name",
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = {  }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                viewModel.onEvent(StudentLookupUiEvent.SearchStudent)
                            }
                        )
                    )

                    // Search Button
                    SmartAttendPrimaryButton(
                        text = "Search Student",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onEvent(StudentLookupUiEvent.SearchStudent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = state.isSearching
                    )
                }
            }

            // Results Section
            when {
                state.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.searchedStudent != null -> {
                    StudentDetailsSection(
                        student = state.searchedStudent!!,
                        attendanceRecords = state.attendanceRecords,
                        deviceRequests = state.pendingDeviceRequests,
                        onApproveDevice = { requestId ->
                            viewModel.onEvent(StudentLookupUiEvent.ApproveDeviceRequest(requestId))
                        },
                        onRejectDevice = { requestId ->
                            viewModel.onEvent(StudentLookupUiEvent.RejectDeviceRequest(requestId))
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
                state.searchPerformed && state.searchQuery.isNotEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.PersonOff,
                        title = "Student Not Found",
                        description = "No student found with the provided registration number or name",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // Pending Device Requests
                    if (state.pendingDeviceRequests.isNotEmpty()) {
                        PendingDeviceRequestsSection(
                            requests = state.pendingDeviceRequests,
                            onApprove = { requestId ->
                                viewModel.onEvent(StudentLookupUiEvent.ApproveDeviceRequest(requestId))
                            },
                            onReject = { requestId ->
                                viewModel.onEvent(StudentLookupUiEvent.RejectDeviceRequest(requestId))
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(16.dp)
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "Search for Students",
                            description = "Enter a student's registration number or name to view their details and manage device requests",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentDetailsSection(
    student: StudentWithDetails,
    attendanceRecords: List<AttendanceRecord>,
    deviceRequests: List<DeviceChangeRequest>,
    onApproveDevice: (String) -> Unit,
    onRejectDevice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        // Student Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student.registrationNo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${student.department} • ${student.semester}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Device Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Device Status",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (student.deviceVerified) "Verified" else "Pending Verification",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (student.deviceVerified) PresentColor else PendingColor
                        )
                    }
                    Text(
                        text = "Last login: ${student.lastLogin}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Pending Device Requests for this student
        val studentRequests = deviceRequests.filter { it.studentId == student.id }
        if (studentRequests.isNotEmpty()) {
            DeviceRequestsSection(
                requests = studentRequests,
                onApprove = onApproveDevice,
                onReject = onRejectDevice,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Attendance History
        AttendanceHistorySection(
            records = attendanceRecords,
            modifier = Modifier.padding(16.dp)
        )

        // Manual Actions
        ManualActionsSection(
            student = student,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun PendingDeviceRequestsSection(
    requests: List<DeviceChangeRequest>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Pending Device Requests",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(12.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            requests.forEach { request ->
                DeviceRequestItem(
                    request = request,
                    onApprove = { onApprove(request.id) },
                    onReject = { onReject(request.id) }
                )
            }
        }
    }
}

@Composable
private fun DeviceRequestItem(
    request: DeviceChangeRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student Info
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = request.studentName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = request.studentRegNo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = request.requestDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Request Details
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Device Change Request",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Student requests to use a new device for attendance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (request.newDeviceInfo.isNotEmpty()) {
                    Text(
                        text = "New device: ${request.newDeviceInfo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartAttendButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    buttonStyle = SmartAttendButtonStyle.Error
                ) {
                    Text("Reject")
                }
//                SmartAttendSecondaryButton(
//                    text = "Reject",
//                    onClick = onReject,
//                    modifier = Modifier.weight(1f),
//                )
                SmartAttendPrimaryButton(
                    text = "Approve",
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DeviceRequestsSection(
    requests: List<DeviceChangeRequest>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Device Change Requests",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(8.dp)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests) { request ->
                DeviceRequestItem(
                    request = request,
                    onApprove = { onApprove(request.id) },
                    onReject = { onReject(request.id) }
                )
            }
        }
    }
}

@Composable
private fun AttendanceHistorySection(
    records: List<AttendanceRecord>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recent Attendance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(8.dp)

        if (records.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                title = "No Attendance Records",
                description = "No attendance records found for this student",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                records.take(5).forEach { record ->
                    AttendanceRecordItem(record = record)
                }
            }
        }
    }
}


@Composable
fun AttendanceRecordItem(
    record: AttendanceRecord,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Course Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = record.courseName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = record.courseCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                StatusBadge(record = record)
            }

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date and Time
                IconText(
                    icon = Icons.Default.Schedule,
                    text = record.displayDateTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Method
                IconText(
                    icon = when (record.method) {
                        AttendanceMethod.QR_CODE -> Icons.Default.QrCode
                        AttendanceMethod.MANUAL_CODE -> Icons.Default.Pin
                        AttendanceMethod.ANY -> Icons.Default.AllInclusive
                        //AttendanceMethod.LECTURER_MANUAL -> Icons.Default.Person
                    },
                    text = record.method.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Verification and Location Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verification Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (record.verified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = PresentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Verified",
                            style = MaterialTheme.typography.labelSmall,
                            color = PresentColor,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Pending,
                            contentDescription = "Pending",
                            tint = PendingColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pending Review",
                            style = MaterialTheme.typography.labelSmall,
                            color = PendingColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Location and Distance
                if (record.location != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = record.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        record.distance?.let { distance ->
                            Text(
                                text = "(${distance}m)",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    distance <= 50 -> PresentColor
                                    distance <= 100 -> PendingColor
                                    else -> AbsentColor
                                }
                            )
                        }
                    }
                }
            }

            // Security Indicators (if any issues)
            if (!record.deviceVerified || !record.locationVerified) {
                SecurityIssuesRow(record = record)
            }
        }
    }
}

@Composable
private fun StatusBadge(record: AttendanceRecord) {
    Box(
        modifier = Modifier
            .background(
                color = record.statusColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = record.statusIcon,
                contentDescription = null,
                tint = record.statusColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = record.status.name,
                style = MaterialTheme.typography.labelSmall,
                color = record.statusColor,
                fontWeight = FontWeight.Medium
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
private fun SecurityIssuesRow(record: AttendanceRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!record.deviceVerified) {
            SecurityIssueItem(
                icon = Icons.Default.PhoneAndroid,
                text = "Device Not Verified",
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!record.locationVerified) {
            SecurityIssueItem(
                icon = Icons.Default.LocationOff,
                text = "Location Issue",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SecurityIssueItem(
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
            contentDescription = text,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ManualActionsSection(
    student: StudentWithDetails,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Manual Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(8.dp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartAttendSecondaryButton(
                text = "Mark Present",
                onClick = { /* Handle manual present */ },
                modifier = Modifier.weight(1f)
            )
            SmartAttendSecondaryButton(
                text = "Reset Device",
                onClick = { /* Handle device reset */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}