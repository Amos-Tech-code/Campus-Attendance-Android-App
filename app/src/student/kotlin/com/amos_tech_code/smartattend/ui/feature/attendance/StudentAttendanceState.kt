package com.amos_tech_code.smartattend.ui.feature.attendance

import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import com.amos_tech_code.smartattend.ui.feature.home.RecentAttendance
import com.amos_tech_code.smartattend.ui.feature.home.Session

// Attendance Screen State
data class StudentAttendanceState(
    val activeSessions: List<Session> = emptyList(),
    val recentAttendance: List<RecentAttendance> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

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

    // Attendance Result
    val attendanceResult: MarkAttendanceResponse? = null,
    val showSuccess: Boolean = false,

    // Location
    val studentLocation: LocationData? = null,

    // Navigation states
    val showQRScanner: Boolean = false,
    val showCodeEntry: Boolean = false
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
    REQUESTING_PERMISSION, // Requesting location permissions
    CAPTURING,         // Actively capturing location
    CAPTURED,          // Location successfully captured
    ERROR              // Error occurred
}