package com.amos_tech_code.smartattend.ui.feature.attendance

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import com.amos_tech_code.smartattend.ui.components.SmartAttendOutlinedButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEntryScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.attendanceState.collectAsStateWithLifecycle()
    val onNavigateBackEnabled = !(state.codeEntryState == CodeEntryState.MARKING_ATTENDANCE || state.codeEntryState == CodeEntryState.VERIFYING_SESSION)
    BackHandler(enabled = onNavigateBackEnabled) {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (state.codeEntryState) {
                            CodeEntryState.VERIFYING_SESSION -> "Verifying Session..."
                            CodeEntryState.VERIFIED -> "Session Verified"
                            CodeEntryState.MARKING_ATTENDANCE -> "Marking Attendance..."
                            CodeEntryState.ERROR -> "Error"
                            else -> "Enter Session Code"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // State-specific header
            CodeEntryHeaderSection(state.codeEntryState)

            when (state.codeEntryState) {
                CodeEntryState.IDLE -> {
                    CodeEntryFormSection(
                        sessionCode = state.sessionCode,
                        secretKey = state.secretKey,
                        onSessionCodeChanged = { viewModel.onEvent(AttendanceUiEvent.UpdateSessionCode(it)) },
                        onSecretKeyChanged = { viewModel.onEvent(AttendanceUiEvent.UpdateSecretKey(it)) },
                        onVerifySession = {
                            viewModel.onEvent(AttendanceUiEvent.VerifySession(state.sessionCode, state.secretKey))
                        },
                        isFormValid = state.sessionCode.isNotBlank() && state.secretKey.isNotBlank(),
                        isLoading = state.isLoading
                    )
                }
                CodeEntryState.VERIFYING_SESSION -> {
                    CodeEntryProgressSection(
                        title = "Verifying Session",
                        message = "Checking session validity...",
                        state = state.codeEntryState
                    )
                }
                CodeEntryState.VERIFIED -> {
                    CodeEntryProgressSection(
                        title = "Session Verified",
                        message = "Session verified successfully!",
                        state = state.codeEntryState
                    )
                }
                CodeEntryState.MARKING_ATTENDANCE -> {
                    CodeEntryProgressSection(
                        title = "Marking Attendance",
                        message = "Recording your attendance...",
                        state = state.codeEntryState
                    )
                }
                CodeEntryState.ERROR -> {
                    CodeEntryErrorSection(
                        errorMessage = state.codeEntryErrorMessage ?: "Unknown error occurred",
                        onRetry = {
                            if (state.sessionCode.isNotBlank() && state.secretKey.isNotBlank()) {
                                viewModel.onEvent(AttendanceUiEvent.VerifySession(state.sessionCode, state.secretKey))
                            } else {
                                viewModel.resetCodeEntry()
                            }
                        },
                        onBackToForm = { viewModel.resetCodeEntry() }
                    )
                }
            }

            // Session info (show when verified)
            if (state.codeEntryState == CodeEntryState.VERIFIED) {
                state.verificationResult?.let { verification ->
                    SessionInfoSection(verification = verification)
                }
            }
        }
    }
}

