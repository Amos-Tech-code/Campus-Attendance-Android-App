package com.amos_tech_code.smartattend.ui.feature.start_session

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.ProfileCompletionRequiredDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.LiveAttendanceRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartSessionScreen(
    navController: NavController,
    viewModel: StartSessionViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is StartSessionEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_LONG).show()
            }
            is StartSessionEvent.CompleteProfile -> {
                navController.navigate(SetUpRoute)
            }
            is StartSessionEvent.SessionStarted -> {
                navController.navigate(LiveAttendanceRoute())
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Start New Session",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Session Configuration Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Session Configuration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Course Selection
                    SmartAttendTextField(
                        value = state.selectedCourse?.name ?: "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Select Course",
                        placeholder = "Choose a course",
                        leadingIcon = { Icon(Icons.Default.Book, "Course") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "Dropdown")
                        },
                        readOnly = true,
//                        onClick = {
//                            // Show course selection dialog
//                            // TODO:
//                        }
                    )

                    // Duration Selection
                    Column {
                        Text(
                            text = "Session Duration",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { minutes ->
                                FilterChip(
                                    selected = state.durationMinutes == minutes,
                                    onClick = {
                                        //viewModel.onEvent(SessionUiEvent.DurationChanged(minutes))
                                    },
                                    label = { Text("$minutes min") }
                                )
                            }
                        }
                    }

                    // Location Radius
                    Column {
                        Text(
                            text = "Allowed Location Radius: ${state.allowedRadius}m",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        SmartAttendHeightSpacer(8.dp)
                        Slider(
                            value = state.allowedRadius.toFloat(),
                            onValueChange = {
                                //viewModel.onEvent(SessionUiEvent.RadiusChanged(it.toInt()))
                            },
                            valueRange = 10f..200f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("10m", style = MaterialTheme.typography.labelSmall)
                            Text("200m", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Security Settings Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Security, "Security", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Security Settings",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Require GPS Location")
                        Switch(
                            checked = state.requireLocation,
                            onCheckedChange = {
                                //viewModel.onEvent(SessionUiEvent.ToggleLocationRequirement(it))
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Device Verification")
                        Switch(
                            checked = state.verifyDevices,
                            onCheckedChange = {
                                //viewModel.onEvent(SessionUiEvent.ToggleDeviceVerification(it))
                            }
                        )
                    }
                }
            }

            // Start Session Button
            SmartAttendPrimaryButton(
                text = "Generate Session Code",
                onClick = {
                    // TODO
                    viewModel::startSession
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isLoading
            )
        }
    }

    AnimatedVisibility(
        visible = viewModel.showCompleteProfileDialog.value,
    ) {
        ProfileCompletionRequiredDialog(
            onDismissRequest = {
                navController.navigateUp()
            },
            onCompleteProfile = {
                viewModel.navigateToCompleteProfile()
            },
            isDismissible = true,
        )
    }
}