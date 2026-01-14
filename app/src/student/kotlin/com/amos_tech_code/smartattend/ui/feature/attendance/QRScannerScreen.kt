package com.amos_tech_code.smartattend.ui.feature.attendance

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonSize
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendOutlinedButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    viewModel: AttendanceViewModel,
    context: Context,
    onBack: () -> Unit
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )

    // Navigation back should work in most states except when actively marking
    val onNavigateBackEnabled = state.qrScannerState != QRScannerState.MARKING_ATTENDANCE &&
            state.codeEntryState != CodeEntryState.VERIFYING_SESSION

    // Check camera permission
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    BackHandler(enabled = onNavigateBackEnabled) {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (state.qrScannerState) {
                            QRScannerState.SCANNING -> "Scanning QR Code..."
                            QRScannerState.SCANNED -> "QR Code Scanned"
                            QRScannerState.VERIFYING_SESSION -> "Verifying Session..."
                            QRScannerState.MARKING_ATTENDANCE -> "Marking Attendance..."
                            QRScannerState.ERROR -> "Error"
                            else -> "Scan QR Code"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = onNavigateBackEnabled
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show different content based on scanner state
            when {
                // Camera permission denied states
                cameraPermissionState.status.shouldShowRationale -> {
                    PermissionRationaleContent(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                        onBack = onBack
                    )
                }

                !cameraPermissionState.status.isGranted -> {
                    PermissionDeniedContent(onBack = onBack, context = context)
                }

                // VERIFIED state - show verification success
                state.qrScannerState == QRScannerState.VERIFIED -> {
                    VerifiedSessionState(
                        verificationResult = state.verificationResult,
                        locationState = state.locationState,
                        studentLocation = state.studentLocation,
                        onMarkAttendance = {
                            viewModel.onEvent(AttendanceUiEvent.MarkAttendanceVerifiedSession)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // ERROR state
                state.qrScannerState == QRScannerState.ERROR -> {
                    ErrorState(
                        errorMessage = state.errorMessage,
                        isMarkingError = state.errorType == AttendanceErrorType.MARKING_ERROR,
                        onRetryVerify = { viewModel.retryVerifySession() },
                        onRetryMarkAttendance = { viewModel.retryMarkAttendance() },
                        onBack = { viewModel.resetQRScanner() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Scanning states (IDLE, SCANNING, SCANNED, VERIFYING, MARKING)
                else -> {
                    // Only show camera when in appropriate states
                    if (state.qrScannerState == QRScannerState.IDLE ||
                        state.qrScannerState == QRScannerState.SCANNING ||
                        state.qrScannerState == QRScannerState.SCANNED ||
                        state.qrScannerState == QRScannerState.VERIFYING_SESSION ||
                        state.qrScannerState == QRScannerState.MARKING_ATTENDANCE) {

                        QRScannerContent(
                            coroutineScope = coroutineScope,
                            context = context,
                            onQRCodeScanned = { qrData ->
                                viewModel.onEvent(AttendanceUiEvent.QRCodeScanned(qrData))
                            },
                            isScanningEnabled = state.qrScannerState == QRScannerState.IDLE ||
                                    state.qrScannerState == QRScannerState.SCANNING,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Scanner overlay
                        ScannerOverlayWithState(
                            scannerState = state.qrScannerState,
                            errorMessage = state.errorMessage,
                            onRetry = { viewModel.resetQRScanner() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun QRScannerContent(
    coroutineScope: CoroutineScope,
    context: Context,
    onQRCodeScanned: (String) -> Unit,
    isScanningEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(cameraProviderFuture) {
        cameraProvider = cameraProviderFuture.get()
    }

    // Only show camera when scanning is enabled
    if (isScanningEnabled) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = modifier,
            update = { previewView ->
                cameraProvider?.let { provider ->
                    val preview = Preview.Builder().build()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1080, 1920))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        if (!isScanningEnabled) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val image = imageProxy.image ?: return@setAnalyzer

                        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)

                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { qrData ->
                                    // Process QR code on main thread
                                    coroutineScope.launch {
                                        onQRCodeScanned(qrData)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }

                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
            cameraProvider?.unbindAll()
        }
    }
}

@Composable
private fun ScannerOverlayWithState(
    scannerState: QRScannerState,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        // Scanner frame
        Box(
            modifier = Modifier.size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated scanner frame based on state
            AnimatedScannerFrame(scannerState = scannerState)

            // State-specific content
            when (scannerState) {
                QRScannerState.SCANNING -> {
                    ScanningContent()
                }
                QRScannerState.SCANNED -> {
                    ScannedContent()
                }
                QRScannerState.VERIFYING_SESSION -> {
                    VerifyingContent("Verifying session...")
                }

                QRScannerState.MARKING_ATTENDANCE -> {
                    VerifyingContent("Marking attendance...")
                }
                QRScannerState.ERROR -> {
                    ErrorContent(errorMessage ?: "Unknown error", onRetry)
                }
                else -> {
                    // IDLE state - show instructions
                    InstructionsContent()
                }
            }
        }
    }
}

@Composable
private fun AnimatedScannerFrame(scannerState: QRScannerState) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 250f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "scanline"
    )

    Canvas(modifier = Modifier.size(250.dp)) {
        val cornerSize = 40.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val frameColor = when (scannerState) {
            QRScannerState.SCANNING -> Color.Green
            QRScannerState.SCANNED -> Color.Blue
            QRScannerState.VERIFYING_SESSION -> Color.Yellow
            QRScannerState.VERIFIED -> Color(0xFF4CAF50) // Green
            QRScannerState.MARKING_ATTENDANCE -> Color.Yellow
            QRScannerState.ERROR -> Color.Red
            else -> Color.Green
        }

        // Draw all four corners
        // Top-left corner
        drawLine(
            color = frameColor,
            start = Offset(0f, cornerSize),
            end = Offset(0f, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(0f, 0f),
            end = Offset(cornerSize, 0f),
            strokeWidth = strokeWidth
        )

        // Top-right corner
        drawLine(
            color = frameColor,
            start = Offset(size.width - cornerSize, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, cornerSize),
            strokeWidth = strokeWidth
        )

        // Bottom-left corner
        drawLine(
            color = frameColor,
            start = Offset(0f, size.height - cornerSize),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(0f, size.height),
            end = Offset(cornerSize, size.height),
            strokeWidth = strokeWidth
        )

        // Bottom-right corner
        drawLine(
            color = frameColor,
            start = Offset(size.width - cornerSize, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = Offset(size.width, size.height - cornerSize),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )

        // Scanning line (only in scanning state)
        if (scannerState == QRScannerState.SCANNING) {
            drawLine(
                color = Color.Green.copy(alpha = 0.8f),
                start = Offset(0f, scanLineOffset),
                end = Offset(size.width, scanLineOffset),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun ScanningContent() {
    // Just show the animated frame, no additional content
}

@Composable
private fun ScannedContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            "Scanned",
            tint = Color.Blue,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "QR Code Scanned",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InstructionsContent() {
    Text(
        text = "Position QR code within the frame",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
        modifier = Modifier
        //.padding(bottom = 100.dp)
    )
}

@Composable
private fun VerifyingContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Error,
            "Error",
            tint = Color.Red,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        SmartAttendPrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            size = SmartAttendButtonSize.Small
        )
    }
}

/*
@Composable
fun ErrorState(
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            SmartAttendHeightSpacer(16.dp)

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            SmartAttendHeightSpacer(8.dp)

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            SmartAttendHeightSpacer(24.dp)

            SmartAttendOutlinedButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )

            SmartAttendHeightSpacer(16.dp)

            SmartAttendPrimaryButton(
                text = "Go Back",
                size = SmartAttendButtonSize.Small,
                onClick = onBack
            )

        }
    }
}

 */

@Composable
private fun ErrorState(
    errorMessage: String?,
    onRetryVerify: () -> Unit,
    onRetryMarkAttendance: () -> Unit,
    onBack: () -> Unit,
    isMarkingError: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            SmartAttendHeightSpacer(16.dp)

            Text(
                text = if (isMarkingError) "Attendance Failed" else "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            SmartAttendHeightSpacer(8.dp)

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            SmartAttendHeightSpacer(32.dp)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmartAttendPrimaryButton(
                    text = if (isMarkingError) "Retry Marking" else "Retry Verification",
                    onClick = if (isMarkingError) onRetryMarkAttendance else onRetryVerify,
                    modifier = Modifier.fillMaxWidth()
                )

                SmartAttendOutlinedButton(
                    text = if (isMarkingError) "Back to Verification" else "Go Back",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PermissionRationaleContent(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CameraAlt,
            "Camera Permission",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        SmartAttendHeightSpacer(24.dp)

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        SmartAttendHeightSpacer(16.dp)

        Text(
            text = "ClassTrack needs camera access to scan QR codes for attendance. The camera is only used for scanning and no images are stored.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        SmartAttendHeightSpacer(32.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartAttendPrimaryButton(
                text = "Grant Permission",
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            )

            SmartAttendOutlinedButton(
                text = "Go Back",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onBack: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.NoPhotography,
            "Camera Denied",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        SmartAttendHeightSpacer(24.dp)

        Text(
            text = "Camera Access Denied",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        SmartAttendHeightSpacer(16.dp)

        Text(
            text = "You have denied camera permission. To scan QR codes, you need to enable camera access in your device settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        SmartAttendHeightSpacer(32.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartAttendPrimaryButton(
                text = "Open Settings",
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            )

            SmartAttendOutlinedButton(
                text = "Go Back",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}