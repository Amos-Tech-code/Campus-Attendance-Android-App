package com.amos_tech_code.smartattend.ui.feature.start_session

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.amos_tech_code.smartattend.domain.models.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButtonWithLeadingIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSuccessScreen(
    navController: NavController,
    sessionResponse: StartAttendanceSessionResponse,
    onBackToHome: () -> Unit,
    onShareSession: (String, String) -> Unit,
    onShareQRCode: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Session Created",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {

                    IconButton(
                        onClick = {
                            val shareText = buildSessionShareText(sessionResponse)
                            onShareSession(shareText, sessionResponse.sessionCode)
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            "Share Session",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Success Header
            SuccessHeaderSection()

            // QR Code Section
            QrCodeSection(
                sessionResponse = sessionResponse,
                onShareQRCode = onShareQRCode
            )

            // Session Details Section
            SessionDetailsSection(sessionResponse = sessionResponse)

            // Action Buttons
            ActionButtonsSection(
                sessionResponse = sessionResponse,
                onShareSession = onShareSession,
                onBackToHome = onBackToHome
            )

            // Important Notes
            ImportantNotesSection()
        }
    }
}

@Composable
fun SuccessHeaderSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                "Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Attendance Session Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Students can now join using the code or QR code below",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun QrCodeSection(
    sessionResponse: StartAttendanceSessionResponse,
    onShareQRCode: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // QR Code Title
            Text(
                text = "Scan to Join",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // QR Code Image
            sessionResponse.qrCodeUrl?.let { qrCodeUrl ->
                AsyncImage(
                    model = qrCodeUrl,
                    contentDescription = "QR Code for attendance session",
                    modifier = Modifier
                        .size(220.dp)
                        .shadow(8.dp, MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )
            } ?: run {
                // Fallback if no QR code URL
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            "QR Code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "QR Code Not Available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Session Code and Secret
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Session Code
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Session Code",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sessionResponse.sessionCode,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp
                    )
                }

                // Secret Key
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Secret Key",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sessionResponse.secretKey,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Share QR Code Button
            SmartAttendOutlinedButton(
                text = "Share QR Code",
                onClick = {
                    sessionResponse.qrCodeUrl?.let { onShareQRCode(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.QrCode2,
                        "Share QR Code",
                        modifier = Modifier.size(18.dp)
                    )
                },
                enabled = sessionResponse.qrCodeUrl != null
            )
        }
    }
}

@Composable
fun SessionDetailsSection(sessionResponse: StartAttendanceSessionResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Session Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Unit Information
            DetailRow(
                icon = Icons.Default.Book,
                title = "Unit",
                value = "${sessionResponse.unit.code} - ${sessionResponse.unit.name}"
            )

            // Programmes
            DetailRow(
                icon = Icons.Default.School,
                title = "Programmes",
                value = sessionResponse.programmes.joinToString(", ") { it.name }
            )

            // Time Information
            DetailRow(
                icon = Icons.Default.Schedule,
                title = "Duration",
                value = "${sessionResponse.timeInfo.durationMinutes} minutes"
            )

            // Location (if available)
            sessionResponse.location?.let { location ->
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    title = "Location Radius",
                    value = "${location.radiusMeters}m"
                )
            }

            // Session ID
            DetailRow(
                icon = Icons.Default.Fingerprint,
                title = "Session ID",
                value = sessionResponse.sessionId.take(8) + "..."
            )
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    sessionResponse: StartAttendanceSessionResponse,
    onShareSession: (String, String) -> Unit,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Share Session Button
        SmartAttendPrimaryButtonWithLeadingIcon(
            text = "Share Session Details",
            onClick = {
                val shareText = buildSessionShareText(sessionResponse)
                onShareSession(shareText, sessionResponse.sessionCode)
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.Share,
                    "Share Session",
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        // Back to Home Button
        SmartAttendOutlinedButton(
            text = "Back to Home",
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ImportantNotesSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    "Important",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Important Notes",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "• Share the QR Code with students only if allowed method is by QR CODE SCAN",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• Share the session code and secret key with students if you need them to join without QR CODE SCAN",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• Session will automatically end after the specified duration",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• Students need both code and secret key to join through manual code attendance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to build share text
fun buildSessionShareText(response: StartAttendanceSessionResponse): String {
    return """
        📋 Attendance Session Created
        
        Join my attendance session using the details below:
        
        🔐 Session Code: ${response.sessionCode}
        🗝️ Secret Key: ${response.secretKey}
        
        📚 Unit: ${response.unit.code} - ${response.unit.name}
        ⏱️ Duration: ${response.timeInfo.durationMinutes} minutes
        ${response.location?.let { "📍 Location Radius: ${it.radiusMeters}m" } ?: ""}
        
        Use the SmartAttend student app to join!
    """.trimIndent()
}


// Share session details function
fun shareSessionDetails(context: Context, shareText: String, sessionCode: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Attendance Session - $sessionCode")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Session Details"))
}


// Share QR code function
fun shareQRCode(context: Context, qrCodeUrl: String) {
    // For now, share the URL. You might want to download and share the actual image
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Attendance QR Code")
        putExtra(Intent.EXTRA_TEXT, "Scan this QR code to join the attendance session:\n$qrCodeUrl")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
}