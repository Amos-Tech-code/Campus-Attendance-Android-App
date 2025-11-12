package com.amos_tech_code.smartattend.ui.feature.attendance

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amos_tech_code.smartattend.domain.models.SeverityLevel
import com.amos_tech_code.smartattend.domain.response.AttendanceFlag
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.VerificationResult
import com.amos_tech_code.smartattend.ui.components.SmartAttendOutlinedButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSuccessScreen(
    result: MarkAttendanceResponse,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Attendance Marked",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Success Header
            SuccessHeaderSection()

            // Verification Results
            VerificationResultsSection(verification = result.verification)

            // Attendance Details
            AttendanceDetailsSection(result = result)

            // Flags/Warnings (if any)
            if (result.flags.isNotEmpty()) {
                AttendanceFlagsSection(flags = result.flags)
            }

            // Action Buttons
            ActionButtonsSection(
                onBack = onBack,
                onShare = {
                    shareAttendanceResult(context, result)
                }
            )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                "Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Column {
                Text(
                    text = "Attendance Marked Successfully!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Your attendance has been recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun VerificationResultsSection(verification: VerificationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Verification Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Location Verification
            VerificationItem(
                verified = verification.locationVerified,
                title = "Location Verified",
                description = "Your location matches the session requirements"
            )

            // Device Verification
            VerificationItem(
                verified = verification.deviceVerified,
                title = "Device Verified",
                description = "Your device is authorized for this session"
            )

            // Method Verification
            VerificationItem(
                verified = verification.methodVerified,
                title = "Method Verified",
                description = "Attendance method validated successfully"
            )

            // Overall Status
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Status",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = if (verification.overallVerified) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (verification.overallVerified) "VERIFIED" else "NOT VERIFIED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationItem(
    verified: Boolean,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (verified) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = if (verified) "Verified" else "Not Verified",
            tint = if (verified) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceDetailsSection(result: MarkAttendanceResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Attendance Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            DetailRow(
                icon = Icons.Default.Fingerprint,
                title = "Session ID",
                value = result.sessionId.take(8) + "..."
            )

            DetailRow(
                icon = Icons.Default.Schedule,
                title = "Attended At",
                value = formatAttendanceTime(result.attendedAt)
            )

            result.programmeId?.let { programmeId ->
                DetailRow(
                    icon = Icons.Default.School,
                    title = "Programme",
                    value = programmeId.take(8) + "..."
                )
            }

            result.message?.let { message ->
                DetailRow(
                    icon = Icons.Default.Info,
                    title = "Message",
                    value = message
                )
            }
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
fun AttendanceFlagsSection(flags: List<AttendanceFlag>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    "Flags",
                    tint = getSeverityColor(flags.maxByOrNull { it.severity.ordinal }?.severity),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Attendance Flags",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            flags.forEach { flag ->
                FlagItem(flag = flag)
            }
        }
    }
}

@Composable
fun FlagItem(flag: AttendanceFlag) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = when (flag.severity) {
                SeverityLevel.LOW -> Icons.Default.Info
                SeverityLevel.MEDIUM -> Icons.Default.Warning
                SeverityLevel.HIGH -> Icons.Default.Error
                else -> Icons.Default.Info
            },
            contentDescription = "Flag severity",
            tint = getSeverityColor(flag.severity),
            modifier = Modifier.size(16.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = flag.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SmartAttendOutlinedButton(
            text = "Share Attendance",
            onClick = onShare,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.Share,
                    "Share",
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        SmartAttendPrimaryButton(
            text = "Back to Home",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Helper functions
fun formatAttendanceTime(attendedAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(attendedAt)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        attendedAt
    }
}

@Composable
fun getSeverityColor(severity: SeverityLevel?): Color {
    return when (severity) {
        SeverityLevel.LOW -> Color(0xFF2196F3) // Blue
        SeverityLevel.MEDIUM -> Color(0xFFFF9800) // Orange
        SeverityLevel.HIGH -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

fun shareAttendanceResult(context: Context, result: MarkAttendanceResponse) {
    val shareText = buildString {
        append("✅ Attendance Marked Successfully!\n\n")
        append("Session: ${result.sessionId.take(8)}...\n")
        append("Time: ${formatAttendanceTime(result.attendedAt)}\n")
        append("Status: ${if (result.verification.overallVerified) "VERIFIED" else "NOT VERIFIED"}\n\n")

        if (result.flags.isNotEmpty()) {
            append("Flags:\n")
            result.flags.forEach { flag ->
                append("• ${flag.message}\n")
            }
        }

        append("\nShared via SmartAttend")
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Attendance Confirmation")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Attendance Result"))
}