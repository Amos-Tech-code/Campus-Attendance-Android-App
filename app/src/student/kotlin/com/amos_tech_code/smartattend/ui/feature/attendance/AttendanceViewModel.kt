package com.amos_tech_code.smartattend.ui.feature.attendance

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repository.AttendanceSessionRepository
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.QRCodeData
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.services.LocationService
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import com.amos_tech_code.smartattend.utils.VibrationHelper
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val attendanceRepository: AttendanceSessionRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val locationService: LocationService,
    private val session: ClassTrackSession,
    private val contentResolver: ContentResolver,
    context: Context
) : ViewModel() {
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

            AttendanceUiEvent.ImagePickerError -> {
                _event.trySend(AttendanceEvent.ShowErrorMessage("Failed to pick image"))
            }

            AttendanceUiEvent.PickImageFromGallery -> {
                _attendanceState.update {
                    it.copy(
                        currentScreen = AttendanceScreen.PhotoPicker.Selecting,
                        selectedImageUri = null,
                        errorMessage = null,
                        errorType = null
                    )
                }
            }

            AttendanceUiEvent.PhotoPickerProcessing -> {
                _attendanceState.update {
                    it.copy(
                        currentScreen = AttendanceScreen.PhotoPicker.Processing
                    )
                }
            }

            is AttendanceUiEvent.PhotoPickerError -> {
                _attendanceState.update {
                    it.copy(
                        currentScreen = AttendanceScreen.PhotoPicker.Error(event.message),
                        errorMessage = event.message
                    )
                }
            }

            AttendanceUiEvent.RetryPhotoPicker -> {
                _attendanceState.update {
                    it.copy(
                        currentScreen = AttendanceScreen.PhotoPicker.Selecting,
                        selectedImageUri = null,
                        errorMessage = null,
                        errorType = null
                    )
                }
            }

            is AttendanceUiEvent.ImageSelected -> {
                _attendanceState.update {
                    it.copy(selectedImageUri = event.uri)
                }
                extractQRCodeFromImage(event.uri)
            }

            is AttendanceUiEvent.VerifySession -> {
                verifySessionFromCode(event.sessionCode, event.unitCode)
            }

            is AttendanceUiEvent.RetryVerifySession -> {
                retryVerifySession(event.sessionCode, event.unitCode)
            }

            is AttendanceUiEvent.RetryMarkAttendance -> {
                retryMarkAttendance()
            }

            is AttendanceUiEvent.ProgrammeSelected -> {
                _attendanceState.update { it.copy(showProgrammeSelection = false) }
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
                            showProgrammeSelection = true,
                        )
                    }
                } else {
                    markAttendanceVerifiedSession()
                }
            }

            is AttendanceUiEvent.CancelLocationCapture -> {
                _attendanceState.update {
                    it.copy(
                        currentScreen = when (val currentScreen = it.currentScreen) {
                            is AttendanceScreen.QRScanner -> AttendanceScreen.QRScanner
                            is AttendanceScreen.CodeEntry -> AttendanceScreen.CodeEntry
                            is AttendanceScreen.PhotoPicker -> AttendanceScreen.PhotoPicker.Selecting
                            else -> AttendanceScreen.Main
                        },
                        showLocationCapture = false
                    )
                }
            }
        }
    }

    fun showQrScanner() {
        _attendanceState.update {
            it.copy(
                currentScreen = AttendanceScreen.QRScanner,
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
                currentScreen = AttendanceScreen.CodeEntry,
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

    private fun extractQRCodeFromImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    onEvent(AttendanceUiEvent.PhotoPickerError("Failed to decode image"))
                    return@launch
                }

                val image = InputImage.fromBitmap(bitmap, 0)
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                )

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            barcodes.firstOrNull()?.rawValue?.let { qrData ->
                                _attendanceState.update {
                                    it.copy(
                                        currentScreen = AttendanceScreen.PhotoPicker.Success(qrData)
                                    )
                                }
                            } ?: run {
                                onEvent(AttendanceUiEvent.PhotoPickerError("No QR code found in image"))
                            }
                        } else {
                            onEvent(AttendanceUiEvent.PhotoPickerError("No QR code found in image"))
                        }
                    }
                    .addOnFailureListener { e ->
                        onEvent(AttendanceUiEvent.PhotoPickerError("Failed to process image: ${e.message}"))
                    }
            } catch (e: Exception) {
                onEvent(AttendanceUiEvent.PhotoPickerError("Error processing image: ${e.message}"))
            }
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

    fun verifySessionFromPhotoPicker(sessionCode: String, unitCode: String) {
        _attendanceState.update {
            it.copy(
                currentScreen = AttendanceScreen.PhotoPicker.VerifyingSession,
                isLoading = true,
                errorMessage = null,
                currentSessionCode = sessionCode,
                currentUnitCode = unitCode
            )
        }
        verifySessionInternal(sessionCode, unitCode, isFromPhotoPicker = true)
    }

    fun retryVerifySession(sessionCode: String, unitCode: String) {
        _attendanceState.update { it.copy(errorType = null) }

        when (val currentScreen = _attendanceState.value.currentScreen) {
            is AttendanceScreen.QRScanner -> verifySessionFromQR(sessionCode, unitCode)
            is AttendanceScreen.CodeEntry -> verifySessionFromCode(sessionCode, unitCode)
            is AttendanceScreen.PhotoPicker -> verifySessionFromPhotoPicker(sessionCode, unitCode)
            else -> {}
        }
    }

    private fun verifySessionInternal(
        sessionCode: String,
        unitCode: String,
        isFromQR: Boolean = false,
        isFromPhotoPicker: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val result = attendanceRepository.verifyAttendanceSession(
                    VerifySessionRequest(sessionCode, unitCode)
                )

                when (result) {
                    is ApiResult.Success -> {
                        val requiresProgrammeSelection = result.data.requiresProgrammeSelection
                        val requiresLocation = result.data.requiresLocation

                        when {
                            isFromQR -> {
                                _attendanceState.update {
                                    it.copy(
                                        qrScannerState = QRScannerState.VERIFIED,
                                        isLoading = false,
                                        verificationResult = result.data
                                    )
                                }
                            }
                            isFromPhotoPicker -> {
                                _attendanceState.update {
                                    it.copy(
                                        currentScreen = AttendanceScreen.PhotoPicker.Verified,
                                        isLoading = false,
                                        verificationResult = result.data
                                    )
                                }
                            }
                            else -> {
                                _attendanceState.update {
                                    it.copy(
                                        codeEntryState = CodeEntryState.VERIFIED,
                                        isLoading = false,
                                        verificationResult = result.data
                                    )
                                }
                            }
                        }

                        vibrationHelper.vibrate(100)

                        when {
                            requiresLocation && _attendanceState.value.studentLocation == null -> {
                                _attendanceState.update {
                                    it.copy(showLocationCapture = true)
                                }
                            }
                            requiresProgrammeSelection -> {
                                _attendanceState.update {
                                    it.copy(
                                        showProgrammeSelection = true
                                    )
                                }
                            }
                            else -> {
                                markAttendanceVerifiedSession()
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = extractApiErrorMessage(result.error)
                        handleVerificationError(errorMessage, isFromQR, isFromPhotoPicker)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred"
                handleVerificationError(errorMessage, isFromQR, isFromPhotoPicker)
            }
        }
    }

    private fun handleVerificationError(
        errorMessage: String,
        isFromQR: Boolean,
        isFromPhotoPicker: Boolean
    ) {
        when {
            isFromQR -> {
                _attendanceState.update {
                    it.copy(
                        qrScannerState = QRScannerState.ERROR,
                        isLoading = false,
                        errorMessage = errorMessage,
                        errorType = AttendanceErrorType.VERIFICATION_ERROR
                    )
                }
            }
            isFromPhotoPicker -> {
                _attendanceState.update {
                    it.copy(
                        currentScreen = AttendanceScreen.PhotoPicker.Error(errorMessage),
                        isLoading = false,
                        errorMessage = errorMessage,
                        errorType = AttendanceErrorType.VERIFICATION_ERROR
                    )
                }
            }
            else -> {
                _attendanceState.update {
                    it.copy(
                        codeEntryState = CodeEntryState.ERROR,
                        isLoading = false,
                        codeEntryErrorMessage = errorMessage,
                        errorType = AttendanceErrorType.VERIFICATION_ERROR
                    )
                }
            }
        }
        _event.trySend(AttendanceEvent.ShowErrorMessage(errorMessage))
    }

    private fun markAttendanceVerifiedSession() {
        val state = _attendanceState.value
        val sessionCode = state.currentSessionCode
        val unitCode = state.currentUnitCode

        if (sessionCode == null || unitCode == null) return

        val method = when (state.currentScreen) {
            is AttendanceScreen.QRScanner -> AttendanceMethod.QR_CODE
            is AttendanceScreen.CodeEntry -> AttendanceMethod.MANUAL_CODE
            is AttendanceScreen.PhotoPicker -> AttendanceMethod.QR_CODE // Photo picker also uses QR
            else -> AttendanceMethod.MANUAL_CODE
        }

        markAttendanceFromFlow(sessionCode, unitCode, method)
    }

    private fun markAttendanceFromFlow(
        sessionCode: String,
        unitCode: String,
        method: AttendanceMethod,
        programmeId: String? = null
    ) {
        // Set appropriate state based on current screen
        _attendanceState.update { currentState ->
            when (val screen = currentState.currentScreen) {
                is AttendanceScreen.QRScanner -> {
                    currentState.copy(
                        qrScannerState = QRScannerState.MARKING_ATTENDANCE,
                        isLoading = true,
                        errorMessage = null
                    )
                }
                is AttendanceScreen.CodeEntry -> {
                    currentState.copy(
                        codeEntryState = CodeEntryState.MARKING_ATTENDANCE,
                        isLoading = true,
                        codeEntryErrorMessage = null
                    )
                }
                is AttendanceScreen.PhotoPicker -> {
                    currentState.copy(
                        currentScreen = AttendanceScreen.PhotoPicker.MarkingAttendance,
                        isLoading = true,
                        errorMessage = null
                    )
                }
                else -> currentState
            }
        }

        viewModelScope.launch {
            try {
                val request = createMarkAttendanceRequest(sessionCode, unitCode, method, programmeId)
                when (val result = attendanceRepository.markAttendance(request)) {
                    is ApiResult.Success -> {
                        _attendanceState.update {
                            it.copy(
                                isLoading = false,
                                attendanceResult = result.data,
                                currentScreen = AttendanceScreen.Success(result.data),
                                verificationResult = null,
                                studentLocation = null,
                                locationState = LocationState.IDLE,
                                qrScannerState = QRScannerState.IDLE,
                                codeEntryState = CodeEntryState.IDLE
                            )
                        }
                        vibrationHelper.vibrate(200)
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = extractApiErrorMessage(result.error)
                        handleMarkingError(errorMessage, method)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Network error occurred"
                handleMarkingError(errorMessage, method)
            }
        }
    }

    private fun handleMarkingError(errorMessage: String, method: AttendanceMethod) {
        when (method) {
            AttendanceMethod.QR_CODE -> {
                if (_attendanceState.value.currentScreen is AttendanceScreen.QRScanner) {
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
                            currentScreen = AttendanceScreen.PhotoPicker.Error(errorMessage),
                            isLoading = false,
                            errorMessage = errorMessage,
                            errorType = AttendanceErrorType.MARKING_ERROR
                        )
                    }
                }
            }
            AttendanceMethod.MANUAL_CODE -> {
                _attendanceState.update {
                    it.copy(
                        codeEntryState = CodeEntryState.ERROR,
                        isLoading = false,
                        codeEntryErrorMessage = errorMessage,
                        errorType = AttendanceErrorType.MARKING_ERROR
                    )
                }
            }

            else -> { }
        }
        _event.trySend(AttendanceEvent.ShowErrorMessage("Attendance marking failed: $errorMessage"))
    }

    fun retryMarkAttendance() {
        _attendanceState.update { it.copy(errorType = null) }
        val state = _attendanceState.value
        val sessionCode = state.currentSessionCode
        val unitCode = state.currentUnitCode

        if (sessionCode == null || unitCode == null) return

        val method = when (state.currentScreen) {
            is AttendanceScreen.QRScanner -> AttendanceMethod.QR_CODE
            is AttendanceScreen.CodeEntry -> AttendanceMethod.MANUAL_CODE
            is AttendanceScreen.PhotoPicker -> AttendanceMethod.QR_CODE
            else -> AttendanceMethod.MANUAL_CODE
        }

        markAttendanceFromFlow(sessionCode, unitCode, method)
    }

    private fun markAttendanceWithProgramme(programmeId: String) {
        val state = _attendanceState.value
        val sessionCode = state.currentSessionCode
        val unitCode = state.currentUnitCode

        if (sessionCode == null || unitCode == null) return

        val method = when (state.currentScreen) {
            is AttendanceScreen.QRScanner -> AttendanceMethod.QR_CODE
            is AttendanceScreen.CodeEntry -> AttendanceMethod.MANUAL_CODE
            is AttendanceScreen.PhotoPicker -> AttendanceMethod.QR_CODE
            else -> AttendanceMethod.MANUAL_CODE
        }

        markAttendanceFromFlow(sessionCode, unitCode, method, programmeId)
    }

    private suspend fun createMarkAttendanceRequest(
        sessionCode: String,
        unitCode: String,
        method: AttendanceMethod,
        programmeId: String? = null
    ): MarkAttendanceRequest {
        return MarkAttendanceRequest(
            sessionCode = sessionCode,
            unitCode = unitCode,
            deviceId = session.getDeviceId() ?: deviceInfoProvider.getDeviceInfo().deviceId,
            programmeId = programmeId,
            studentLat = _attendanceState.value.studentLocation?.latitude,
            studentLng = _attendanceState.value.studentLocation?.longitude,
            methodUsed = method
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