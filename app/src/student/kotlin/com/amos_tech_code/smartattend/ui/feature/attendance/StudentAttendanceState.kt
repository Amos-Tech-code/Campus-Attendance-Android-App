package com.amos_tech_code.smartattend.ui.feature.attendance

import android.net.Uri
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse

// Attendance Screen State
data class StudentAttendanceState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorType: AttendanceErrorType? = null,

    // Current screen in the flow
    val currentScreen: AttendanceScreen = AttendanceScreen.Main,
    // Track the attendance method for the current session
    val currentAttendanceMethod: AttendanceMethod? = null,

    // Session Verification
    val verificationResult: VerifyAttendanceResponse? = null,
    val currentSessionCode: String? = null,
    val currentUnitCode: String? = null,

    // QR Scanner State
    val qrScannerState: QRScannerState = QRScannerState.IDLE,
    val scannedQRData: String? = null,

    // Code Entry State
    val codeEntryState: CodeEntryState = CodeEntryState.IDLE,
    val sessionCode: String = "",
    val unitCode: String = "",
    val codeEntryErrorMessage: String? = null,

    // Programme Selection
    val showProgrammeSelection: Boolean = false,

    // Location States
    val showLocationCapture: Boolean = false,
    val locationState: LocationState = LocationState.IDLE,
    val locationError: String? = null,
    // Location
    val studentLocation: LocationData? = null,

    // Photo picker specific state (optional, can be derived from currentScreen)
    val selectedImageUri: Uri? = null,

    // Attendance Result
    val attendanceResult: MarkAttendanceResponse? = null,
    val showSuccess: Boolean = false,
)

enum class QRScannerState {
    IDLE,              // Ready to scan
    SCANNING,          // Actively scanning for QR codes
    SCANNED,           // QR code successfully scanned
    VERIFYING_SESSION, // Verifying the session with server
    VERIFIED,          // Session verified successfully
    MARKING_ATTENDANCE, // Marking attendance
    ERROR              // Error occurred
}

enum class CodeEntryState {
    IDLE,              // Ready for input
    VERIFYING_SESSION, // Verifying session with server
    VERIFIED,          // Session verified successfully
    MARKING_ATTENDANCE, // Marking attendance
    ERROR              // Error occurred
}


enum class LocationState {
    IDLE,              // Initial state
    CAPTURING,         // Actively capturing location
    CAPTURED,          // Location successfully captured
    ERROR              // Error occurred
}

enum class AttendanceErrorType {
    VERIFICATION_ERROR,    // Session verification failed
    MARKING_ERROR,         // Attendance marking failed
}


sealed class AttendanceScreen {
    data object Main : AttendanceScreen()
    data object QRScanner : AttendanceScreen()
    data object CodeEntry : AttendanceScreen()

    // Photo picker with its own substates
    sealed class PhotoPicker : AttendanceScreen() {
        data object Selecting : PhotoPicker()  // User is selecting an image
        data object Processing : PhotoPicker() // Processing the selected image
        data class Error(val message: String) : PhotoPicker() // Error occurred
        data class Success(val qrData: String) : PhotoPicker() // QR code extracted successfully
        data object VerifyingSession : PhotoPicker() // Verifying the session
        data object Verified : PhotoPicker() // Session verified
        data object MarkingAttendance : PhotoPicker() // Marking attendance
    }

    data class Success(val result: MarkAttendanceResponse) : AttendanceScreen()
}
