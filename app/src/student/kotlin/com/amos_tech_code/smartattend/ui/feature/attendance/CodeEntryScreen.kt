package com.amos_tech_code.smartattend.ui.feature.attendance

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
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

    val onNavigateBackEnabled = state.codeEntryState != CodeEntryState.MARKING_ATTENDANCE &&
            state.codeEntryState != CodeEntryState.VERIFYING_SESSION
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            //verticalArrangement = Arrangement.spacedBy(16.dp),
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.codeEntryState) {
                CodeEntryState.IDLE -> {
                    CodeEntryFormSection(
                        sessionCode = state.sessionCode,
                        unitCode = state.unitCode,
                        onSessionCodeChanged = { viewModel.onEvent(AttendanceUiEvent.UpdateSessionCode(it)) },
                        onUnitCodeChanged = { viewModel.onEvent(AttendanceUiEvent.UpdateUnitCode(it)) },
                        onVerifySession = {
                            viewModel.onEvent(AttendanceUiEvent.VerifySession(state.sessionCode, state.unitCode))
                        },
                        isLoading = state.isLoading,
                        sessionCodeError = if (state.sessionCode.isNotBlank() &&
                            (state.sessionCode.length != 6 ||
                                    !state.sessionCode.all { it.isDigit() }))
                            "Must be 6 digits"
                        else null,
                        unitCodeError = if (state.unitCode.isBlank()) "Required" else null,
                        modifier = Modifier.imePadding()
                    )
                }
                CodeEntryState.VERIFYING_SESSION -> {
                    CodeEntryProgressSection(
                        title = "Verifying Session",
                        message = "Checking session validity...",
                        state = state.codeEntryState,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                CodeEntryState.VERIFIED -> {
                    // Show verified state with mark attendance button
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
                CodeEntryState.MARKING_ATTENDANCE -> {
                    CodeEntryProgressSection(
                        title = "Marking Attendance",
                        message = "Recording your attendance...",
                        state = state.codeEntryState,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                CodeEntryState.ERROR -> {
                    val isMarkingError = state.errorType == AttendanceErrorType.MARKING_ERROR
                    CodeEntryErrorSection(
                        errorMessage = state.codeEntryErrorMessage ?: "Unknown error occurred",
                        isMarkingError = isMarkingError,
                        onRetry = {
                            if (isMarkingError) {
                                viewModel.onEvent(AttendanceUiEvent.RetryMarkAttendance)
                            } else {
                                viewModel.onEvent(AttendanceUiEvent.RetryVerifySession(state.sessionCode, state.unitCode))
                            }
                        },
                        onBackToForm = { viewModel.resetCodeEntry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeEntryFormSection(
    sessionCode: String,
    unitCode: String,
    onSessionCodeChanged: (String) -> Unit,
    onUnitCodeChanged: (String) -> Unit,
    onVerifySession: () -> Unit,
    isLoading: Boolean,
    sessionCodeError: String? = null,
    unitCodeError: String? = null,
    modifier: Modifier = Modifier
) {
    var isSessionCodeTouched by remember { mutableStateOf(false) }
    var isUnitCodeTouched by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Session Code Input
        SmartAttendTextField(
            value = sessionCode,
            onValueChange = {
                onSessionCodeChanged(it)
                isSessionCodeTouched = true
            },
            label = "Session Code",
            placeholder = "Enter 6-digit code",
            leadingIcon = {
                Icon(
                    Icons.Default.Code,
                    "Session Code",
                    tint = if (sessionCode.length == 6)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = isSessionCodeTouched && sessionCodeError != null,
            errorMessage = if (isSessionCodeTouched) sessionCodeError else null,
            supportingMessage = "${sessionCode.length}/6",
            enabled = !isLoading
        )

        // Unit Code Input
        SmartAttendTextField(
            value = unitCode,
            onValueChange = {
                onUnitCodeChanged(it)
                isUnitCodeTouched = true
            },
            label = "Unit Code",
            placeholder = "Enter unit code",
            leadingIcon = {
                Icon(
                    Icons.Default.Book,
                    "Unit Code",
                    tint = if (unitCode.isNotEmpty())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            isError = isUnitCodeTouched && unitCodeError != null,
            errorMessage = if (isUnitCodeTouched) unitCodeError else null,
            supportingMessage = if (unitCode.isNotEmpty()) "${unitCode.length}/8" else "Required",
            enabled = !isLoading
        )

        // Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.medium
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
                    text = "• Session Code: 6-digit code displayed by lecturer\n• Unit Code: Unit code of the unit you are marking attendance for",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Verify Button
        SmartAttendPrimaryButton(
            text = if (isLoading) "Verifying..." else "Verify Session",
            onClick = onVerifySession,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isLoading = isLoading
        )
    }
}

@Composable
private fun CodeEntryProgressSection(
    title: String,
    message: String,
    state: CodeEntryState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
private fun CodeEntryErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
    onBackToForm: () -> Unit,
    isMarkingError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        SmartAttendHeightSpacer(16.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isMarkingError) "Attendance Failed" else "Verification Failed",
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
        SmartAttendHeightSpacer(8.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmartAttendPrimaryButton(
                text = "Try Again",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
            SmartAttendOutlinedButton(
                text = if (isMarkingError) "Back to Verification" else "Back to Form",
                onClick = onBackToForm,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}