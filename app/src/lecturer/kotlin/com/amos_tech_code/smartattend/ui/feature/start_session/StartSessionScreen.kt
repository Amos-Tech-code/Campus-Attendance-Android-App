package com.amos_tech_code.smartattend.ui.feature.start_session

import android.Manifest
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.ui.components.LoadingDialog
import com.amos_tech_code.smartattend.ui.components.LocationCapturedState
import com.amos_tech_code.smartattend.ui.components.LocationCapturingState
import com.amos_tech_code.smartattend.ui.components.LocationNotCapturedState
import com.amos_tech_code.smartattend.ui.components.PermissionRationaleDialog
import com.amos_tech_code.smartattend.ui.components.PermissionSettingsDialog
import com.amos_tech_code.smartattend.ui.components.ProfileCompletionRequiredDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.LiveAttendanceRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun StartSessionScreen(
    navController: NavController,
    viewModel: StartSessionViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val successState by viewModel.successState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val activity = context as? Activity
    val scrollState = rememberScrollState()
    var shouldShowPermissionRationale by remember { mutableStateOf(false) }
    var shouldShowSettingsDialog by remember { mutableStateOf(false) }

    // Permission Launcher
//    val locationPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val granted = permissions.values.all { it }
//        activity?.let { viewModel.onLocationPermissionResult(granted, it) }
//    }
    // Permission State using Accompanist
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )


    // --- GPS ENABLING LAUNCHER ---
    val enableGpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // After the user interacts with the GPS dialog, check if they enabled it.
        // If they did, we can retry capturing the location.
        val activity = context as? Activity
        if (result.resultCode == Activity.RESULT_OK && activity != null) {
            // GPS was enabled, automatically retry the capture
            viewModel.onEvent(SessionUiEvent.CaptureTeachingVenue(activity))
        } else {
            // User cancelled, show a message
            Toast.makeText(context, "GPS is required to capture location. Turn on location to proceed.", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle permission changes
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            // Permission granted, proceed with location capture
            activity?.let {
                viewModel.onEvent(SessionUiEvent.CaptureTeachingVenue(it))
            }
        }
    }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is StartSessionEvent.ShowErrorMessage -> {
                scope.launch {
                    snackBarHostState.showSnackbar(event.message)
                }
            }
            is StartSessionEvent.CompleteProfile -> {
                navController.navigate(SetUpRoute)
            }
            is StartSessionEvent.NavigateToLiveAttendance -> {
                navController.navigate(LiveAttendanceRoute)
            }

//            is StartSessionEvent.RequestPermission -> {
//                when (event.state) {
//                    LocationPermissionState.GRANTED -> {
//                        // Should not happen here since we checked
//                    }
//                    LocationPermissionState.DENIED_SHOW_RATIONALE -> {
//                        // Show rationale dialog
//                        shouldShowPermissionRationale = true
//                    }
//                    LocationPermissionState.DENIED_NEVER_ASK -> {
//                        // Show settings dialog
//                        shouldShowSettingsDialog = true
//                    }
//                }
//            }

            StartSessionEvent.RequestEnableGps -> {
                activity?.let {
                    viewModel.locationService.promptEnableGPS(it, enableGpsLauncher)
                }
            }
        }
    }

    // Check if we should show success screen
    successState.sessionResponse?.let { sessionResponse ->
        SessionSuccessScreen(
            scope = scope,
            sessionResponse = sessionResponse,
            onLiveAttendanceClick = { viewModel.navigateToLiveAttendance() },
            onBackToHome = { navController.popBackStack() }
        )
       // return
    } ?: run {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Start New Session",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                BottomNavigation(navController)
            },
            snackbarHost = { SnackbarHost(snackBarHostState) }
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
                // Academic Selection Card
                AcademicSelectionCard(state, viewModel::onEvent)

                // Session Configuration Card
                SessionConfigurationCard(state, viewModel::onEvent)

                // Security Settings Card
                SecuritySettingsCard(state, viewModel::onEvent,locationPermissionState, context)

                // Start Session Button
                SmartAttendPrimaryButton(
                    text = "Generate Session Code",
                    onClick = { viewModel.onEvent(SessionUiEvent.StartSession) },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = state.isLoading,
                    enabled = state.selectedProgrammes.isNotEmpty() &&
                            state.selectedUnit != null &&
                            (!state.requireLocation || state.teachingVenue != null)
                )
            }
        }
    }

    // Programme Selection Dialog
    if (state.showProgrammeSelection) {
        ProgrammeSelectionDialog(
            allProgrammes = viewModel.allProgrammes,
            selectedProgrammes = state.selectedProgrammes,
            onProgrammeSelectionChanged = { programme, selected ->
                viewModel.onEvent(SessionUiEvent.ProgrammeSelectionChanged(programme, selected))
            },
            onDismiss = { viewModel.dismissProgrammeSelection() }
        )
    }

    // Unit Selection Dialog
    if (state.showUnitSelection) {
        UnitSelectionDialog(
            availableUnits = state.availableUnits,
            selectedUnit = state.selectedUnit,
            onUnitSelected = { unit ->
                viewModel.onEvent(SessionUiEvent.UnitSelected(unit))
                viewModel.dismissUnitSelection()
            },
            onDismiss = { viewModel.dismissUnitSelection() }
        )
    }

    if (state.isLoading) {
        LoadingDialog(
            message = "Creating Attendance Session...",
        )
    }

    AnimatedVisibility(visible = viewModel.showCompleteProfileDialog.value) {
        ProfileCompletionRequiredDialog(
            onDismissRequest = { navController.navigateUp() },
            onCompleteProfile = { viewModel.navigateToCompleteProfile() },
            isDismissible = true,
        )
    }

}

