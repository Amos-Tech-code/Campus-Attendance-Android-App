package com.amos_tech_code.smartattend.ui.feature.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendSecondaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.components.SmartAttendWidthSpacer
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.NotificationsRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.ui.navigation.SettingsRoute
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


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
                Toast.makeText(navController.context, "Institution updated successfully", Toast.LENGTH_SHORT).show()
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
                showAddInstitution = state.showAddInstitution,
                newInstitutionState = state.newInstitutionState,
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
    showAddInstitution: Boolean,
    newInstitutionState: NewInstitutionState,
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
                    imageVector = if (showAddInstitution) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showAddInstitution) "Close" else "Add Institution"
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

        SmartAttendHeightSpacer(12.dp)

        // Add Institution Form
        if (showAddInstitution) {
            AddInstitutionForm(
                state = newInstitutionState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth()
            )
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
private fun AddInstitutionForm(
    state: NewInstitutionState,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add New Institution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SmartAttendTextField(
                value = state.name,
                onValueChange = { onEvent(ProfileUiEvent.NewInstitutionNameChanged(it)) },
                label = "Institution Name",
                placeholder = "Enter institution name",
                isError = state.nameError != null,
                errorMessage = state.nameError
            )

            SmartAttendTextField(
                value = state.department,
                onValueChange = { onEvent(ProfileUiEvent.NewInstitutionDepartmentChanged(it)) },
                label = "Department",
                placeholder = "Enter department name",
                isError = state.departmentError != null,
                errorMessage = state.departmentError
            )

            SmartAttendTextField(
                value = state.campus,
                onValueChange = { onEvent(ProfileUiEvent.NewInstitutionCampusChanged(it)) },
                label = "Campus Location",
                placeholder = "Enter campus location"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmartAttendSecondaryButton(
                    text = "Cancel",
                    onClick = { onEvent(ProfileUiEvent.ToggleAddInstitution) },
                    modifier = Modifier.weight(1f)
                )
                SmartAttendPrimaryButton(
                    text = "Add Institution",
                    onClick = { onEvent(ProfileUiEvent.SaveNewInstitution) },
                    modifier = Modifier.weight(1f),
                    isLoading = state.isLoading
                )
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
                    text = lecturer.department,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = lecturer.institution,
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
                    onClick = { /* Handle email */ }
                )
                InfoChip(
                    icon = Icons.Default.Badge,
                    text = lecturer.staffId,
                    onClick = { /* Copy staff ID */ }
                )
            }

            // Additional Info
            lecturer.officeLocation?.let { office ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Office",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    SmartAttendWidthSpacer(4.dp)
                    Text(
                        text = office,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    Text(
                        text = "${stats.averageAttendance}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                    TeachingStatItem(
                        value = stats.totalSessions.toString(),
                        label = "Sessions",
                        icon = Icons.Default.Schedule
                    )
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { stats.averageAttendance / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when {
                        stats.averageAttendance >= 80 -> PresentColor
                        stats.averageAttendance >= 60 -> PendingColor
                        else -> AbsentColor
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // Additional Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Average Attendance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Teaching since ${stats.teachingSince}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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