package com.amos_tech_code.smartattend.ui.feature.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.ConfirmActionDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.NotificationsRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.ui.navigation.SettingsRoute
import com.amos_tech_code.smartattend.ui.navigation.SignInRoute
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is ProfileEvent.ShowErrorMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
            is ProfileEvent.InstitutionUpdated -> {
                Toast.makeText(context, "Active institution updated", Toast.LENGTH_SHORT).show()
            }
            is ProfileEvent.ShowSuccessMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
            ProfileEvent.NavigateToEditProfile -> {
            }
            ProfileEvent.NavigateToInstitutionSetUp -> {
                navController.navigate(SetUpRoute)
            }
            ProfileEvent.LogOut -> {
                navController.navigate(SignInRoute) {
                    popUpTo(0) { inclusive = true  }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ProfileTopAppBar(scrollBehavior) {
                viewModel.logOut()
            }
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                ErrorState(
                    message = state.errorMessage ?: "Something went wrong",
                    onRetry = { viewModel.onEvent(ProfileUiEvent.RefreshData) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                ProfileContent(
                    state = state,
                    onEvent = viewModel::onEvent,
                    paddingValues = paddingValues,
                    navController = navController,
                    modifier = Modifier.verticalScroll(scrollState)
                )
            }
        }

        // Show the bottom sheet when the state flag is true
        if (state.showEditNameSheet) {
            EditNameBottomSheet(
                name = state.editingName,
                onNameChange = { viewModel.onEvent(ProfileUiEvent.OnEditingNameChanged(it)) },
                onDismiss = { viewModel.onEvent(ProfileUiEvent.HideEditNameSheet) },
                onSave = { viewModel.onEvent(ProfileUiEvent.SaveEditedName) }
            )
        }

    }
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    onEvent: (ProfileUiEvent) -> Unit,
    paddingValues: PaddingValues,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Profile Header with gradient
        ProfileHeader(
            lecturer = state.lecturer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Active Institution Card
        ActiveInstitutionCard(
            institution = state.selectedInstitution,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            onEdit = { /* Handle edit */ }
        )

        // Teaching Statistics with animated cards
        TeachingStatisticsSection(
            stats = state.teachingStats,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Other Institutions List
        if (state.institutions.size > 1) {
            OtherInstitutionsSection(
                institutions = state.institutions,
                selectedInstitutionId = state.selectedInstitution?.id,
                onSelectInstitution = { id -> onEvent(ProfileUiEvent.SelectInstitution(id)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Quick Actions Grid
        QuickActionsGrid(
            onNavigateToNotifications = { navController.navigate(NotificationsRoute) },
            onNavigateToSettings = { navController.navigate(SettingsRoute) },
            onEditProfile = { onEvent(ProfileUiEvent.EditProfile) },
            onExportData = { onEvent(ProfileUiEvent.ExportProfileData) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

    }
}

@Composable
private fun ProfileHeader(
    lecturer: Lecturer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Avatar with border
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lecturer.profileImage != null) {
                    // Load profile image using Coil or similar
                    Text(
                        text = "IMG",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = lecturer.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = lecturer.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = lecturer.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                lecturer.staffId?.let { staffId ->
                    Text(
                        text = "ID: $staffId",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveInstitutionCard(
    institution: Institution?,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit
) {
    institution?.let { inst ->
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Institution",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Active Institution",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = inst.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (inst.isActive) "✓ Currently Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (inst.isActive) PresentColor else MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TeachingStatisticsSection(
    stats: TeachingStatisticsUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Teaching Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Current Institution & Semester Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Active Institution",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stats.activeInstitution,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (stats.isInstitutionActive) {
                        Badge(
                            containerColor = PresentColor.copy(alpha = 0.2f),
                            contentColor = PresentColor
                        ) {
                            Text("Active", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Current Semester",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stats.currentSemester,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        SmartAttendHeightSpacer(16.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Space between rows
        ) {
            // Prepare the list of stat items
            val statItems = mutableListOf(
                StatCardData(
                    "Courses",
                    stats.totalCourses.toString(),
                    Icons.Default.MenuBook,
                    MaterialTheme.colorScheme.primary
                ),
                StatCardData(
                    "Students",
                    stats.totalExpectedStudents.toString(),
                    Icons.Default.People,
                    MaterialTheme.colorScheme.secondary
                )
            )
            if (stats.totalProgrammes > 0) {
                statItems.add(
                    StatCardData(
                        "Programmes",
                        stats.totalProgrammes.toString(),
                        Icons.Default.School,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            }
            if (stats.totalDepartments > 0) {
                statItems.add(
                    StatCardData(
                        "Departments",
                        stats.totalDepartments.toString(),
                        Icons.Default.AccountBalance,
                        MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Group the items into rows of 2
            statItems.chunked(2).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Space between columns
                ) {
                    rowItems.forEach { item ->
                        // Use weight(1f) to ensure equal column widths
                        StatCard(
                            title = item.title,
                            value = item.value,
                            icon = item.icon,
                            color = item.color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If there's an odd number of items, fill the remaining space to maintain alignment
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OtherInstitutionsSection(
    institutions: List<Institution>,
    selectedInstitutionId: String?,
    onSelectInstitution: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Other Institutions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            institutions.filter { it.id != selectedInstitutionId }.forEach { institution ->
                InstitutionListItem(
                    institution = institution,
                    onClick = { onSelectInstitution(institution.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun InstitutionListItem(
    institution: Institution,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = institution.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (institution.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (institution.isActive) PresentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onExportData: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Define the list of actions
    val actions = listOf(
        QuickActionItem("Notifications", Icons.Default.Notifications, onNavigateToNotifications),
        QuickActionItem("Settings", Icons.Default.Settings, onNavigateToSettings),
        QuickActionItem("Edit Profile", Icons.Default.Edit, onEditProfile),
        QuickActionItem("Export Data", Icons.Default.Download, onExportData)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Space between the rows
    ) {
        // Group actions into pairs (for a 2-column grid)
        actions.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Space between the columns
            ) {
                rowItems.forEach { item ->
                    // Use weight(1f) to make columns equal width
                    QuickActionCard(
                        title = item.title,
                        icon = item.icon,
                        onClick = item.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                // If there's an odd number of items, add a spacer to keep alignment
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onLogOutClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = { // Add actions parameter
            IconButton(onClick = {showLogoutDialog = true}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Log Out",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        scrollBehavior = scrollBehavior
    )

    if (showLogoutDialog) {
        ConfirmActionDialog(
            title = "Log Out",
            message = "Are you sure you want to log out?",
            isDestructive = true,
            onConfirm = onLogOutClick,
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNameBottomSheet(
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets.ime }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit Your Name",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            SmartAttendTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Full Name",
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }

                Button(onClick = onSave) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        SmartAttendHeightSpacer(16.dp)
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        SmartAttendHeightSpacer(8.dp)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        SmartAttendHeightSpacer(24.dp)
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

private data class StatCardData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

private data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is ProfileEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_LONG).show()
            }
            is ProfileEvent.InstitutionUpdated -> {
                Toast.makeText(navController.context, "Active Institution updated successfully", Toast.LENGTH_SHORT).show()
            }

            ProfileEvent.NavigateToEditProfile -> {
                //navController.navigate()
            }
            is ProfileEvent.ShowSuccessMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_LONG).show()
            }

            ProfileEvent.NavigateToInstitutionSetUp -> { navController.navigate(SetUpRoute)}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
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
            // Profile Header
            ProfileHeaderSection(
                lecturer = state.lecturer,
                modifier = Modifier.padding(16.dp)
            )

            // Institution Configuration
            InstitutionConfigurationSection(
                institutions = state.institutions,
                selectedInstitution = state.selectedInstitution,
                onEvent = { viewModel.onEvent(it) },
                modifier = Modifier.padding(16.dp)
            )

            // Teaching Statistics
            TeachingStatsSection(
                stats = state.teachingStats,
                modifier = Modifier.padding(16.dp)
            )

            // Quick Actions
            QuickActionsSection(
                onNavigateToNotifications = { navController.navigate(NotificationsRoute) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun InstitutionConfigurationSection(
    institutions: List<Institution>,
    selectedInstitution: Institution?,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Institution Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { onEvent(ProfileUiEvent.ToggleAddInstitution) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Institution"
                )
            }
        }

        SmartAttendHeightSpacer(16.dp)

        // Current Institution
        selectedInstitution?.let { institution ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = institution.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${institution.department} • ${institution.campus}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = PresentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Other Institutions
        if (institutions.size > 1) {
            SmartAttendHeightSpacer(16.dp)
            Text(
                text = "Other Institutions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SmartAttendHeightSpacer(8.dp)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                institutions.filter { it.id != selectedInstitution?.id }.forEach { institution ->
                    InstitutionItem(
                        institution = institution,
                        onSelect = { onEvent(ProfileUiEvent.SelectInstitution(institution.id)) },
                        onEdit = { /* Handle edit */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun InstitutionItem(
    institution: Institution,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = institution.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${institution.department} • ${institution.campus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onSelect) {
                    Icon(Icons.Default.Check, "Set Active", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
            }
        }
    }
}


@Composable
fun ProfileHeaderSection(
    lecturer: Lecturer,
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
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lecturer.profileImage != null) {
                    // Load profile image here
                    Text(
                        text = "IMG",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = lecturer.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Lecturer Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = lecturer.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = lecturer.activeInstitution,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Contact Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(
                    icon = Icons.Default.Email,
                    text = lecturer.email,
                    onClick = { }
                )
                InfoChip(
                    icon = Icons.Default.Badge,
                    text = "",
                    onClick = { }
                )
            }

            lecturer.joinDate?.let { joinDate ->
                Text(
                    text = "Teaching since $joinDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TeachingStatsSection(
    stats: TeachingStatistics,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Teaching Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(16.dp)

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with semester info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Semester",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stats.currentSemester,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                }

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TeachingStatItem(
                        value = stats.totalCourses.toString(),
                        label = "Courses",
                        icon = Icons.Default.School
                    )
                    TeachingStatItem(
                        value = stats.totalStudents.toString(),
                        label = "Students",
                        icon = Icons.Default.People
                    )
                }

            }
        }
    }
}

@Composable
private fun TeachingStatItem(
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
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
fun QuickActionsSection(
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendHeightSpacer(12.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Notifications",
                subtitle = "View alerts and requests",
                icon = Icons.Default.Notifications,
                onClick = onNavigateToNotifications,
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                title = "Settings",
                subtitle = "App preferences",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )
        }

        SmartAttendHeightSpacer(8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Edit Profile",
                subtitle = "Update your information",
                icon = Icons.Default.Edit,
                onClick = { /* Handle edit profile */ },
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                title = "Export Data",
                subtitle = "Download records",
                icon = Icons.Default.Download,
                onClick = { /* Handle export */ },
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
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
private fun InfoChip(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
*/