@Composable
fun AcademicSelectionCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Academic Selection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Programme Selection
            SelectionField(
                value = if (state.selectedProgrammes.isEmpty()) ""
                else "${state.selectedProgrammes.size} programme(s) selected",
                label = "Select Programmes",
                placeholder = "Choose programmes",
                leadingIcon = {
                    Icon(
                        Icons.Default.School,
                        "Programmes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { onEvent(SessionUiEvent.ShowProgrammeSelection) },
                supportingMessage = if (state.selectedProgrammes.isNotEmpty())
                    "Units will be common across all selected programmes" else null
            )

            // Unit Selection
            SelectionField(
                value = state.selectedUnit?.let { "${it.code} - ${it.name}" } ?: "",
                label = "Select Unit",
                placeholder = "Choose a unit",
                leadingIcon = {
                    Icon(
                        Icons.Default.Book,
                        "Unit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    if (state.selectedProgrammes.isNotEmpty()) {
                        onEvent(SessionUiEvent.ShowUnitSelection)
                    }
                },
                //enabled = state.selectedProgrammes.isNotEmpty(),
                supportingMessage = if (state.selectedProgrammes.isEmpty())
                    "Select programmes first" else "${state.availableUnits.size} units available"
            )

            // Selected Programmes Chips
            if (state.selectedProgrammes.isNotEmpty()) {
                SelectedProgrammesChips(
                    programmes = state.selectedProgrammes,
                    onRemove = { programme ->
                        onEvent(SessionUiEvent.ProgrammeSelectionChanged(programme, false))
                    }
                )
            }
        }
    }
}

@Composable
fun SelectionField(
    value: String,
    label: String,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = false,
    supportingMessage: String? = null
) {

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            SmartAttendTextField(
                onClick = onClick,
                value = value,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        "Dropdown",
                        tint = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                readOnly = true,
                enabled = enabled,
                supportingMessage = supportingMessage
            )

    }
}

