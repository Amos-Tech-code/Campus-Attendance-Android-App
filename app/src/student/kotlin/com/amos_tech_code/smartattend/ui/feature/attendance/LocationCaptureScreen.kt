package com.amos_tech_code.smartattend.ui.feature.attendance

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.ui.components.LocationCapturedState
import com.amos_tech_code.smartattend.ui.components.LocationCapturingState
import com.amos_tech_code.smartattend.ui.components.LocationNotCapturedState
import com.amos_tech_code.smartattend.ui.components.LocationPermissionRationaleDialog
import com.amos_tech_code.smartattend.ui.components.LocationPermissionSettingsDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCaptureScreen(
    viewModel: AttendanceViewModel,
    context: Context,
    isFromQR: Boolean,
    onBack: () -> Unit
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()
    val activity = context as Activity

    // Create launcher for enabling GPS
    val enableGpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // GPS was enabled, try to capture location again
            viewModel.onEvent(AttendanceUiEvent.RetryLocationCapture)
        } else {
            // User canceled or GPS still disabled
            viewModel.onEvent(
                AttendanceUiEvent.LocationError("GPS is required for location verification")
            )
        }
    }

    // State for showing permission dialogs
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Check if we have location data
    val hasLocation = state.studentLocation != null

    // Create launcher for location permission request
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, check if GPS is enabled
            if (viewModel.isLocationEnabled()) {
                // GPS is enabled, start capturing location
                viewModel.onEvent(AttendanceUiEvent.RequestLocation)
            } else {
                // GPS is disabled, prompt to enable it
                viewModel.promptEnableGPS(activity, enableGpsLauncher)
            }
        } else {
            // Permission denied
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )) {
                // Show rationale dialog
                showRationaleDialog = true
            } else {
                // Permission permanently denied, show settings dialog
                showSettingsDialog = true
            }
        }
    }

    // Check permission on initial load
    LaunchedEffect(Unit) {
        when {
            // Check if permission is already granted
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, check if GPS is enabled
                if (viewModel.isLocationEnabled()) {
                    // Start capturing location if not already doing so
                    if (state.locationState == LocationState.IDLE ||
                        state.locationState == LocationState.ERROR) {
                        viewModel.onEvent(AttendanceUiEvent.RequestLocation)
                    }
                } else {
                    // GPS is disabled, prompt to enable it
                    viewModel.promptEnableGPS(activity, enableGpsLauncher)
                }
            }

            // Check if we should show rationale
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showRationaleDialog = true
            }

            // Otherwise request permission
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Handle location state changes to check GPS when needed
    LaunchedEffect(state.locationState) {
        if (state.locationState == LocationState.ERROR &&
            state.locationError?.contains("GPS") == true) {
            // GPS error, prompt to enable it
            viewModel.promptEnableGPS(activity, enableGpsLauncher)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Location Verification",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LocationCaptureOverlay(
            locationState = state.locationState,
            location = state.studentLocation,
            locationError = state.locationError,
            onCaptureLocation = {
                // Check permission before capturing location
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Permission granted, check if GPS is enabled
                        if (viewModel.isLocationEnabled()) {
                            viewModel.onEvent(AttendanceUiEvent.RequestLocation)
                        } else {
                            viewModel.promptEnableGPS(activity, enableGpsLauncher)
                        }
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) -> {
                        showRationaleDialog = true
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            },
            onRecapture = {
                viewModel.onEvent(AttendanceUiEvent.RetryLocationCapture)
            },
            onProceed = {
                viewModel.onEvent(AttendanceUiEvent.ContinueWithCapturedLocation)
            },
            isProceedEnabled = hasLocation && !state.isLoading,
            isLoading = state.isLoading,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        )
    }

    // Permission rationale dialog
    if (showRationaleDialog) {
        LocationPermissionRationaleDialog(
            isFromLecturer = false,
            onDismissRequest = { showRationaleDialog = false },
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
            onDismissRequest = { showSettingsDialog = false }
        )
    }
}


@Composable
private fun LocationCaptureOverlay(
    locationState: LocationState,
    location: LocationData?,
    locationError: String?,
    isProceedEnabled: Boolean,
    isLoading: Boolean,
    onCaptureLocation: () -> Unit,
    onRecapture: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocationOn,
                            "Location Required",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        SmartAttendHeightSpacer(16.dp)
                        Text(
                            text = "Location Verification Required",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        SmartAttendHeightSpacer(8.dp)
                        Text(
                            text = "This session requires location verification. Please capture your current location.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Location Capture Section
                    LocationCaptureSection(
                        locationState = locationState,
                        location = location,
                        locationError = locationError,
                        onCaptureLocation = onCaptureLocation,
                        onRecapture = onRecapture,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Proceed Button
                    SmartAttendPrimaryButton(
                        text = "Proceed to Mark Attendance",
                        onClick = onProceed,
                        isLoading = isLoading,
                        enabled = isProceedEnabled,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


@Composable
private fun LocationCaptureSection(
    locationState: LocationState,
    location: LocationData?,
    locationError: String?,
    onCaptureLocation: () -> Unit,
    onRecapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Location Verification",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (locationState) {
            LocationState.IDLE -> {
                LocationNotCapturedState(
                    onCaptureLocation = onCaptureLocation,
                    error = null
                )
            }

            LocationState.CAPTURING -> {
                LocationCapturingState()
            }

            LocationState.CAPTURED -> {
                location?.let {
                    LocationCapturedState(
                        location = it,
                        onRecapture = onRecapture
                    )
                }
            }

            LocationState.ERROR -> {
                LocationNotCapturedState(
                    onCaptureLocation = onCaptureLocation,
                    error = locationError
                )
            }

            else -> {}
        }
    }
}