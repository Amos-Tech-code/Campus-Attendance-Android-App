package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.ConfirmActionDialog
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.profileState.collectAsStateWithLifecycle()
    var showConfirmLogOutDialog by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.event) { event ->
        when(event) {
            is ProfileEvent.NavigateToLogin -> {
                navController.navigate(SignInRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            ProfileHeader(
                student = state.student,
                modifier = Modifier.padding(16.dp)
            )

            // Device Information
            DeviceInfoSection(
                deviceInfo = state.deviceInfo,
                modifier = Modifier.padding(16.dp)
            )

            // Settings
            SettingsSection(
                isLoggingOut = state.isLoggingOut,
                onLogout = { showConfirmLogOutDialog = true },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showConfirmLogOutDialog) {
        ConfirmActionDialog(
            title = "Logout",
            message = "Are you sure you want to logout?",
            isDestructive = true,
            onDismiss = {
                showConfirmLogOutDialog = false
            },
            onConfirm = {
                showConfirmLogOutDialog = false
                viewModel.logOut()
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    student: Student,
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
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.first().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Student Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = student.registrationNo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Additional Info

        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeviceInfoSection(
    deviceInfo: DeviceInfoUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Device Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(12.dp)

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Device Status",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (deviceInfo.isCurrentDevice) "Registered & Active" else "Different Device",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (deviceInfo.isCurrentDevice) PresentColor else AbsentColor
                        )
                    }
                    Icon(
                        imageVector = if (deviceInfo.isCurrentDevice) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (deviceInfo.isCurrentDevice) PresentColor else AbsentColor
                    )
                }

                // Device Details
                InfoRow(
                    label = "Device ID",
                    value = deviceInfo.deviceId,
                    showCopy = true
                )
                InfoRow(
                    label = "Device Model",
                    value = deviceInfo.deviceModel
                )
                InfoRow(
                    label = "Registered On",
                    value = deviceInfo.registrationDate
                )
            }
        }

        SmartAttendHeightSpacer(8.dp)

        if (!deviceInfo.isCurrentDevice) {
            Text(
                text = "You're viewing from a different device. Some features may be limited.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    isLoggingOut: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(12.dp)

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                // Notification Settings
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage your notification preferences",
                    onClick = { /* Navigate to notifications */ }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // Privacy Settings
                SettingItem(
                    icon = Icons.Default.Security,
                    title = "Privacy & Security",
                    subtitle = "Control your privacy settings",
                    onClick = { /* Navigate to privacy */ }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // Help & Support
                SettingItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Help & Support",
                    subtitle = "Get help with the app",
                    onClick = { /* Navigate to help */ }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // About
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "App version and information",
                    onClick = { /* Navigate to about */ }
                )
            }
        }

        SmartAttendHeightSpacer(24.dp)

        // Logout Button
        SmartAttendButton(
            onClick = onLogout,
            isLoading = isLoggingOut,
            modifier = Modifier.fillMaxWidth(),
            buttonStyle = SmartAttendButtonStyle.Error,
            size = SmartAttendButtonSize.Large
        ) {
            Row {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(20.dp)
                )
                SmartAttendWidthSpacer(8.dp)
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    showCopy: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (showCopy) {
                IconButton(
                    onClick = { /* Copy to clipboard */ },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.profileState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirmLogOutDialog by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showEnrollmentInfo by remember { mutableStateOf(false) }
    var showEnrollmentSheet by remember { mutableStateOf(false) }
    var showYearUpdateSheet by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.events) { event ->
            when (event) {
                is ProfileEvent.NavigateToLogin -> {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is ProfileEvent.ShowEditProfile -> {
                    showEditProfile = true
                }
                is ProfileEvent.ShowEnrollmentInfo -> {
                    showEnrollmentInfo = true
                }
                is ProfileEvent.ShowEnrollmentSheet -> {
                    showEnrollmentSheet = true
                }
                is ProfileEvent.ShowYearUpdateSheet -> {
                    showYearUpdateSheet = true
                }
                is ProfileEvent.ShowDeactivateDialog -> {
                    showDeactivateDialog = true
                }
                is ProfileEvent.ShowMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is ProfileEvent.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.error)
                    }
                }
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ProfileTopBar(
                title = "My Profile",
                onSyncClick = { viewModel.syncEnrollment() },
                isLoading = state.isLoading
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showEditProfile() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile"
                    )
                },
                text = {
                    Text("Edit Profile")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header with Avatar
            ProfileHeader(
                student = state.student,
                onEditClick = { viewModel.showEditProfile() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )

            // Enrollment Section
            EnrollmentSection(
                enrollment = state.enrollment,
                showEnrollmentPrompt = state.showEnrollmentPrompt,
                onEnrollClick = { viewModel.showEnrollmentSheet() },
                onInfoClick = { viewModel.showEnrollmentInfo() },
                onYearUpdateClick = { viewModel.showYearUpdateSheet() },
                onDeactivateClick = { viewModel.showDeactivateDialog() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Device Information Card
            DeviceInfoCard(
                deviceInfo = state.deviceInfo,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Settings Cards
            SettingsSection(
                isLoggingOut = state.isLoggingOut,
                onLogout = { showConfirmLogOutDialog = true },
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit Profile Bottom Sheet
    if (showEditProfile) {
        EditProfileBottomSheet(
            student = state.student,
            onDismiss = { showEditProfile = false },
            onSave = { request ->
                viewModel.updateProfile(request)
                showEditProfile = false
            }
        )
    }

    // Enrollment Info Bottom Sheet
    if (showEnrollmentInfo && state.enrollment != null) {
        EnrollmentInfoBottomSheet(
            enrollment = state.enrollment!!,
            onDismiss = { showEnrollmentInfo = false }
        )
    }

    // Enrollment Sheet
    if (showEnrollmentSheet) {
        EnrollmentBottomSheet(
            state = state,
            onDismiss = {
                showEnrollmentSheet = false
                viewModel.clearSearchState()
            },
            onUniversitySearch = { query ->
                viewModel.searchUniversities(query)
            },
            onProgrammeSearch = { universityId, query ->
                viewModel.searchProgrammes(universityId, query)
            },
            onEnroll = { universityId, universityName, programmeId, programmeName ->
                viewModel.enrollStudent(universityId, universityName, programmeId, programmeName)
                showEnrollmentSheet = false
                viewModel.clearSearchState()
            }
        )
    }

    // Year Update Sheet
    if (showYearUpdateSheet && state.enrollment != null) {
        UpdateYearBottomSheet(
            currentYear = state.enrollment!!.yearOfStudy,
            onDismiss = { showYearUpdateSheet = false },
            onUpdate = { newYear ->
                viewModel.updateYearOfStudy(newYear)
                showYearUpdateSheet = false
            }
        )
    }

    // Deactivate Enrollment Dialog
    if (showDeactivateDialog && state.enrollment != null) {
        ConfirmActionDialog(
            title = "Deactivate Enrollment",
            message = "Are you sure you want to deactivate your enrollment? You can enroll again later.",
            confirmText = "Deactivate",
            onDismiss = { showDeactivateDialog = false },
            onConfirm = {
                viewModel.deactivateEnrollment(state.enrollment!!.enrollmentId)
                showDeactivateDialog = false
            }
        )
    }

    // Logout Confirmation Dialog
    if (showConfirmLogOutDialog) {
        ConfirmActionDialog(
            title = "Logout",
            message = "Are you sure you want to logout?",
            confirmText = "Logout",
            isDestructive = true,
            onDismiss = { showConfirmLogOutDialog = false },
            onConfirm = {
                showConfirmLogOutDialog = false
                viewModel.logOut()
            }
        )
    }
}

@Composable
private fun EnrollmentSection(
    enrollment: EnrollmentUiState?,
    showEnrollmentPrompt: Boolean,
    onEnrollClick: () -> Unit,
    onInfoClick: () -> Unit,
    onYearUpdateClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (enrollment != null) {
            // Active Enrollment Card
            ActiveEnrollmentCard(
                enrollment = enrollment,
                onInfoClick = onInfoClick,
                onYearUpdateClick = onYearUpdateClick,
                onDeactivateClick = onDeactivateClick
            )
        } else if (showEnrollmentPrompt) {
            // Enrollment Prompt Card
            EnrollmentPromptCard(onEnrollClick = onEnrollClick)
        }
    }
}

@Composable
private fun ActiveEnrollmentCard(
    enrollment: EnrollmentUiState,
    onInfoClick: () -> Unit,
    onYearUpdateClick: () -> Unit,
    onDeactivateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Current Enrollment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AssistChip(
                    onClick = { /* Status indicator only */ },
                    label = {
                        Text(
                            text = if (enrollment.isActive) "ACTIVE" else "INACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (enrollment.isActive)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        labelColor = if (enrollment.isActive)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    ),
                    border = null,
                    enabled = false
                )
            }

            // Enrollment Details
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = enrollment.programme,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = enrollment.university,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(
                        icon = Icons.Default.Numbers,
                        text = "Year ${enrollment.yearOfStudy}"
                    )
                    InfoChip(
                        icon = Icons.Default.DateRange,
                        text = enrollment.semester
                    )
                }
            }

            // Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                OutlinedButton(
                    onClick = onInfoClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details")
                }

                OutlinedButton(
                    onClick = onYearUpdateClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Year")
                }

                OutlinedButton(
                    onClick = onDeactivateClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deactivate")
                }
            }
        }
    }
}

@Composable
private fun EnrollmentPromptCard(
    onEnrollClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                .copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Complete Your Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Enroll in a university program to access all features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onEnrollClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Enroll Now",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    title: String,
    onSyncClick: () -> Unit,
    isLoading: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        actions = {
            IconButton(
                onClick = onSyncClick,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.shadow(elevation = 2.dp)
    )
}

@Composable
private fun ProfileHeader(
    student: Student,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Avatar with gradient background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(2).uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Student Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = student.registrationNo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (student.university.isNotEmpty()) {
                    Text(
                        text = student.university,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = student.yearOfStudy.toString(),
                    label = "Year",
                    icon = Icons.Default.School
                )

                if (student.programme.isNotEmpty()) {
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    StatItem(
                        value = student.programme.take(3),
                        label = "Program",
                        icon = Icons.Default.Book
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeviceInfoCard(
    deviceInfo: DeviceInfoUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Device Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Registered & Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Device ID",
                    value = deviceInfo.deviceId.take(5) + "...",
                    icon = Icons.Default.Fingerprint
                )

                InfoRow(
                    label = "Device Model",
                    value = deviceInfo.deviceModel,
                    icon = Icons.Default.Devices
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingsSection(
    isLoggingOut: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notifications",
                    onClick = { /* TODO */ }
                )

                SettingItem(
                    icon = Icons.Default.Security,
                    title = "Privacy & Security",
                    subtitle = "Privacy settings",
                    onClick = { /* TODO */ }
                )

                SettingItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help with the app",
                    onClick = { /* TODO */ }
                )

                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "App version 1.0.0",
                    onClick = { /* TODO */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF44336),
                            Color(0xFFD32F2F)
                        )
                    )
                )
                .clickable(
                    enabled = !isLoggingOut,
                    onClick = onLogout
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}