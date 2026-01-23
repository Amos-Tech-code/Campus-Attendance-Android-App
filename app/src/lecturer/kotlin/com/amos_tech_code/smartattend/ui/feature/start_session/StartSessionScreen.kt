package com.amos_tech_code.smartattend.ui.feature.start_session

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.ui.components.LoadingDialog
import com.amos_tech_code.smartattend.ui.components.LocationCapturedState
import com.amos_tech_code.smartattend.ui.components.LocationCapturingState
import com.amos_tech_code.smartattend.ui.components.LocationNotCapturedState
import com.amos_tech_code.smartattend.ui.components.LocationPermissionRationaleDialog
import com.amos_tech_code.smartattend.ui.components.LocationPermissionSettingsDialog
import com.amos_tech_code.smartattend.ui.components.ProfileCompletionRequiredDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonSize
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButtonWithLeadingIcon
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.navigation.LiveAttendanceRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
    // State for showing permission dialogs
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Create launcher for enabling GPS
    val enableGpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (!state.requireLocation) return@rememberLauncherForActivityResult
        if (result.resultCode == Activity.RESULT_OK && activity != null) {
            // GPS was enabled, try to capture location
            viewModel.onGpsEnabled()
        } else {
            // User canceled or GPS still disabled
            viewModel.onEvent(
                SessionUiEvent.LocationCaptureFailed("GPS is required for location capture")
            )
        }
    }

    // Create launcher for location permission request
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!state.requireLocation) return@rememberLauncherForActivityResult
        if (isGranted) {
            if (viewModel.isLocationEnabled()) {
                viewModel.onEvent(SessionUiEvent.CaptureTeachingVenue)
            } else {
                activity?.let {
                    viewModel.promptEnableGPS(it, enableGpsLauncher)
                }
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showRationaleDialog = true
            } else {
                showSettingsDialog = true
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

            StartSessionEvent.RequestEnableGps -> {
                activity?.let {
                    viewModel.promptEnableGPS(it, enableGpsLauncher)
                }
            }

            is StartSessionEvent.LocationPermissionDenied -> {
                if (event.shouldShowRationale) {
                    showRationaleDialog = true
                } else {
                    showSettingsDialog = true
                }
            }
        }
    }

    LaunchedEffect(state.requireLocation) {
        if (!state.requireLocation) return@LaunchedEffect
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (viewModel.isLocationEnabled()) {
                    viewModel.onEvent(SessionUiEvent.CaptureTeachingVenue)
                } else {
                    activity?.let {
                        viewModel.promptEnableGPS(it, enableGpsLauncher)
                    }
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showRationaleDialog = true
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!state.requireLocation) return@LifecycleEventObserver

                val permissionGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                if (permissionGranted) {
                    // Permission is now granted → hide permission dialogs
                    showRationaleDialog = false
                    showSettingsDialog = false

                    // Now check GPS
                    if (!viewModel.isLocationEnabled()) {
                        activity?.let {
                            viewModel.promptEnableGPS(it, enableGpsLauncher)
                        }
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
    } ?: run {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Start New Session",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
            snackbarHost = { SnackbarHost(snackBarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Session Details Card
                SessionDetailsCard(state, viewModel::onEvent)

                // Academic Selection Card
                AcademicSelectionCard(state, viewModel::onEvent)

                // Attendance Method Card
                AttendanceMethodCard(state, viewModel::onEvent)

                // Session Configuration Card
                SessionConfigurationCard(state, viewModel::onEvent)

                // Security Settings Card
                SecuritySettingsCard(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onCaptureLocation = {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                if (viewModel.isLocationEnabled()) {
                                    viewModel.onEvent(SessionUiEvent.CaptureTeachingVenue)
                                } else {
                                    activity?.let {
                                        viewModel.promptEnableGPS(it, enableGpsLauncher)
                                    }
                                }
                            }

                            ActivityCompat.shouldShowRequestPermissionRationale(
                                activity!!,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) -> showRationaleDialog = true

                            else -> requestPermissionLauncher.launch(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }

                    }
                )

                SmartAttendPrimaryButtonWithLeadingIcon(
                    onClick = { viewModel.onEvent(SessionUiEvent.StartSession) },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, "Start Session") },
                    text = "Start Session",
                    size = SmartAttendButtonSize.Medium,
                    enabled = !state.isLoading
                )

                SmartAttendHeightSpacer(16.dp)
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

    // Permission rationale dialog
    if (showRationaleDialog) {
        LocationPermissionRationaleDialog(
            onDismissRequest = {
                showRationaleDialog = false
                viewModel.onEvent(SessionUiEvent.ToggleLocationRequirement)
            },
            onRequestPermission = {
                showRationaleDialog = false
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        )
    }

    // Permission settings dialog (when permission permanently denied)
    if (showSettingsDialog) {
        LocationPermissionSettingsDialog(
            context = context,
            onDismissRequest = {
                showSettingsDialog = false
                viewModel.onEvent(SessionUiEvent.ToggleLocationRequirement)
            }
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
private fun SessionDetailsCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    "Details",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Session Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Session Title
            SmartAttendTextField(
                value = state.title,
                onValueChange = { onEvent(SessionUiEvent.TitleChanged(it)) },
                label = "Session Title (Optional)",
                placeholder = "e.g., Week 3 Lecture - Introduction to Algorithms",
                leadingIcon = {
                    Icon(
                        Icons.Default.Title,
                        "Title",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                supportingMessage = "Helpful for identifying the session later"
            )

            // Week Number
            Column {
                Text(
                    text = "Academic week for this session *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    (1..16).forEach { weekNumber ->
                        FilterChip(
                            selected = state.weekNumber == weekNumber,
                            onClick = { onEvent(SessionUiEvent.WeekNumberChanged(weekNumber)) },
                            label = { Text("Week $weekNumber") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            border = if (state.weekNumber == weekNumber) BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ) else null,
                        )
                    }
                    SmartAttendTextField(
                        value = state.weekNumber.toString(),
                        onValueChange = {
                            val week = it.toIntOrNull() ?: 0
                            onEvent(SessionUiEvent.WeekNumberChanged(week))
                        },
                        label = "Other",
                        placeholder = "e.g., 17",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Session Type Selection
            SessionTypeSelection(
                selectedType = state.sessionType,
                onTypeSelected = { sessionType ->
                    onEvent(SessionUiEvent.SessionTypeChanged(sessionType))
                }
            )
        }
    }
}

@Composable
private fun SessionTypeSelection(
    selectedType: AttendanceSessionType,
    onTypeSelected: (AttendanceSessionType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Session Type",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            AttendanceSessionType.entries.forEach { type ->
                val (icon, label, description) = when (type) {
                    AttendanceSessionType.REGULAR -> Triple(
                        Icons.Default.Schedule,
                        "Regular",
                        "Standard lecture"
                    )
                    AttendanceSessionType.MAKEUP -> Triple(
                        Icons.Default.Restore,
                        "Makeup",
                        "Rescheduled session"
                    )
                    AttendanceSessionType.SPECIAL -> Triple(
                        Icons.Default.Star,
                        "Special",
                        "Guest lecture/event"
                    )
                }

                SessionTypeCard(
                    icon = icon,
                    title = label,
                    description = description,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) }
                )
            }
        }
    }
}

@Composable
private fun SessionTypeCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ) else null,
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AttendanceMethodCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    "Attendance Method",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Attendance Method",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Method Selection
            AttendanceMethodSelection(
                selectedMethod = state.attendanceMethod,
                onMethodSelected = { method ->
                    onEvent(SessionUiEvent.AttendanceMethodChanged(method))
                }
            )
        }
    }
}

