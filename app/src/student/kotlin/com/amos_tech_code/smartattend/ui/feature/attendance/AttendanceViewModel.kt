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
) : ViewModel() {

    private val _attendanceState = MutableStateFlow(StudentAttendanceState())
    val attendanceState = _attendanceState.asStateFlow()

    private val _event = Channel<AttendanceEvent>()
    val event = _event.receiveAsFlow()

    private val vibrationHelper = VibrationHelper(context)

    fun onEvent(event: AttendanceUiEvent) {
        when (event) {
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
                qrScannerState = QRScannerState.IDLE,
                errorMessage = null
            )
        }
    }

    fun showCodeEntry() {
        _attendanceState.update {
            it.copy(
                showCodeEntry = true,
                errorMessage = null
            )
        }
    }

    private fun processQRCode(qrData: String) {
        // Update state to show scanning completed
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.SCANNED,
                scannedQRData = qrData
            )
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
        verifySession(qrCodeData.sessionCode, qrCodeData.secretKey)
    }

    private fun verifySession(sessionCode: String, secretKey: String) {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.VERIFYING_SESSION,
                isLoading = true,
                errorMessage = null,
                currentSessionCode = sessionCode,
                currentSecretKey = secretKey
            )
        }

        viewModelScope.launch {
            try {
                val result = attendanceRepository.verifyAttendanceSession(
                    VerifySessionRequest(sessionCode, secretKey)
                )

                when (result) {
                    is ApiResult.Success -> {
                        _attendanceState.update {
                            it.copy(
                                qrScannerState = QRScannerState.VERIFIED,
                                isLoading = false,
                                verificationResult = result.data,
                                showProgrammeSelection = result.data.requiresProgrammeSelection
                            )
                        }

                        // Vibrate on successful scan
                        vibrationHelper.vibrate(50) // Short vibration for scan

                        // If no programme selection needed, proceed to mark attendance
                        if (!result.data.requiresProgrammeSelection) {
                            markAttendanceDirectly(sessionCode, secretKey)
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
                        _event.send(AttendanceEvent.ShowErrorMessage("Session verification failed"))
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

    private fun markAttendanceDirectly(sessionCode: String, secretKey: String) {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.MARKING_ATTENDANCE,
                isLoading = true
            )
        }

        viewModelScope.launch {
            val request = MarkAttendanceRequest(
                sessionCode = sessionCode,
                secretKey = secretKey,
                deviceId = session.getDeviceId() ?: deviceInfoProvider.getDeviceInfo().deviceId,
                studentLat = _attendanceState.value.studentLocation?.latitude,
                studentLng = _attendanceState.value.studentLocation?.longitude
            )

            markAttendance(request)
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
                        _event.send(AttendanceEvent.AttendanceMarkedSuccessfully(result.data))
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

    fun resetQRScanner() {
        _attendanceState.update {
            it.copy(
                qrScannerState = QRScannerState.IDLE,
                scannedQRData = null,
                errorMessage = null
            )
        }
    }

    private fun markAttendanceWithProgramme(programmeId: String) {
        viewModelScope.launch {
            val state = _attendanceState.value
            state.currentSessionCode?.let { sessionCode ->
                state.currentSecretKey?.let { secretKey ->
                    val request = MarkAttendanceRequest(
                        sessionCode = sessionCode,
                        secretKey = secretKey,
                        deviceId = session.getDeviceId()
                            ?: deviceInfoProvider.getDeviceInfo().deviceId,
                        programmeId = programmeId,
                        studentLat = state.studentLocation?.latitude,
                        studentLng = state.studentLocation?.longitude
                    )
                    markAttendance(request)
                }
            }
        }
    }

    private fun extractApiErrorMessage(error: ApiError) : String {
        return when (error) {
            is ApiError.NetworkError -> {
                "Network error: ${error.exception.message}"
            }
            is ApiError.HttpError -> {
                "HTTP error: ${error.statusCode} - ${error.message}"
            }
            is ApiError.UnknownError -> {
                "Unknown error: ${error.throwable.message}"
            }

        }
    }
}