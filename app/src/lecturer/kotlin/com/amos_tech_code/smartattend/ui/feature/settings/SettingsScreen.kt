package com.amos_tech_code.smartattend.ui.feature.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonSize
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendSecondaryButton
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is SettingsEvent.ShowErrorMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
            is SettingsEvent.ShowSuccessMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
            is SettingsEvent.SettingsUpdated -> {
                Toast.makeText(context, "Settings updated successfully", Toast.LENGTH_SHORT).show()
            }
            is SettingsEvent.DataExported -> {
                // Handle file export success
                Toast.makeText(context, "Data exported to ${event.fileName}", Toast.LENGTH_LONG).show()
            }
            SettingsEvent.CacheCleared -> {
                // Handle cache cleared success
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading settings...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
            } else {
                // Attendance Settings
                SettingsSection(
                    title = "Attendance Settings",
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Location Accuracy - Improved with visual indicators
                    SettingsItem1(
                        icon = Icons.Default.LocationOn,
                        title = "Location Precision",
                        subtitle = "GPS accuracy requirement for attendance",
                        action = {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Visual indicator chips
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "High" to "10m",
                                        "Medium" to "25m",
                                        "Low" to "50m"
                                    ).forEachIndexed { index, (label, value) ->
                                        FilterChip(
                                            selected = state.locationAccuracy == index,
                                            onClick = {
                                                viewModel.onEvent(SettingsUiEvent.LocationAccuracyChanged(index))
                                            },
                                            label = {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                                    Text(value, style = MaterialTheme.typography.labelSmall)
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }

                                // Current selection text
                                Text(
                                    text = when (state.locationAccuracy) {
                                        0 -> "High precision (10m radius)"
                                        1 -> "Medium precision (25m radius)"
                                        2 -> "Low precision (50m radius)"
                                        else -> "Custom precision"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    SettingsItem1(
                        icon = Icons.Default.Schedule,
                        title = "Default Session Duration",
                        subtitle = "How long attendance codes remain valid",
                        action = {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Duration cards in a row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(
                                        "15 min" to 0,
                                        "30 min" to 1,
                                        "45 min" to 2,
                                        "60 min" to 3
                                    ).forEach { (duration, index) ->
                                        DurationCard(
                                            duration = duration,
                                            isSelected = state.defaultDuration == index,
                                            onClick = {
                                                viewModel.onEvent(SettingsUiEvent.DefaultDurationChanged(index))
                                            }
                                        )
                                    }
                                }

                                // Current selection
                                Text(
                                    text = "Selected: ${listOf("15", "30", "45", "60")[state.defaultDuration]} minutes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    SettingsItem1(
                        icon = Icons.Default.Radar,
                        title = "Attendance Radius",
                        subtitle = "Maximum distance from classroom allowed",
                        action = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Current value display
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${state.defaultRadius} meters",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                // Slider with min/max labels
                                Column {
                                    Slider(
                                        value = state.defaultRadius.toFloat(),
                                        onValueChange = {  viewModel.onEvent(SettingsUiEvent.DefaultRadiusChanged(it.toInt())) },
                                        valueRange = 10f..210f,
                                        steps = 200,
                                        modifier = Modifier.width(200.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            activeTickColor = Color.Transparent,
                                            inactiveTickColor = Color.Transparent
                                        ),
                                        thumb = {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .shadow(4.dp, shape = CircleShape)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        width = 3.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("10m", style = MaterialTheme.typography.labelSmall)
                                        Text("210m", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                // Radius preview
                                Text(
                                    text = when {
                                        state.defaultRadius <= 25 -> "Tight range - small classrooms"
                                        state.defaultRadius <= 50 -> "Standard range - typical classrooms"
                                        state.defaultRadius <= 100 -> "Large range - lecture halls"
                                        else -> "Very large range - outdoor areas"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    )
                }

                // Notification Settings
                SettingsSection(
                    title = "Notifications",
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        subtitle = "Receive push notifications",
                        action = {
                            Switch(
                                checked = state.pushNotifications,
                                onCheckedChange = {
                                    viewModel.onEvent(SettingsUiEvent.PushNotificationsChanged(it))
                                }
                            )
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Device Change Alerts",
                        subtitle = "Alert on student device changes",
                        action = {
                            Switch(
                                checked = state.deviceChangeAlerts,
                                onCheckedChange = {
                                    viewModel.onEvent(SettingsUiEvent.DeviceChangeAlertsChanged(it))
                                }
                            )
                        }
                    )
                }

                // Security Settings
                SettingsSection(
                    title = "Security",
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Require Device Verification",
                        subtitle = "Verify student devices",
                        action = {
                            Switch(
                                checked = state.requireDeviceVerification,
                                onCheckedChange = {
                                    viewModel.onEvent(SettingsUiEvent.DeviceVerificationChanged(it))
                                }
                            )
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.GpsFixed,
                        title = "Require Location",
                        subtitle = "Enforce GPS verification",
                        action = {
                            Switch(
                                checked = state.requireLocation,
                                onCheckedChange = {
                                    viewModel.onEvent(SettingsUiEvent.LocationRequirementChanged(it))
                                }
                            )
                        }
                    )
                }

                // Data Management
                SettingsSection(
                    title = "Data Management",
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "Export Data",
                        subtitle = "Download attendance records",
                        action = {
                            IconButton(
                                onClick = { viewModel.onEvent(SettingsUiEvent.ExportData) },
                                enabled = !state.isExportingData
                            ) {
                                if (state.isExportingData) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Download, "Export")
                                }
                            }
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Clear Cache",
                        subtitle = "Clear local app data",
                        action = {
                            IconButton(
                                onClick = { viewModel.onEvent(SettingsUiEvent.ClearCache) },
                                enabled = !state.isClearingCache
                            ) {
                                if (state.isClearingCache) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Delete, "Clear Cache")
                                }
                            }
                        }
                    )
                }

                // App Information
                SettingsSection(
                    title = "About",
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "App version information",
                        action = {
                            Text("v${state.appVersion}")
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy",
                        action = {
                            IconButton(onClick = { /* Open privacy policy */ }) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, "View")
                            }
                        }
                    )

                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Get help with the app",
                        action = {
                            IconButton(onClick = { /* Open help */ }) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, "View")
                            }
                        }
                    )
                }

                // Action Buttons
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Save Settings Button
                    SmartAttendPrimaryButton(
                        text = "Save Settings",
                        onClick = { viewModel.onEvent(SettingsUiEvent.SaveSettings) },
                        isLoading = state.isSaving,
                        size = SmartAttendButtonSize.Medium
                    )

                    // Reset to Defaults Button
                    SmartAttendSecondaryButton(
                        text = "Reset to Defaults",
                        onClick = { viewModel.onEvent(SettingsUiEvent.ResetToDefaults) },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = state.isResetting
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem1(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header part of the item
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
            // Action part, now neatly below the header
            Box(modifier = Modifier.padding(start = 40.dp)) { // Indent action to align with text
                action()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header part of the item
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                action()

            }
        }
    }
}


@Composable
private fun DurationCard(
    duration: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(70.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = duration,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}