@Composable
fun SelectedProgrammesChips(
    programmes: List<Programme>,
    onRemove: (Programme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Selected Programmes:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            programmes.forEach { programme ->
                AssistChip(
                    onClick = { onRemove(programme) },
                    label = {
                        Text(
                            programme.name.split(" ").take(3).joinToString(" "),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
//                    border = SuggestionChipDefaults.suggestionChipBorder(
//                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
//                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            "Remove",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SessionConfigurationCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Session Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Duration Selection
            DurationSelection(
                selectedDuration = state.durationMinutes,
                onDurationSelected = { minutes ->
                    onEvent(SessionUiEvent.DurationChanged(minutes))
                }
            )

            // Location Radius
            LocationRadiusSelection(
                radius = state.allowedRadius,
                onRadiusChanged = { radius ->
                    onEvent(SessionUiEvent.RadiusChanged(radius))
                }
            )
        }
    }
}

@Composable
fun DurationSelection(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Session Duration",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            listOf(15, 30, 45, 60, 120).forEach { minutes ->
                FilterChip(
                    selected = selectedDuration == minutes,
                    onClick = { onDurationSelected(minutes) },
                    label = {
                        Text(
                            "$minutes min",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        selectedBorderColor = Color.Transparent,
                        borderColor = MaterialTheme.colorScheme.outline,
                        enabled = selectedDuration == minutes,
                        selected = selectedDuration == minutes,
                    )
                )
            }
        }
    }
}

@Composable
fun LocationRadiusSelection(
    radius: Int,
    onRadiusChanged: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Location Radius",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Allowed distance from teaching venue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${radius}m",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = radius.toFloat(),
            onValueChange = { onRadiusChanged(it.toInt()) },
            valueRange = 10f..200f,
            steps = 19,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SecuritySettingsCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit,
    locationPermissionState: MultiplePermissionsState,
    context: Context
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    "Security",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Security Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Location Requirement Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Require GPS Location",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Students must be within allowed radius",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.requireLocation,
                    onCheckedChange = { onEvent(SessionUiEvent.ToggleLocationRequirement) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Teaching Venue Location Capture
            if (state.requireLocation) {
                TeachingVenueLocationSection(
                    teachingVenue = state.teachingVenue,
                    isCapturing = state.isCapturingLocation,
                    locationError = state.locationError,
                    locationPermissionState,
                    context,
                    onCaptureLocation = {
                        val activity = context as? Activity
                        activity?.let {
                            // Use Accompanist to handle permission flow
                            if (locationPermissionState.allPermissionsGranted) {
                                onEvent(SessionUiEvent.CaptureTeachingVenue(it))
                            } else {
                                // This will automatically show the appropriate dialogs/rationale
                                locationPermissionState.launchMultiplePermissionRequest()
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TeachingVenueLocationSection(
    teachingVenue: LocationData?,
    isCapturing: Boolean,
    locationError: String?,
    locationPermissionState: MultiplePermissionsState,
    context: Context,
    onCaptureLocation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                "Location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Teaching Venue Location",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // Handle permission rationale using Accompanist
        locationPermissionState.permissions.forEach { permission ->
            when (permission.permission) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    when {
                        permission.status.isGranted -> {
                            // Permission granted - show location state
                            LocationContentState(
                                teachingVenue = teachingVenue,
                                isCapturing = isCapturing,
                                locationError = locationError,
                                onCaptureLocation = onCaptureLocation
                            )
                        }

                        permission.status.shouldShowRationale -> {
                            // Show rationale UI
                            PermissionRationaleDialog(
                                onDismissRequest = {  },
                                onRequestPermission = { locationPermissionState.launchMultiplePermissionRequest() }
                            )
                        }

                        !permission.status.isGranted && !permission.status.shouldShowRationale -> {
                            // Permission permanently denied - show settings UI
                            PermissionSettingsDialog(
                                context = context,
                                onDismissRequest = {  }
                            )
                        }
                    }
                }
            }
        }

    }
}


@Composable
private fun LocationContentState(
    teachingVenue: LocationData?,
    isCapturing: Boolean,
    locationError: String?,
    onCaptureLocation: () -> Unit
) {
    when {
        isCapturing -> {
            LocationCapturingState()
        }
        teachingVenue != null -> {
            LocationCapturedState(
                location = teachingVenue,
                onRecapture = onCaptureLocation
            )
        }
        else -> {
            LocationNotCapturedState(
                onCaptureLocation = onCaptureLocation,
                error = locationError
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgrammeSelectionDialog(
    allProgrammes: List<Programme>,
    selectedProgrammes: List<Programme>,
    onProgrammeSelectionChanged: (Programme, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Select Programmes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Units will be common across all selected programmes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allProgrammes) { programme ->
                        ProgrammeSelectionItem(
                            programme = programme,
                            isSelected = programme in selectedProgrammes,
                            onSelectionChanged = { selected ->
                                onProgrammeSelectionChanged(programme, selected)
                            }
                        )
                    }
                }

                SmartAttendPrimaryButton(
                    text = "Done",
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
fun ProgrammeSelectionItem(
    programme: Programme,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        onClick = { onSelectionChanged(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) else null,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = programme.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${programme.department} • Year ${programme.yearOfStudy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${programme.units.size} units available",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelectionDialog(
    availableUnits: List<UnitModel>,
    selectedUnit: UnitModel?,
    onUnitSelected: (UnitModel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Select Unit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Common units across all selected programmes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableUnits) { unit ->
                        UnitSelectionItem(
                            unit = unit,
                            isSelected = unit.id == selectedUnit?.id,
                            onSelected = { onUnitSelected(unit) }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun UnitSelectionItem(
    unit: UnitModel,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        onClick = onSelected,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) else null,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelected,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = unit.code,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



// Helper function to calculate time ago
fun calculateTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "just now" // Less than 1 minute
        diff < 3600000 -> "${diff / 60000} minutes ago" // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000} hours ago" // Less than 1 day
        else -> "${diff / 86400000} days ago" // More than 1 day
    }
}