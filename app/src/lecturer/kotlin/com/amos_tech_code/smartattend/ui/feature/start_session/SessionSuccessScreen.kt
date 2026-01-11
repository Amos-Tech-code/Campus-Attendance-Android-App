package com.amos_tech_code.smartattend.ui.feature.start_session

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.services.QrCodeSharingService
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendWidthSpacer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSuccessScreen(
    scope: CoroutineScope,
    sessionResponse: StartAttendanceSessionResponse,
    shouldShowSessionSuccess: Boolean = true,
    onLiveAttendanceClick: () -> Unit,
    onBackToHome: () -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSharingQrCode by remember { mutableStateOf(false) }
    var qrShareError by remember { mutableStateOf<String?>(null) }
    var isSessionCodeCopied by remember { mutableStateOf(false) }
    val qrCodeSharingService = remember { QrCodeSharingService(context) }

    // Show success message after copying session code
    LaunchedEffect(isSessionCodeCopied) {
        if (isSessionCodeCopied) {
            delay(3000)
            isSessionCodeCopied = false
        }
    }

    // Error dialog for QR sharing failures
    if (qrShareError != null) {
        AlertDialog(
            onDismissRequest = { qrShareError = null },
            title = { Text("Sharing Failed", fontWeight = FontWeight.SemiBold) },
            text = { Text(qrShareError!!) },
            confirmButton = {
                TextButton(onClick = { qrShareError = null }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (shouldShowSessionSuccess) "Session Created" else "Session Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState)},
        floatingActionButton = {
            if (!shouldShowSessionSuccess) {
                ExtendedFloatingActionButton(
                    onClick = onLiveAttendanceClick,
                    icon = { Icon(Icons.Default.Circle, "Live", tint = MaterialTheme.colorScheme.error) },
                    text = { Text("Live Attendance") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    expanded = !scrollState.isScrollInProgress
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Success Header with Celebration Animation
            if (shouldShowSessionSuccess) {
                AnimatedSuccessHeader()
            }

            // Main Content Cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // QR Code Card (Always show if QR method is enabled)
                if (sessionResponse.method == AttendanceMethod.QR_CODE ||
                    sessionResponse.method == AttendanceMethod.ANY) {
                    QrCodeCard(
                        sessionResponse = sessionResponse,
                        isSharing = isSharingQrCode,
                        onShareQRCode = { qrCodeUrl ->
                            scope.launch {
                                isSharingQrCode = true
                                qrShareError = null
                                try {
                                    qrCodeSharingService.shareQrCodeImage(qrCodeUrl)
                                } catch (e: Exception) {
                                    qrShareError = "Failed to share QR code: ${e.message}"
                                } finally {
                                    isSharingQrCode = false
                                }
                            }
                        }
                    )
                }

                // Session Code Card (Always show)
                SessionCodeCard(
                    sessionCode = sessionResponse.sessionCode,
                    isCopied = isSessionCodeCopied,
                    onCopyClick = {
                        // Copy to clipboard
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Session Code",
                            sessionResponse.sessionCode
                        )
                        clipboard.setPrimaryClip(clip)
                        isSessionCodeCopied = true
                        scope.launch { snackbarHostState.showSnackbar("Session code copied to clipboard") }
                    }
                )

                // Session Details Card
                SessionDetailsCard(sessionResponse = sessionResponse)

                // Important Notes Card
                ImportantNotesCard(sessionResponse = sessionResponse)

                // Action Buttons
                ActionButtonsSection(
                    sessionResponse = sessionResponse,
                    onShareSession = { shareText, sessionCode ->
                        shareSessionDetails(context, shareText, sessionCode)
                    },
                    onBackToHome = onBackToHome,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SmartAttendHeightSpacer(16.dp)
        }
    }
}

@Composable
private fun AnimatedSuccessHeader() {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
            shape = MaterialTheme.shapes.large
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
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Attendance Session Started!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Students can now join using the details below",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QrCodeCard(
    sessionResponse: StartAttendanceSessionResponse,
    isSharing: Boolean,
    onShareQRCode: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "QR Code Attendance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Students scan to mark attendance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // QR Code Display
            sessionResponse.qrCodeUrl?.let { qrCodeUrl ->
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = qrCodeUrl,
                        contentDescription = "QR Code for attendance session",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val state = painter.state
                        when (state) {
                            is AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }

                            is AsyncImagePainter.State.Error -> {
                                // Fallback QR Code
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.QrCode2,
                                            "QR Code",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "QR Code Loading Failed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            else -> {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }

                    // Sharing overlay
                    if (isSharing) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = "Preparing QR Code...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                // Share Button
                Button(
                    onClick = { onShareQRCode(qrCodeUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    enabled = !isSharing,
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                Icons.Default.QrCode2,
                                "Share QR",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        SmartAttendWidthSpacer(12.dp)
                        Text(
                            text = if (isSharing) "Preparing..." else "Share QR Code",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } ?: run {
                // No QR code available
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            "QR Code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        text = "QR Code not available for this session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCodeCard(
    sessionCode: String,
    isCopied: Boolean,
    onCopyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Keyboard,
                    "Session Code",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Session Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Code Display
            Card(
                onClick = onCopyClick,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCopied) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCopied) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(if (isCopied) 4.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sessionCode,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        if (isCopied) "Copied" else "Copy",
                        tint = if (isCopied) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Instructions
            Text(
                text = "Share this code with students for manual attendance entry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SessionDetailsCard(sessionResponse: StartAttendanceSessionResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    "Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Session Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Prepare Data for Grid
            val detailsList = listOf(
                Triple(
                    Icons.Default.Book,
                    "Unit",
                    "${sessionResponse.unit.code} - ${sessionResponse.unit.name}"
                ),
                Triple(
                    Icons.Default.Timer,
                    "Duration",
                    "${sessionResponse.timeInfo.durationMinutes} min"
                ),
                Triple(
                    Icons.Default.CalendarMonth,
                    "Week",
                    "Week ${sessionResponse.weekNumber}"
                ),
                Triple(
                    Icons.Default.HowToReg,
                    "Method",
                    when (sessionResponse.method) {
                        AttendanceMethod.QR_CODE -> "QR Code Only"
                        AttendanceMethod.MANUAL_CODE -> "Manual Code Only"
                        AttendanceMethod.ANY -> "QR & Manual"
                    }
                )
            )

            // Custom 2-Column Grid Implementation
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp) // Space between rows
            ) {
                detailsList.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Space between columns
                    ) {
                        rowItems.forEach { item ->
                            // Use Box with weight(1f) to ensure equal column width
                            Box(modifier = Modifier.weight(1f)) {
                                DetailItem(
                                    icon = item.first,
                                    title = item.second,
                                    value = item.third
                                )
                            }
                        }
                        // If there is an odd number of items, fill the empty space to keep alignment
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ImportantNotesCard(sessionResponse: StartAttendanceSessionResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    "Tips",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Important Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tips based on attendance method
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (sessionResponse.method) {
                    AttendanceMethod.QR_CODE -> {
                        NoteItem(
                            icon = Icons.Default.QrCode2,
                            text = "Share the QR code with students for scanning"
                        )
                        NoteItem(
                            icon = Icons.Default.Smartphone,
                            text = "Students need the ClassTrack app to scan"
                        )
                    }
                    AttendanceMethod.MANUAL_CODE -> {
                        NoteItem(
                            icon = Icons.Default.Keyboard,
                            text = "Share the session code above with students"
                        )
                        NoteItem(
                            icon = Icons.Default.Info,
                            text = "Students need both session and unit code"
                        )
                    }
                    AttendanceMethod.ANY -> {
                        NoteItem(
                            icon = Icons.Default.QrCode2,
                            text = "Students can use either QR code or manual entry"
                        )
                        NoteItem(
                            icon = Icons.Default.Share,
                            text = "Share both QR code and session code"
                        )
                    }
                }

                // Common notes
                NoteItem(
                    icon = Icons.Default.Timer,
                    text = "Session auto-ends after ${sessionResponse.timeInfo.durationMinutes} minutes"
                )
                NoteItem(
                    icon = Icons.Default.LocationOn,
                    text = if (sessionResponse.isLocationRequired)
                        "Location verification is required (${sessionResponse.location?.radiusMeters}m radius)"
                    else "No location restriction"
                )
                NoteItem(
                    icon = Icons.Default.Visibility,
                    text = "Monitor attendance in real-time from Live Attendance"
                )
            }
        }
    }
}

@Composable
private fun NoteItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            "Note",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ActionButtonsSection(
    sessionResponse: StartAttendanceSessionResponse,
    onShareSession: (String, String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Share Session Button
        Button(
            onClick = {
                val shareText = buildSessionShareText(sessionResponse)
                onShareSession(shareText, sessionResponse.sessionCode)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Share,
                    "Share",
                    modifier = Modifier.size(20.dp)
                )
                SmartAttendWidthSpacer(12.dp)
                Text(
                    text = "Share Session Details",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Back to Home Button
        OutlinedButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Home,
                    "Home",
                    modifier = Modifier.size(20.dp)
                )
                SmartAttendWidthSpacer(12.dp)
                Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun buildSessionShareText(response: StartAttendanceSessionResponse): String {
    val qrMethod = when (response.method) {
        AttendanceMethod.QR_CODE -> "QR Code Scanning"
        AttendanceMethod.MANUAL_CODE -> "Manual Code Entry"
        AttendanceMethod.ANY -> "QR Code or Manual Entry"
    }

    return """
        📋 Attendance Session
        
        Join my attendance session:
        
        🔐 Session Code: ${response.sessionCode}
        📚 Unit: ${response.unit.code} - ${response.unit.name}
        ⏱️ Duration: ${response.timeInfo.durationMinutes} minutes
        📍 Method: $qrMethod
        ${response.location?.let { "📍 Location Radius: ${it.radiusMeters}m" } ?: ""}
        
        Mark Attendance using the ClassTrack app!
    """.trimIndent()
}

private fun shareSessionDetails(context: Context, shareText: String, sessionCode: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Attendance Session - $sessionCode")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    // Try to show a nicer share dialog
    try {
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                "Share Session Details"
            ).apply {
                // Add flags for better handling
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: Exception) {
        // Fallback
        context.startActivity(shareIntent)
    }
}
