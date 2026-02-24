package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.University
import com.amos_tech_code.smartattend.ui.components.ConfirmActionDialog
import com.amos_tech_code.smartattend.ui.components.ErrorBanner
import com.amos_tech_code.smartattend.ui.components.ErrorState
import com.amos_tech_code.smartattend.ui.components.LoadingState
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.navigation.AllExportsRoute
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.InstitutionDetailsRoute
import com.amos_tech_code.smartattend.ui.navigation.NotificationsRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.ui.navigation.SettingsRoute
import com.amos_tech_code.smartattend.ui.navigation.SignInRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Observe events
    ObserveAsEvents(viewModel.event) { event ->
            when (event) {
                is ProfileEvent.ShowErrorMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is ProfileEvent.ShowSuccessMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                ProfileEvent.NavigateToInstitutionSetUp -> {
                    navController.navigate(SetUpRoute)
                }

                is ProfileEvent.NavigateToInstitutionDetail -> {
                    navController.navigate(InstitutionDetailsRoute(event.institutionId))
                }

                ProfileEvent.NavigateToNotifications -> {
                    navController.navigate(NotificationsRoute)
                }

                ProfileEvent.NavigateToDataManagement -> {
                    //navController.navigate("data_management")
                }

                ProfileEvent.NavigateToPreferences -> {
                    navController.navigate(SettingsRoute)
                }

                is ProfileEvent.ExportData -> {
                    navController.navigate(AllExportsRoute)
                }

                ProfileEvent.LogOut -> {
                    navController.navigate(SignInRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer.copy(alpha = 0.03f),
                        colorScheme.background
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            ),
        topBar = {
            ProfileTopAppBar(
                isLoading = state.isLoading,
                onLogOut = { showLogoutDialog = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigation(navController)
        }
    )
    { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingState()
                }

                state.errorMessage != null -> {
                    ErrorState(
                        message = state.errorMessage!!,
                        onRetry = { viewModel.onEvent(ProfileUiEvent.RefreshData) }
                    )
                }

                else -> {
                    ProfileContent(
                        state = state,
                        onEvent = viewModel::onEvent,
                        onNavigateToInstitutionDetail = viewModel::navigateToInstitutionDetail,
                        scrollState = scrollState
                    )
                }
            }

            // Loading overlays
            if (state.isSwitchingInstitution || state.isExporting || state.isUpdatingProfile) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.scrim.copy(alpha = 0.3f))
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 48.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = when {
                                state.isSwitchingInstitution -> "Switching Institution..."
                                state.isExporting -> "Exporting Data..."
                                state.isUpdatingProfile -> "Updating Profile..."
                                else -> "Processing..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Edit Name Bottom Sheet
        if (state.showEditNameSheet) {
            EditNameBottomSheet(
                state = state,
                onEvent = viewModel::onEvent
            )
        }
    }

    if (showLogoutDialog) {
        ConfirmActionDialog(
            title = "Log Out",
            message = "Are you sure you want to log out?",
            confirmText = "Log Out",
            isDestructive = true,
            onConfirm = {
                viewModel.onEvent(ProfileUiEvent.LogOut)
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopAppBar(
    isLoading: Boolean,
    onLogOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colorScheme.onPrimaryContainer
            )
        },
        actions = {
            if (!isLoading) {
                IconButton(
                    onClick = onLogOut,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log out",
                        tint = colorScheme.error
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    onEvent: (ProfileUiEvent) -> Unit,
    onNavigateToInstitutionDetail: (String) -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Personal Information Section
        PersonalInformationSection(
            lecturer = state.lecturer,
            onEditProfile = { onEvent(ProfileUiEvent.EditProfile) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        SmartAttendHeightSpacer(24.dp)

        // Active Institution Section
        ActiveInstitutionSection(
            institution = state.activeInstitution,
            onViewDetail = {
                state.activeInstitution?.id?.let { onNavigateToInstitutionDetail(it) }
            },
            onAddInstitution = { onEvent(ProfileUiEvent.ToggleAddInstitution) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SmartAttendHeightSpacer(24.dp)

        // Data Management Section
        DataManagementSection(
            onExportData = { onEvent(ProfileUiEvent.ExportProfileData) },
            onManageData = { onEvent(ProfileUiEvent.ManageData) },
            lastSyncTime = state.lastSyncTime,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SmartAttendHeightSpacer(24.dp)

        // All Institutions
        InstitutionsSection(
            institutions = state.institutions,
            selectedInstitutionId = state.activeInstitution?.id,
            onSelectInstitution = { id -> onEvent(ProfileUiEvent.SelectInstitution(id)) },
            onViewDetail = onNavigateToInstitutionDetail,
            onAddInstitution = { onEvent(ProfileUiEvent.ToggleAddInstitution) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SmartAttendHeightSpacer(24.dp)

        // App Information
        AppInformationSection(
            onViewAppInfo = { onEvent(ProfileUiEvent.ViewAppInfo) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SmartAttendHeightSpacer(32.dp)
    }
}

@Composable
private fun PersonalInformationSection(
    lecturer: Lecturer,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            1.dp,
            colorScheme.outlineVariant.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with title and edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onSurface
                )

                IconButton(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Profile info items
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                ProfileInfoItem(
                    icon = Icons.Default.Person,
                    title = "Full Name",
                    value = lecturer.name,
                    iconColor = colorScheme.primary
                )

                // Email
                ProfileInfoItem(
                    icon = Icons.Default.Email,
                    title = "Email Address",
                    value = lecturer.email,
                    iconColor = colorScheme.secondary
                )

                // Phone (if available)
                lecturer.phone?.let { phone ->
                    ProfileInfoItem(
                        icon = Icons.Default.Phone,
                        title = "Phone Number",
                        value = phone,
                        iconColor = colorScheme.tertiary
                    )
                }

                // Staff ID (if available)
                lecturer.staffId?.let { staffId ->
                    ProfileInfoItem(
                        icon = Icons.Default.Badge,
                        title = "Staff ID",
                        value = staffId,
                        iconColor = colorScheme.primary
                    )
                }

                // Department (if available)
                lecturer.department?.let { department ->
                    ProfileInfoItem(
                        icon = Icons.Default.Business,
                        title = "Department",
                        value = department,
                        iconColor = colorScheme.secondary
                    )
                }

                // Join Date (if available)
                lecturer.joinDate?.let { joinDate ->
                    ProfileInfoItem(
                        icon = Icons.Default.CalendarToday,
                        title = "Member Since",
                        value = joinDate,
                        iconColor = colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActiveInstitutionSection(
    institution: University?,
    onViewDetail: (String) -> Unit,
    onAddInstitution: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Institution",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (institution != null) {
                TextButton(
                    onClick = { onViewDetail(institution.id) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("View Details")
                }
            }
        }

        if (institution == null) {
            EmptyInstitutionCard(onAddInstitution = onAddInstitution)
        } else {
            ActiveInstitutionCard(
                institution = institution,
                onClick = { onViewDetail.invoke(institution.id) }
            )
        }
    }
}

@Composable
private fun ActiveInstitutionCard(
    institution: University,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Institution",
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = institution.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Currently using for attendance tracking",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun InstitutionsSection(
    institutions: List<University>,
    selectedInstitutionId: String?,
    onSelectInstitution: (String) -> Unit,
    onViewDetail: (String) -> Unit,
    onAddInstitution: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Institutions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(
                onClick = onAddInstitution,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add New")
            }
        }

        if (institutions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "No institutions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )

                    Text(
                        text = "No institutions added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                institutions.forEach { institution ->
                    InstitutionListItem(
                        institution = institution,
                        isSelected = institution.id == selectedInstitutionId,
                        onSelect = { onSelectInstitution(institution.id) },
                        onViewDetail = { onViewDetail(institution.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyInstitutionCard(
    onAddInstitution: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "No institution",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No Active Institution",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Add an institution to start tracking attendance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onAddInstitution,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Institution",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun InstitutionListItem(
    institution: University,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Institution logo/icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isSelected) colorScheme.primary.copy(alpha = 0.1f)
                        else colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Institution",
                    tint = if (isSelected) colorScheme.primary
                    else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = institution.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colorScheme.primary, CircleShape)
                        )
                    }
                }

                Text(
                    text = if (isSelected) "Active • Tap to switch" else "Tap to make active",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onViewDetail,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View details",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onSelect,
                    enabled = !isSelected,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isSelected) colorScheme.primary.copy(alpha = 0.1f)
                            else colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.SwapHoriz,
                        contentDescription = "Select",
                        tint = if (isSelected) colorScheme.primary
                        else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DataManagementSection(
    onExportData: () -> Unit,
    onManageData: () -> Unit,
    lastSyncTime: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Export Data
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "Export Data",
                    subtitle = "Export attendance records",
                    onClick = onExportData
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // Manage Data
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Manage Data",
                    subtitle = "Clear cache, backup settings",
                    onClick = onManageData
                )

                // Last sync info
                lastSyncTime?.let { syncTime ->
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Last sync",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Last sync: $syncTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppInformationSection(
    onViewAppInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // App Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewAppInfo)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Class Track Pro",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Version $appVersion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // Support
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Navigate to support */ }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = "Support",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Help & Support",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Contact support, FAQs, documentation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNameBottomSheet(
    state: ProfileState,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = {
            if (!state.isUpdatingProfile) {
                onEvent(ProfileUiEvent.HideEditNameSheet)
            }
        },
        contentWindowInsets = { WindowInsets.ime },
        sheetGesturesEnabled = !state.isUpdatingProfile,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = !state.isUpdatingProfile,
            shouldDismissOnClickOutside = !state.isUpdatingProfile
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Edit Your Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            // Error Banner
            ErrorBanner(
                message = state.bottomSheetErrorMessage,
                onDismiss = { onEvent(ProfileUiEvent.ClearBottomSheetError) }
            )
            SmartAttendHeightSpacer(8.dp)

            // Input Field
            SmartAttendTextField(
                value = state.editingName,
                onValueChange = { onEvent(ProfileUiEvent.OnEditingNameChanged(it)) },
                label = "Full Name",
                placeholder = "Enter your full name",
                isError = state.editingNameError != null,
                errorMessage = state.editingNameError,
                enabled = !state.isUpdatingProfile,
                modifier = Modifier.fillMaxWidth()
            )

            SmartAttendHeightSpacer(8.dp)

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(ProfileUiEvent.HideEditNameSheet) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isUpdatingProfile
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onEvent(ProfileUiEvent.SaveEditedName) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !state.isUpdatingProfile
                ) {
                    if (state.isUpdatingProfile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }

        }
    }
}
