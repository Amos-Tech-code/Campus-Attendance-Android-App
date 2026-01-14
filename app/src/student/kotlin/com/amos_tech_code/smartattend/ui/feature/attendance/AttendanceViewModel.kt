package com.amos_tech_code.smartattend.ui.feature.attendance

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AttendanceRepository
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.QRCodeData
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.services.LocationService
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import com.amos_tech_code.smartattend.utils.VibrationHelper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val locationService: LocationService,
    private val session: ClassTrackSession,
    context: Context
) : ViewModel()
{

    private val _attendanceState = MutableStateFlow(StudentAttendanceState())
    val attendanceState = _attendanceState.asStateFlow()

    private val _event = Channel<AttendanceEvent>()
    val event = _event.receiveAsFlow()

    private val vibrationHelper = VibrationHelper(context)

    fun onEvent(event: AttendanceUiEvent) {
        when (event) {
            is AttendanceUiEvent.UpdateSessionCode -> {
                _attendanceState.update {
                    it.copy(
                        sessionCode = event.sessionCode.uppercase().take(6),
                        codeEntryErrorMessage = null
                    )
                }
            }
            is AttendanceUiEvent.UpdateUnitCode -> {
                _attendanceState.update {
                    it.copy(
                        unitCode = event.unitCode.take(8),
                        codeEntryErrorMessage = null
                    )
                }
            }
            is AttendanceUiEvent.VerifySession -> {
                verifySessionFromCode(event.sessionCode, event.unitCode)
            }
            is AttendanceUiEvent.MarkAttendance -> {
                markAttendance(event.request)
            }
            is AttendanceUiEvent.ProgrammeSelected -> {
                markAttendanceWithProgramme(event.programmeId)
            }
            is AttendanceUiEvent.QRCodeScanned -> {
                processQRCode(event.qrData)
            }
            AttendanceUiEvent.ResetState -> {
                _attendanceState.update { StudentAttendanceState() }
            }

            AttendanceUiEvent.RequestLocation -> {
                requestLocation()
            }

            is AttendanceUiEvent.LocationCaptured -> {
                _attendanceState.update {
                    it.copy(
                        locationState = LocationState.CAPTURED,
                        studentLocation = event.location,
                        locationError = null
                    )
                }
            }

            AttendanceUiEvent.RetryLocationCapture -> {
                requestLocation()
            }

            is AttendanceUiEvent.LocationError -> {
                _attendanceState.update {
                    it.copy(
                        locationState = LocationState.ERROR,
                        locationError = event.error
                    )
                }
            }

            is AttendanceUiEvent.MarkAttendanceVerifiedSession -> {
                // Get current state
                val state = _attendanceState.value
                val isFromQR = state.showQRScanner

                // Check if we're in ERROR state and need to reset first
                if (isFromQR && state.qrScannerState == QRScannerState.ERROR) {
                    // Reset QR scanner state before proceeding
                    _attendanceState.update {
                        it.copy(
                            qrScannerState = QRScannerState.VERIFIED,
                            errorMessage = null
                        )
                    }
                } else if (!isFromQR && state.codeEntryState == CodeEntryState.ERROR) {
                    // Reset code entry state before proceeding
                    _attendanceState.update {
                        it.copy(
                            codeEntryState = CodeEntryState.VERIFIED,
                            codeEntryErrorMessage = null
                        )
                    }
                }

                state.currentSessionCode?.let { sessionCode ->
                    state.currentUnitCode?.let { unitCode ->
                        if (isFromQR) {
                            markAttendanceDirectlyFromQR(sessionCode, unitCode)
                        } else {
                            markAttendanceDirectlyFromCode(sessionCode, unitCode)
                        }
                    }
                }
            }

            AttendanceUiEvent.ContinueWithCapturedLocation -> {
                val state = _attendanceState.value
                val verificationResult = state.verificationResult

                // After location is captured, check if we need programme selection
                if (verificationResult?.requiresProgrammeSelection == true) {
                    _attendanceState.update {
                        it.copy(
                            showLocationCapture = false,
                            showProgrammeSelection = true
                        )
                    }
                } else {
                    // If no programme selection needed, proceed to mark attendance
                    onEvent(AttendanceUiEvent.MarkAttendanceVerifiedSession)
                }
            }

            AttendanceUiEvent.CancelLocationCapture -> {
                _attendanceState.update {
                    it.copy(showLocationCapture = false)
                }
                // Optionally reset to IDLE state
                if (_attendanceState.value.showQRScanner) {
                    resetQRScanner()
                } else {
                    resetCodeEntry()
                }
            }
        }
    }

    fun showQrScanner() {
        _attendanceState.update {
            it.copy(
                showQRScanner = true,
                showCodeEntry = false,
                qrScannerState = QRScannerState.IDLE,
                scannedQRData = null,
                errorMessage = null
            )
        }
    }

    fun showCodeEntry() {
        _attendanceState.update {
            it.copy(
                showCodeEntry = true,
                showQRScanner = false,
                codeEntryState = CodeEntryState.IDLE,
//                sessionCode = "",
//                unitCode = "",
                codeEntryErrorMessage = null
            )
        }
    }

    private fun processQRCode(qrData: String) {
        var shouldProcess = false
        _attendanceState.update { currentState ->
            if (currentState.qrScannerState == QRScannerState.IDLE || currentState.qrScannerState == QRScannerState.SCANNING) {
                shouldProcess = true
                currentState.copy(
                    qrScannerState = QRScannerState.SCANNED,
                    scannedQRData = qrData
                )
            } else {
                currentState
            }
        }

        if (!shouldProcess) {
            return
        }

        // Parse QR code data
        val qrCodeData = QRCodeData.fromJson(qrData)

        if (qrCodeData == null || !QRCodeData.isValid(qrCodeData)) {
            _attendanceState.update {
                it.copy(
                    qrScannerState = QRScannerState.ERROR,
                    errorMessage = "Invalid QR code format"
                )
            }
            _event.trySend(AttendanceEvent.ShowErrorMessage("Invalid QR code"))
            return
        }

        // Verify the session from QR code
        verifySessionFromQR(qrCodeData.sessionCode, qrCodeData.unitCode)
    }

    private fun verifySessionFromQR(sessionCode: String, unitCode: String) {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.VERIFYING_SESSION,
                isLoading = true,
                errorMessage = null,
                currentSessionCode = sessionCode,
                currentUnitCode = unitCode
            )
        }

        verifySessionInternal(sessionCode, unitCode, isFromQR = true)
    }

    private fun verifySessionFromCode(sessionCode: String, unitCode: String) {
        _attendanceState.update {
            it.copy(
                codeEntryState = CodeEntryState.VERIFYING_SESSION,
                isLoading = true,
                codeEntryErrorMessage = null,
                currentSessionCode = sessionCode,
                currentUnitCode = unitCode
            )
        }

        verifySessionInternal(sessionCode, unitCode, isFromQR = false)
    }

    private fun verifySessionInternal(sessionCode: String, unitCode: String, isFromQR: Boolean) {
        viewModelScope.launch {
            try {
                val result = attendanceRepository.verifyAttendanceSession(
                    VerifySessionRequest(sessionCode, unitCode)
                )

                when (result) {
                    is ApiResult.Success -> {
                        val requiresProgrammeSelection = result.data.requiresProgrammeSelection
                        val requiresLocation = result.data.requiresLocation

                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.VERIFIED,
                                    isLoading = false,
                                    verificationResult = result.data,
                                    // Don't show programme selection immediately
                                    showProgrammeSelection = false,
                                    // Don't show location capture immediately
                                    showLocationCapture = false
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.VERIFIED,
                                    isLoading = false,
                                    verificationResult = result.data,
                                    // Don't show programme selection immediately
                                    showProgrammeSelection = false,
                                    // Don't show location capture immediately
                                    showLocationCapture = false
                                )
                            }
                        }

                        // Vibrate on successful verification
                        vibrationHelper.vibrate(100)

                        // Determine what to show next based on requirements
                        if (requiresLocation && _attendanceState.value.studentLocation == null) {
                            // Show location capture first
                            _attendanceState.update {
                                it.copy(showLocationCapture = true)
                            }
                        } else if (requiresProgrammeSelection) {
                            // Show programme selection
                            _attendanceState.update {
                                it.copy(showProgrammeSelection = true)
                            }
                        } else {
                            // If nothing else required, mark attendance directly
                            if (isFromQR) {
                                markAttendanceDirectlyFromQR(sessionCode, unitCode)
                            } else {
                                markAttendanceDirectlyFromCode(sessionCode, unitCode)
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = extractApiErrorMessage(result.error)
                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.ERROR,
                                    isLoading = false,
                                    errorMessage = errorMessage
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.ERROR,
                                    isLoading = false,
                                    codeEntryErrorMessage = errorMessage
                                )
                            }
                        }
                        _event.send(AttendanceEvent.ShowErrorMessage(errorMessage))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred"
                if (isFromQR) {
                    _attendanceState.update {
                        it.copy(
                            qrScannerState = QRScannerState.ERROR,
                            isLoading = false,
                            errorMessage = errorMessage
                        )
                    }
                } else {
                    _attendanceState.update {
                        it.copy(
                            codeEntryState = CodeEntryState.ERROR,
                            isLoading = false,
                            codeEntryErrorMessage = errorMessage
                        )
                    }
                }
                _event.send(AttendanceEvent.ShowErrorMessage(errorMessage))
            }
        }
    }

    fun markAttendanceDirectlyFromQR(sessionCode: String, unitCode: String) {

        // Proceed with marking attendance
        var shouldMark = false
        _attendanceState.update {
            if (it.qrScannerState == QRScannerState.VERIFIED) {
                shouldMark = true
                it.copy(
                    qrScannerState = QRScannerState.MARKING_ATTENDANCE,
                    isLoading = true
                )
            } else {
                it
            }
        }

        if (!shouldMark) return

        viewModelScope.launch {
            val request = createMarkAttendanceRequest(sessionCode, unitCode)
            markAttendanceFromQR(request)
        }

    }

    fun markAttendanceDirectlyFromCode(sessionCode: String, unitCode: String) {
        var shouldMark = false
        _attendanceState.update {
            if (it.codeEntryState == CodeEntryState.VERIFIED) {
                shouldMark = true
                it.copy(
                    codeEntryState = CodeEntryState.MARKING_ATTENDANCE,
                    isLoading = true
                )
            } else {
                it
            }
        }

        if (!shouldMark) return

        viewModelScope.launch {
            val request = createMarkAttendanceRequest(sessionCode, unitCode)
            markAttendanceFromCode(request)
        }
    }

    private fun markAttendanceFromQR(request: MarkAttendanceRequest) {
        viewModelScope.launch {
            try {
                when (val result = attendanceRepository.markAttendance(request)) {
                    is ApiResult.Success -> {
                        _attendanceState.update {
                            it.copy(
                                qrScannerState = QRScannerState.IDLE,
                                isLoading = false,
                                attendanceResult = result.data,
                                showSuccess = true,
                                showQRScanner = false,
                                showProgrammeSelection = false
                            )
                        }
                        vibrationHelper.vibrate(200)
                    }
                    is ApiResult.Failure -> {
                        _attendanceState.update {
                            it.copy(
                                qrScannerState = QRScannerState.ERROR,
                                isLoading = false,
                                errorMessage = extractApiErrorMessage(result.error)
                            )
                        }
                        _event.send(AttendanceEvent.ShowErrorMessage("Attendance marking failed"))
                    }
                }
            } catch (e: Exception) {
                _attendanceState.update {
                    it.copy(
                        qrScannerState = QRScannerState.ERROR,
                        isLoading = false,
                        errorMessage = "Network error: ${e.message}"
                    )
                }
                _event.send(AttendanceEvent.ShowErrorMessage("Network error occurred"))
            }
        }
    }

    private fun markAttendanceFromCode(request: MarkAttendanceRequest) {
        viewModelScope.launch {
            try {
                when (val result = attendanceRepository.markAttendance(request)) {
                    is ApiResult.Success -> {
                        _attendanceState.update {
                            it.copy(
                                codeEntryState = CodeEntryState.IDLE,
                                isLoading = false,
                                attendanceResult = result.data,
                                showSuccess = true,
                                showCodeEntry = false,
                                showProgrammeSelection = false
                            )
                        }
                        vibrationHelper.vibrate(200)
                    }
                    is ApiResult.Failure -> {
                        _attendanceState.update {
                            it.copy(
                                codeEntryState = CodeEntryState.ERROR,
                                isLoading = false,
                                codeEntryErrorMessage = extractApiErrorMessage(result.error)
                            )
                        }
                        _event.send(AttendanceEvent.ShowErrorMessage("Attendance marking failed"))
                    }
                }
            } catch (e: Exception) {
                _attendanceState.update {
                    it.copy(
                        codeEntryState = CodeEntryState.ERROR,
                        isLoading = false,
                        codeEntryErrorMessage = "Network error: ${e.message}"
                    )
                }
                _event.send(AttendanceEvent.ShowErrorMessage("Network error occurred"))
            }
        }
    }

    private fun markAttendanceWithProgramme(programmeId: String) {
        val state = _attendanceState.value
        val isQrFlow = state.showQRScanner
        val isCodeFlow = state.showCodeEntry

        if (!isQrFlow && !isCodeFlow) return

        // Update state for marking
        _attendanceState.update {
            it.copy(
                isLoading = true,
                showProgrammeSelection = false,
                qrScannerState = if (isQrFlow) QRScannerState.MARKING_ATTENDANCE else it.qrScannerState,
                codeEntryState = if (isCodeFlow) CodeEntryState.MARKING_ATTENDANCE else it.codeEntryState
            )
        }

        viewModelScope.launch {
            state.currentSessionCode?.let { sessionCode ->
                state.currentUnitCode?.let { unitCode ->
                    val request = createMarkAttendanceRequest(sessionCode, unitCode, programmeId)

                    // Determine which flow we're in
                    if (isQrFlow) {
                        markAttendanceFromQR(request)
                    } else {
                        markAttendanceFromCode(request)
                    }
                }
            }
        }
    }

    private fun markAttendance(request: MarkAttendanceRequest) {
        viewModelScope.launch {
            try {
                when (val result = attendanceRepository.markAttendance(request)) {
                    is ApiResult.Success -> {
                        _attendanceState.update {
                            it.copy(
                                qrScannerState = QRScannerState.IDLE,
                                isLoading = false,
                                attendanceResult = result.data,
                                showSuccess = true,
                                showQRScanner = false
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        _attendanceState.update {
                            it.copy(
                                qrScannerState = QRScannerState.ERROR,
                                isLoading = false,
                                errorMessage = extractApiErrorMessage(result.error)
                            )
                        }
                        _event.send(AttendanceEvent.ShowErrorMessage("Attendance marking failed"))
                    }
                }
            } catch (e: Exception) {
                _attendanceState.update {
                    it.copy(
                        qrScannerState = QRScannerState.ERROR,
                        isLoading = false,
                        errorMessage = "Network error: ${e.message}"
                    )
                }
                _event.send(AttendanceEvent.ShowErrorMessage("Network error occurred"))
            }
        }
    }

    private suspend fun createMarkAttendanceRequest(
        sessionCode: String,
        unitCode: String,
        programmeId: String? = null
    ): MarkAttendanceRequest {
        return MarkAttendanceRequest(
            sessionCode = sessionCode,
            unitCode = unitCode,
            deviceId = session.getDeviceId() ?: deviceInfoProvider.getDeviceInfo().deviceId,
            programmeId = programmeId,
            studentLat = _attendanceState.value.studentLocation?.latitude,
            studentLng = _attendanceState.value.studentLocation?.longitude,
            methodUsed = if (_attendanceState.value.showQRScanner)
                AttendanceMethod.QR_CODE
            else
                AttendanceMethod.MANUAL_CODE
        )
    }

    private fun extractApiErrorMessage(error: ApiError) : String {
        return when (error) {
            is ApiError.NetworkError -> {
                "Network error: ${error.exception.message ?: "Unknown network error" }"
            }
            is ApiError.HttpError -> {
                "HTTP error: ${error.statusCode} - ${error.message}"
            }
            is ApiError.UnknownError -> {
                "Unknown error: ${error.throwable.message ?: "Unknown error"}"
            }

        }
    }


    fun resetQRScanner() {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.IDLE,
                scannedQRData = null,
                errorMessage = null
            )
        }
    }

    fun resetCodeEntry() {
        _attendanceState.update {
            it.copy(
                codeEntryState = CodeEntryState.IDLE,
                codeEntryErrorMessage = null,
                sessionCode = "",
                unitCode = ""
            )
        }
    }

    fun isLocationEnabled(): Boolean {
        return locationService.isLocationEnabled()
    }

    fun promptEnableGPS(
        activity: Activity,
        enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        locationService.promptEnableGPS(activity, enableGpsLauncher)
    }

    private fun requestLocation() {
        _attendanceState.update {
            it.copy(
                locationState = LocationState.CAPTURING,
                locationError = null
            )
        }

        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                onEvent(AttendanceUiEvent.LocationCaptured(location))
            } catch (e: Exception) {
                onEvent(AttendanceUiEvent.LocationError(e.message ?: "Failed to get location"))
            }
        }
    }

}

 */

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val locationService: LocationService,
    private val session: ClassTrackSession,
    context: Context
) : ViewModel()
{

    private val _attendanceState = MutableStateFlow(StudentAttendanceState())
    val attendanceState = _attendanceState.asStateFlow()

    private val _event = Channel<AttendanceEvent>()
    val event = _event.receiveAsFlow()

    private val vibrationHelper = VibrationHelper(context)

    fun onEvent(event: AttendanceUiEvent) {
        when (event) {
            is AttendanceUiEvent.UpdateSessionCode -> {
                val sanitizedCode = event.sessionCode.filter { it.isDigit() }.take(6)
                _attendanceState.update {
                    it.copy(
                        sessionCode = sanitizedCode,
                        codeEntryErrorMessage = null,
                        errorType = null
                    )
                }
            }
            is AttendanceUiEvent.UpdateUnitCode -> {
                _attendanceState.update {
                    it.copy(
                        unitCode = event.unitCode.take(8),
                        codeEntryErrorMessage = null
                    )
                }
            }
            is AttendanceUiEvent.VerifySession -> {
                verifySessionFromCode(event.sessionCode, event.unitCode)
            }

            is AttendanceUiEvent.RetryVerifySession -> {
                retryVerifySession()
            }

            is AttendanceUiEvent.RetryMarkAttendance -> {
                retryMarkAttendance()
            }

            is AttendanceUiEvent.ProgrammeSelected -> {
                markAttendanceWithProgramme(event.programmeId)
            }

            is AttendanceUiEvent.QRCodeScanned -> {
                processQRCode(event.qrData)
            }
            AttendanceUiEvent.ResetState -> {
                _attendanceState.update { StudentAttendanceState() }
            }

            AttendanceUiEvent.RequestLocation -> {
                requestLocation()
            }

            is AttendanceUiEvent.LocationCaptured -> {
                _attendanceState.update {
                    it.copy(
                        locationState = LocationState.CAPTURED,
                        studentLocation = event.location,
                        locationError = null
                    )
                }
            }

            AttendanceUiEvent.RetryLocationCapture -> {
                requestLocation()
            }

            is AttendanceUiEvent.LocationError -> {
                _attendanceState.update {
                    it.copy(
                        locationState = LocationState.ERROR,
                        locationError = event.error
                    )
                }
            }

            is AttendanceUiEvent.MarkAttendanceVerifiedSession -> {
                markAttendanceVerifiedSession()
            }

            is AttendanceUiEvent.ContinueWithCapturedLocation -> {
                val state = _attendanceState.value
                val verificationResult = state.verificationResult

                if (verificationResult?.requiresProgrammeSelection == true) {
                    _attendanceState.update {
                        it.copy(
                            showLocationCapture = false,
                            showProgrammeSelection = true
                        )
                    }
                } else {
                    markAttendanceVerifiedSession()
                }
            }

            is AttendanceUiEvent.CancelLocationCapture -> {
                _attendanceState.update {
                    it.copy(showLocationCapture = false)
                }
                if (_attendanceState.value.showQRScanner) {
                    resetQRScanner()
                } else {
                    resetCodeEntry()
                }
            }
        }
    }

    fun showQrScanner() {
        _attendanceState.update {
            it.copy(
                showQRScanner = true,
                showCodeEntry = false,
                qrScannerState = QRScannerState.IDLE,
                scannedQRData = null,
                errorMessage = null,
                errorType = null
            )
        }
    }

    fun showCodeEntry() {
        _attendanceState.update {
            it.copy(
                showCodeEntry = true,
                showQRScanner = false,
                codeEntryState = CodeEntryState.IDLE,
                codeEntryErrorMessage = null,
                errorType = null
            )
        }
    }

    private fun validateForm(): String? {
        val state = _attendanceState.value
        return when {
            state.sessionCode.isBlank() -> "Session code is required"
            state.sessionCode.length != 6 -> "Session code must be exactly 6 digits"
            !state.sessionCode.all { it.isDigit() } -> "Session code must contain only numbers"
            state.unitCode.isBlank() -> "Unit code is required"
            state.unitCode.length > 8 -> "Unit code cannot exceed 8 characters"
            else -> null
        }
    }

    private fun processQRCode(qrData: String) {
        var shouldProcess = false
        _attendanceState.update { currentState ->
            if (currentState.qrScannerState == QRScannerState.IDLE ||
                currentState.qrScannerState == QRScannerState.SCANNING) {
                shouldProcess = true
                currentState.copy(
                    qrScannerState = QRScannerState.SCANNED,
                    scannedQRData = qrData
                )
            } else {
                currentState
            }
        }

        if (!shouldProcess) return

        val qrCodeData = QRCodeData.fromJson(qrData)
        if (qrCodeData == null || !QRCodeData.isValid(qrCodeData)) {
            _attendanceState.update {
                it.copy(
                    qrScannerState = QRScannerState.ERROR,
                    errorMessage = "Invalid QR code format"
                )
            }
            _event.trySend(AttendanceEvent.ShowErrorMessage("Invalid QR code"))
            return
        }

        verifySessionFromQR(qrCodeData.sessionCode, qrCodeData.unitCode)
    }

    private fun verifySessionFromQR(sessionCode: String, unitCode: String) {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.VERIFYING_SESSION,
                isLoading = true,
                errorMessage = null,
                currentSessionCode = sessionCode,
                currentUnitCode = unitCode
            )
        }
        verifySessionInternal(sessionCode, unitCode, isFromQR = true)
    }

    private fun verifySessionFromCode(sessionCode: String, unitCode: String) {
        val message = validateForm()
        if (message != null) {
            _event.trySend(AttendanceEvent.ShowErrorMessage(message))
            return
        }
        _attendanceState.update {
            it.copy(
                codeEntryState = CodeEntryState.VERIFYING_SESSION,
                isLoading = true,
                codeEntryErrorMessage = null,
                currentSessionCode = sessionCode,
                currentUnitCode = unitCode
            )
        }
        verifySessionInternal(sessionCode, unitCode, isFromQR = false)
    }

    // Consolidated function for retrying verification
    fun retryVerifySession() {
        _attendanceState.update { it.copy(errorType = null) }
        val state = _attendanceState.value
        val sessionCode = state.currentSessionCode
        val unitCode = state.currentUnitCode

        if (sessionCode == null || unitCode == null) return

        if (state.showQRScanner) {
            verifySessionFromQR(sessionCode, unitCode)
        } else {
            verifySessionFromCode(sessionCode, unitCode)
        }
    }

    private fun verifySessionInternal(sessionCode: String, unitCode: String, isFromQR: Boolean) {
        viewModelScope.launch {
            try {
                val result = attendanceRepository.verifyAttendanceSession(
                    VerifySessionRequest(sessionCode, unitCode)
                )

                when (result) {
                    is ApiResult.Success -> {
                        val requiresProgrammeSelection = result.data.requiresProgrammeSelection
                        val requiresLocation = result.data.requiresLocation

                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.VERIFIED,
                                    isLoading = false,
                                    verificationResult = result.data,
                                    showProgrammeSelection = false,
                                    showLocationCapture = false
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.VERIFIED,
                                    isLoading = false,
                                    verificationResult = result.data,
                                    showProgrammeSelection = false,
                                    showLocationCapture = false
                                )
                            }
                        }

                        vibrationHelper.vibrate(100)

                        if (requiresLocation && _attendanceState.value.studentLocation == null) {
                            _attendanceState.update {
                                it.copy(showLocationCapture = true)
                            }
                        } else if (requiresProgrammeSelection) {
                            _attendanceState.update {
                                it.copy(showProgrammeSelection = true)
                            }
                        } else {
                            markAttendanceVerifiedSession()
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = extractApiErrorMessage(result.error)
                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.ERROR,
                                    isLoading = false,
                                    errorMessage = errorMessage,
                                    errorType = AttendanceErrorType.VERIFICATION_ERROR
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.ERROR,
                                    isLoading = false,
                                    codeEntryErrorMessage = errorMessage,
                                    errorType = AttendanceErrorType.VERIFICATION_ERROR
                                )
                            }
                        }
                        _event.send(AttendanceEvent.ShowErrorMessage(errorMessage))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred"
                if (isFromQR) {
                    _attendanceState.update {
                        it.copy(
                            qrScannerState = QRScannerState.ERROR,
                            isLoading = false,
                            errorMessage = errorMessage,
                            errorType = AttendanceErrorType.VERIFICATION_ERROR
                        )
                    }
                } else {
                    _attendanceState.update {
                        it.copy(
                            codeEntryState = CodeEntryState.ERROR,
                            isLoading = false,
                            codeEntryErrorMessage = errorMessage,
                            errorType = AttendanceErrorType.VERIFICATION_ERROR
                        )
                    }
                }
                _event.send(AttendanceEvent.ShowErrorMessage(errorMessage))
            }
        }
    }

    // Consolidated function to mark attendance
    private fun markAttendanceVerifiedSession() {
        val state = _attendanceState.value
        val isFromQR = state.showQRScanner
        val sessionCode = state.currentSessionCode
        val unitCode = state.currentUnitCode

        if (sessionCode == null || unitCode == null) return

        markAttendanceFromFlow(sessionCode, unitCode, isFromQR = isFromQR)
    }

    // Single consolidated function for marking attendance
    private fun markAttendanceFromFlow(
        sessionCode: String,
        unitCode: String,
        isFromQR: Boolean,
        programmeId: String? = null
    ) {
        // Set appropriate state
        if (isFromQR) {
            _attendanceState.update {
                if (it.qrScannerState == QRScannerState.VERIFIED ||
                    it.qrScannerState == QRScannerState.ERROR) {
                    it.copy(
                        qrScannerState = QRScannerState.MARKING_ATTENDANCE,
                        isLoading = true,
                        errorMessage = null,
                        showProgrammeSelection = false
                    )
                } else {
                    it
                }
            }
        } else {
            _attendanceState.update {
                if (it.codeEntryState == CodeEntryState.VERIFIED ||
                    it.codeEntryState == CodeEntryState.ERROR) {
                    it.copy(
                        codeEntryState = CodeEntryState.MARKING_ATTENDANCE,
                        isLoading = true,
                        codeEntryErrorMessage = null,
                        showProgrammeSelection = false
                    )
                } else {
                    it
                }
            }
        }

        viewModelScope.launch {
            try {
                val request = createMarkAttendanceRequest(sessionCode, unitCode, programmeId)
                when (val result = attendanceRepository.markAttendance(request)) {
                    is ApiResult.Success -> {
                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.IDLE,
                                    isLoading = false,
                                    attendanceResult = result.data,
                                    showSuccess = true,
                                    showQRScanner = false,
                                    showProgrammeSelection = false,
                                    verificationResult = null,
                                    studentLocation = null,
                                    locationState = LocationState.IDLE
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.IDLE,
                                    isLoading = false,
                                    attendanceResult = result.data,
                                    showSuccess = true,
                                    showCodeEntry = false,
                                    showProgrammeSelection = false,
                                    verificationResult = null,
                                    studentLocation = null,
                                    locationState = LocationState.IDLE
                                )
                            }
                        }
                        vibrationHelper.vibrate(200)
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = extractApiErrorMessage(result.error)
                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.ERROR,
                                    isLoading = false,
                                    errorMessage = errorMessage,
                                    errorType = AttendanceErrorType.MARKING_ERROR
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.ERROR,
                                    isLoading = false,
                                    codeEntryErrorMessage = errorMessage,
                                    errorType = AttendanceErrorType.MARKING_ERROR
                                )
                            }
                        }
                        _event.send(AttendanceEvent.ShowErrorMessage("Attendance marking failed: $errorMessage"))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Network error occurred"
                if (isFromQR) {
                    _attendanceState.update {
                        it.copy(
                            qrScannerState = QRScannerState.ERROR,
                            isLoading = false,
                            errorMessage = errorMessage,
                            errorType = AttendanceErrorType.MARKING_ERROR
                        )
                    }
                } else {
                    _attendanceState.update {
                        it.copy(
                            codeEntryState = CodeEntryState.ERROR,
                            isLoading = false,
                            codeEntryErrorMessage = errorMessage,
                            errorType = AttendanceErrorType.MARKING_ERROR
                        )
                    }
                }
                _event.send(AttendanceEvent.ShowErrorMessage(errorMessage))
            }
        }
    }

    // Consolidated function for retrying mark attendance
    fun retryMarkAttendance() {
        _attendanceState.update { it.copy(errorType = null) }
        val state = _attendanceState.value
        val sessionCode = state.currentSessionCode
        val unitCode = state.currentUnitCode

        if (sessionCode == null || unitCode == null) return

        markAttendanceFromFlow(sessionCode, unitCode, state.showQRScanner)
    }

    // Mark attendance with programme (uses the same consolidated function)
    private fun markAttendanceWithProgramme(programmeId: String) {
        val state = _attendanceState.value
        val isFromQR = state.showQRScanner
        val sessionCode = state.currentSessionCode
        val unitCode = state.currentUnitCode

        if (!isFromQR && !state.showCodeEntry) return
        if (sessionCode == null || unitCode == null) return

        // Use the consolidated marking function
        markAttendanceFromFlow(sessionCode, unitCode, isFromQR, programmeId)
    }

    private suspend fun createMarkAttendanceRequest(
        sessionCode: String,
        unitCode: String,
        programmeId: String? = null
    ): MarkAttendanceRequest {
        return MarkAttendanceRequest(
            sessionCode = sessionCode,
            unitCode = unitCode,
            deviceId = session.getDeviceId() ?: deviceInfoProvider.getDeviceInfo().deviceId,
            programmeId = programmeId,
            studentLat = _attendanceState.value.studentLocation?.latitude,
            studentLng = _attendanceState.value.studentLocation?.longitude,
            methodUsed = if (_attendanceState.value.showQRScanner)
                AttendanceMethod.QR_CODE
            else
                AttendanceMethod.MANUAL_CODE
        )
    }

    private fun extractApiErrorMessage(error: ApiError): String {
        return when (error) {
            is ApiError.NetworkError -> {
                "Network error: ${error.exception.message ?: "Please check your internet connection"}"
            }
            is ApiError.HttpError -> {
               "Error ${error.statusCode}: ${error.message}"
            }
            is ApiError.UnknownError -> {
                "An unexpected error occurred: ${error.throwable.message ?: "Please try again"}"
            }
        }
    }

    fun resetQRScanner() {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.IDLE,
                scannedQRData = null,
                errorMessage = null
            )
        }
    }

    fun resetCodeEntry() {
        _attendanceState.update {
            it.copy(
                codeEntryState = CodeEntryState.IDLE,
                codeEntryErrorMessage = null,
                sessionCode = "",
                unitCode = ""
            )
        }
    }

    fun isLocationEnabled(): Boolean {
        return locationService.isLocationEnabled()
    }

    fun promptEnableGPS(
        activity: Activity,
        enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        locationService.promptEnableGPS(activity, enableGpsLauncher)
    }

    private fun requestLocation() {
        _attendanceState.update {
            it.copy(
                locationState = LocationState.CAPTURING,
                locationError = null
            )
        }

        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                onEvent(AttendanceUiEvent.LocationCaptured(location))
            } catch (e: Exception) {
                onEvent(AttendanceUiEvent.LocationError(e.message ?: "Failed to get location"))
            }
        }
    }
}