@Composable
private fun AttendanceMethodSelection(
    selectedMethod: AttendanceMethod,
    onMethodSelected: (AttendanceMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "How will students mark attendance?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AttendanceMethod.entries.forEach { method ->
            val (icon, title, description) = when (method) {
                AttendanceMethod.QR_CODE -> Triple(
                    Icons.Default.QrCode2,
                    "QR Code Only",
                    "Students scan QR code"
                )
                AttendanceMethod.MANUAL_CODE -> Triple(
                    Icons.Default.Keyboard,
                    "Manual Code Only",
                    "Students enter session code"
                )
                AttendanceMethod.ANY -> Triple(
                    Icons.Default.AllInclusive,
                    "Any Method",
                    "QR code or manual entry"
                )
            }

            AttendanceMethodOption(
                icon = icon,
                title = title,
                description = description,
                isSelected = selectedMethod == method,
                onClick = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
private fun AttendanceMethodOption(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isSelected) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ) else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SessionConfigurationCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    "Configuration",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Session Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Duration Selection
            DurationSelection(
                selectedDuration = state.durationMinutes,
                onDurationSelected = { minutes ->
                    onEvent(SessionUiEvent.DurationChanged(minutes))
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Location Radius (only shown when location is required)
            if (state.requireLocation) {
                LocationRadiusSelection(
                    radius = state.allowedRadius,
                    onRadiusChanged = { radius ->
                        onEvent(SessionUiEvent.RadiusChanged(radius))
                    }
                )
            } else {
                Text(
                    text = "Turn on 'Require GPS Location' to set location radius",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun AcademicSelectionCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    "Academic",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Academic Selection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Programme Selection
            SelectionField(
                value = if (state.selectedProgrammes.isEmpty()) ""
                else "${state.selectedProgrammes.size} programme(s) selected",
                label = "Select Programmes",
                placeholder = "Choose programmes",
                leadingIcon = {
                    Icon(
                        Icons.Default.Group,
                        "Programmes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { onEvent(SessionUiEvent.ShowProgrammeSelection) },
                supportingMessage = if (state.selectedProgrammes.isNotEmpty())
                    "Units will be common across all selected programmes"
                else "Tap to select one or more programmes"
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
                supportingMessage = if (state.selectedProgrammes.isEmpty())
                    "Select programmes first"
                else if (state.availableUnits.isEmpty())
                    "No common units available"
                else "${state.availableUnits.size} common units available"
            )

            // Selected Programmes Chips
            AnimatedVisibility(
                visible = state.selectedProgrammes.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
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
private fun SecuritySettingsCard(
    state: SessionState,
    onEvent: (SessionUiEvent) -> Unit,
    onCaptureLocation: () -> Unit
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
                    onCheckedChange = {
                        onEvent(SessionUiEvent.ToggleLocationRequirement)
                    },
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
                    onCaptureLocation = onCaptureLocation
                )

            }
        }
    }
}

@Composable
private fun TeachingVenueLocationSection(
    teachingVenue: LocationData?,
    isCapturing: Boolean,
    locationError: String?,
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

        // Location content based on state
        LocationContentState(
            teachingVenue = teachingVenue,
            isCapturing = isCapturing,
            locationError = locationError,
            onCaptureLocation = onCaptureLocation,
        )
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
private fun LocationRadiusSelection(
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
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${radius}m",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Slider(
            value = radius.toFloat(),
            onValueChange = { onRadiusChanged(it.toInt()) },
            valueRange = 10f..210f,
            steps = 200,
            modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun SelectionField(
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
private fun SelectedProgrammesChips(
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
private fun DurationSelection(
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgrammeSelectionDialog(
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
private fun ProgrammeSelectionItem(
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
                    text = "${programme.departmentName} • Year ${programme.yearOfStudy}",
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
private fun UnitSelectionDialog(
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
private fun UnitSelectionItem(
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