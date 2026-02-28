package com.amos_tech_code.smartattend.ui.feature.attendance

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.amos_tech_code.smartattend.domain.models.QRCodeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()
    val currentPhotoPickerState = state.currentScreen as? AttendanceScreen.PhotoPicker

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(AttendanceUiEvent.PhotoPickerProcessing)
            viewModel.onEvent(AttendanceUiEvent.ImageSelected(uri))
        } else {
            viewModel.onEvent(AttendanceUiEvent.PhotoPickerError("No image selected"))
        }
    }

    LaunchedEffect(currentPhotoPickerState) {
        if (currentPhotoPickerState is AttendanceScreen.PhotoPicker.Selecting) {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val canGoBack = currentPhotoPickerState is AttendanceScreen.PhotoPicker.Selecting ||
            currentPhotoPickerState is AttendanceScreen.PhotoPicker.Error

    BackHandler(enabled = canGoBack) {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentPhotoPickerState) {
                            is AttendanceScreen.PhotoPicker.Selecting -> "Select QR Code Image"
                            is AttendanceScreen.PhotoPicker.Processing -> "Processing Image"
                            is AttendanceScreen.PhotoPicker.Success -> "QR Code Found"
                            is AttendanceScreen.PhotoPicker.VerifyingSession -> "Verifying Session..."
                            is AttendanceScreen.PhotoPicker.Verified -> "Session Verified"
                            is AttendanceScreen.PhotoPicker.MarkingAttendance -> "Marking Attendance..."
                            is AttendanceScreen.PhotoPicker.Error -> "Error"
                            else -> "Photo Picker"
                        }
                    )
                },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (currentPhotoPickerState) {
                is AttendanceScreen.PhotoPicker.Selecting -> {
                    PhotoPickerSelectingContent()
                }

                is AttendanceScreen.PhotoPicker.Processing -> {
                    PhotoPickerProcessingContent(uri = state.selectedImageUri)
                }

                is AttendanceScreen.PhotoPicker.Success -> {
                    PhotoPickerSuccessContent(
                        qrData = currentPhotoPickerState.qrData,
                        onContinue = {
                            // Parse QR data and verify session
                            val qrCodeData = QRCodeData.fromJson(currentPhotoPickerState.qrData)
                            if (qrCodeData != null && QRCodeData.isValid(qrCodeData)) {
                                viewModel.verifySessionFromPhotoPicker(
                                    qrCodeData.sessionCode,
                                    qrCodeData.unitCode
                                )
                            } else {
                                viewModel.onEvent(
                                    AttendanceUiEvent.PhotoPickerError("Invalid QR code format")
                                )
                            }
                        },
                        onBack = onBack
                    )
                }

                is AttendanceScreen.PhotoPicker.VerifyingSession -> {
                    PhotoPickerProgressContent(
                        message = "Verifying session...",
                        subMessage = "Please wait"
                    )
                }

                is AttendanceScreen.PhotoPicker.Verified -> {
                    VerifiedSessionState(
                        verificationResult = state.verificationResult,
                        locationState = state.locationState,
                        studentLocation = state.studentLocation,
                        onMarkAttendance = {
                            viewModel.onEvent(AttendanceUiEvent.MarkAttendanceVerifiedSession)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is AttendanceScreen.PhotoPicker.MarkingAttendance -> {
                    PhotoPickerProgressContent(
                        message = "Marking attendance...",
                        subMessage = "Recording your attendance"
                    )
                }

                is AttendanceScreen.PhotoPicker.Error -> {
                    PhotoPickerErrorContent(
                        message = currentPhotoPickerState.message,
                        onRetry = { viewModel.onEvent(AttendanceUiEvent.RetryPhotoPicker) },
                        onBack = onBack
                    )
                }

                else -> {
                    Text("Invalid state")
                }
            }

            // Show Location Capture as overlay (this will appear on top of PhotoPicker)
            if (state.showLocationCapture) {
                LocationCaptureScreen(
                    viewModel = viewModel,
                    context = context,
                    onBack = { viewModel.onEvent(AttendanceUiEvent.CancelLocationCapture) }
                )
            }

            // Show Programme Selection as overlay (this will appear on top of PhotoPicker)
            if (state.showProgrammeSelection) {
                ProgrammeSelectionDialog(
                    programmes = state.verificationResult?.availableProgrammes ?: emptyList(),
                    onProgrammeSelected = { programmeId ->
                        viewModel.onEvent(AttendanceUiEvent.ProgrammeSelected(programmeId))
                    },
                    onDismiss = {
                        viewModel.onEvent(AttendanceUiEvent.ResetState)
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoPickerProgressContent(
    message: String,
    subMessage: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )

        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = subMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhotoPickerSelectingContent(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Select an image containing a QR code",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = "The QR code will be automatically detected",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Optional: Show a sample QR code image or illustration
        Card(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun PhotoPickerProcessingContent(
    uri: Uri?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )

        Text(
            text = "Processing image...",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Detecting QR code",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Show selected image preview
        uri?.let {
            AsyncImage(
                model = it,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun PhotoPickerSuccessContent(
    qrData: String,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "QR Code Detected!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = qrData.take(50) + if (qrData.length > 50) "..." else "",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun PhotoPickerErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = "Failed to Process Image",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Text(
            text = "Please ensure the image contains a clear QR code",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Text("Try Again")
            }
        }
    }
}
