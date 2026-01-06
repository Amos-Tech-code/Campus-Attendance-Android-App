package com.amos_tech_code.smartattend.ui.feature.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AttendanceRepository
import com.amos_tech_code.smartattend.domain.models.QRCodeData
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import com.amos_tech_code.smartattend.utils.VibrationHelper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val session: SmartAttendSession,
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
            is AttendanceUiEvent.UpdateSecretKey -> {
                _attendanceState.update {
                    it.copy(
                        secretKey = event.secretKey.take(8),
                        codeEntryErrorMessage = null
                    )
                }
            }
            is AttendanceUiEvent.VerifySession -> {
                verifySession(event.sessionCode, event.secretKey)
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
            is AttendanceUiEvent.UpdateLocation -> {
                _attendanceState.update { it.copy(studentLocation = event.location) }
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
                sessionCode = "",
                secretKey = "",
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
        verifySessionFromQR(qrCodeData.sessionCode, qrCodeData.secretKey)
    }

    private fun verifySessionFromQR(sessionCode: String, secretKey: String) {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.VERIFYING_SESSION,
                isLoading = true,
                errorMessage = null,
                currentSessionCode = sessionCode,
                currentSecretKey = secretKey
            )
        }

        verifySessionInternal(sessionCode, secretKey, isFromQR = true)
    }

    private fun verifySession(sessionCode: String, secretKey: String) {
        _attendanceState.update {
            it.copy(
                codeEntryState = CodeEntryState.VERIFYING_SESSION,
                isLoading = true,
                codeEntryErrorMessage = null,
                currentSessionCode = sessionCode,
                currentSecretKey = secretKey
            )
        }

        verifySessionInternal(sessionCode, secretKey, isFromQR = false)
    }

    private fun verifySessionInternal(sessionCode: String, secretKey: String, isFromQR: Boolean) {
        viewModelScope.launch {
            try {
                val result = attendanceRepository.verifyAttendanceSession(
                    VerifySessionRequest(sessionCode, secretKey)
                )

                when (result) {
                    is ApiResult.Success -> {
                        val requiresProgrammeSelection = result.data.requiresProgrammeSelection
                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.VERIFIED,
                                    isLoading = false,
                                    verificationResult = result.data,
                                    showProgrammeSelection = requiresProgrammeSelection
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.VERIFIED,
                                    isLoading = false,
                                    verificationResult = result.data,
                                    showProgrammeSelection = requiresProgrammeSelection
                                )
                            }
                        }

                        // Vibrate on successful verification
                        vibrationHelper.vibrate(100)

                        // If no programme selection needed, proceed to mark attendance
                        if (!requiresProgrammeSelection) {
                            if (isFromQR) {
                                markAttendanceDirectlyFromQR(sessionCode, secretKey)
                            } else {
                                markAttendanceDirectlyFromCode(sessionCode, secretKey)
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        if (isFromQR) {
                            _attendanceState.update {
                                it.copy(
                                    qrScannerState = QRScannerState.ERROR,
                                    isLoading = false,
                                    errorMessage = extractApiErrorMessage(result.error)
                                )
                            }
                        } else {
                            _attendanceState.update {
                                it.copy(
                                    codeEntryState = CodeEntryState.ERROR,
                                    isLoading = false,
                                    codeEntryErrorMessage = extractApiErrorMessage(result.error)
                                )
                            }
                        }
                        _event.send(AttendanceEvent.ShowErrorMessage("Session verification failed"))
                    }
                }
            } catch (e: Exception) {
                if (isFromQR) {
                    _attendanceState.update {
                        it.copy(
                            qrScannerState = QRScannerState.ERROR,
                            isLoading = false,
                            errorMessage = "Network error: ${e.message}"
                        )
                    }
                } else {
                    _attendanceState.update {
                        it.copy(
                            codeEntryState = CodeEntryState.ERROR,
                            isLoading = false,
                            codeEntryErrorMessage = "Network error: ${e.message}"
                        )
                    }
                }
                _event.send(AttendanceEvent.ShowErrorMessage("Network error occurred"))
            }
        }
    }

    private fun markAttendanceDirectlyFromQR(sessionCode: String, secretKey: String) {
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
            val request = MarkAttendanceRequest(
                sessionCode = sessionCode,
                secretKey = secretKey,
                deviceId = session.getDeviceId() ?: deviceInfoProvider.getDeviceInfo().deviceId,
                studentLat = _attendanceState.value.studentLocation?.latitude,
                studentLng = _attendanceState.value.studentLocation?.longitude
            )

            markAttendanceFromQR(request)
        }
    }

    private fun markAttendanceDirectlyFromCode(sessionCode: String, secretKey: String) {
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
            val request = MarkAttendanceRequest(
                sessionCode = sessionCode,
                secretKey = secretKey,
                deviceId = session.getDeviceId() ?: deviceInfoProvider.getDeviceInfo().deviceId,
                studentLat = _attendanceState.value.studentLocation?.latitude,
                studentLng = _attendanceState.value.studentLocation?.longitude
            )

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
                secretKey = ""
            )
        }
    }

    private fun markAttendanceWithProgramme(programmeId: String) {
        val state = _attendanceState.value

        val isQrFlow = state.qrScannerState == QRScannerState.VERIFIED
        val isCodeFlow = state.codeEntryState == CodeEntryState.VERIFIED

        if (!isQrFlow && !isCodeFlow) return // Not in a state to mark attendance with programme

        _attendanceState.update {
            it.copy(
                isLoading = true,
                showProgrammeSelection = false, // Dismiss dialog
                qrScannerState = if (isQrFlow) QRScannerState.MARKING_ATTENDANCE else it.qrScannerState,
                codeEntryState = if (isCodeFlow) CodeEntryState.MARKING_ATTENDANCE else it.codeEntryState
            )
        }

        viewModelScope.launch {
            state.currentSessionCode?.let { sessionCode ->
                state.currentSecretKey?.let { secretKey ->
                    val request = MarkAttendanceRequest(
                        sessionCode = sessionCode,
                        secretKey = secretKey,
                        deviceId = session.getDeviceId() ?: deviceInfoProvider.getDeviceInfo().deviceId,
                        programmeId = programmeId,
                        studentLat = state.studentLocation?.latitude,
                        studentLng = state.studentLocation?.longitude
                    )

                    // Determine which flow we're in and mark accordingly
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
}