@Composable
fun CodeEntryHeaderSection(codeEntryState: CodeEntryState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (codeEntryState) {
                CodeEntryState.VERIFYING_SESSION -> MaterialTheme.colorScheme.surfaceVariant
                CodeEntryState.VERIFIED -> MaterialTheme.colorScheme.primaryContainer
                CodeEntryState.MARKING_ATTENDANCE -> MaterialTheme.colorScheme.surfaceVariant
                CodeEntryState.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
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
                imageVector = when (codeEntryState) {
                    CodeEntryState.VERIFYING_SESSION -> Icons.Default.Schedule
                    CodeEntryState.VERIFIED -> Icons.Default.CheckCircle
                    CodeEntryState.MARKING_ATTENDANCE -> Icons.Default.Pending
                    CodeEntryState.ERROR -> Icons.Default.Error
                    else -> Icons.Default.Keyboard
                },
                contentDescription = "Status",
                tint = when (codeEntryState) {
                    CodeEntryState.VERIFYING_SESSION -> MaterialTheme.colorScheme.primary
                    CodeEntryState.VERIFIED -> MaterialTheme.colorScheme.primary
                    CodeEntryState.MARKING_ATTENDANCE -> MaterialTheme.colorScheme.primary
                    CodeEntryState.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = when (codeEntryState) {
                        CodeEntryState.VERIFYING_SESSION -> "Verifying Session"
                        CodeEntryState.VERIFIED -> "Session Verified"
                        CodeEntryState.MARKING_ATTENDANCE -> "Marking Attendance"
                        CodeEntryState.ERROR -> "Error Occurred"
                        else -> "Enter Session Details"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (codeEntryState) {
                        CodeEntryState.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        CodeEntryState.VERIFIED -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = when (codeEntryState) {
                        CodeEntryState.VERIFYING_SESSION -> "Please wait while we verify the session"
                        CodeEntryState.VERIFIED -> "Session verified successfully!"
                        CodeEntryState.MARKING_ATTENDANCE -> "Recording your attendance..."
                        CodeEntryState.ERROR -> "An error occurred during verification"
                        else -> "Get the session code and secret key from your lecturer"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (codeEntryState) {
                        CodeEntryState.ERROR -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        CodeEntryState.VERIFIED -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun CodeEntryFormSection(
    sessionCode: String,
    secretKey: String,
    onSessionCodeChanged: (String) -> Unit,
    onSecretKeyChanged: (String) -> Unit,
    onVerifySession: () -> Unit,
    isFormValid: Boolean,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Session Code Input
        SmartAttendTextField(
            value = sessionCode,
            onValueChange = onSessionCodeChanged,
            label = "Session Code",
            placeholder = "Enter 6-digit code",
            leadingIcon = { Icon(Icons.Default.Code, "Session Code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                autoCorrect = false
            ),
            supportingMessage = "${sessionCode.length}/6"
        )

        // Secret Key Input
        SmartAttendTextField(
            value = secretKey,
            onValueChange = onSecretKeyChanged,
            label = "Secret Key",
            placeholder = "Enter 8-character key",
            leadingIcon = { Icon(Icons.Default.Key, "Secret Key") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                autoCorrect = false
            ),
            supportingMessage = "${secretKey.length}/8"
        )

        // Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Where to find these?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "• Session Code: 6-digit code displayed by lecturer\n• Secret Key: 8-character key shared by lecturer",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Verify Button
        SmartAttendPrimaryButton(
            text = "Verify Session",
            onClick = onVerifySession,
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid && !isLoading
        )
    }
}

@Composable
fun CodeEntryProgressSection(
    title: String,
    message: String,
    state: CodeEntryState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Animated progress indicator
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state == CodeEntryState.VERIFYING_SESSION || state == CodeEntryState.MARKING_ATTENDANCE) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Additional loading text for marking attendance
        if (state == CodeEntryState.MARKING_ATTENDANCE) {
            Text(
                text = "Please wait...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun CodeEntryErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
    onBackToForm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            Icons.Default.Error,
            "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(60.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Verification Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartAttendPrimaryButton(
                text = "Try Again",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
            SmartAttendOutlinedButton(
                text = "Back to Form",
                onClick = onBackToForm,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SessionInfoSection(verification: VerifyAttendanceResponse) {
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
                text = "Session Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            DetailRow(
                icon = Icons.Default.Book,
                title = "Unit",
                value = "${verification.sessionInfo.unitCode} - ${verification.sessionInfo.unitName}"
            )

            DetailRow(
                icon = Icons.Default.Person,
                title = "Lecturer",
                value = verification.sessionInfo.lecturerName
            )

            if (verification.requiresProgrammeSelection) {
                DetailRow(
                    icon = Icons.Default.Info,
                    title = "Note",
                    value = "Please select your programme to continue"
                )
            }
        }
    }
}
