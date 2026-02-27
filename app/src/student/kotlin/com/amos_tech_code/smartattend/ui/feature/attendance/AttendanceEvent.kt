package com.amos_tech_code.smartattend.ui.feature.attendance

import android.net.Uri
import com.amos_tech_code.smartattend.domain.models.LocationData

// UI Events
sealed class AttendanceUiEvent {
    data class VerifySession(val sessionCode: String, val unitCode: String) : AttendanceUiEvent()
    data class ProgrammeSelected(val programmeId: String) : AttendanceUiEvent()
    data class QRCodeScanned(val qrData: String) : AttendanceUiEvent()
    data class UpdateSessionCode(val sessionCode: String) : AttendanceUiEvent()
    data class UpdateUnitCode(val unitCode: String) : AttendanceUiEvent()
    data object PickImageFromGallery : AttendanceUiEvent()
    data class ImageSelected(val uri: Uri) : AttendanceUiEvent()
    data object ImagePickerError : AttendanceUiEvent()
    object ResetState : AttendanceUiEvent()
    object RequestLocation : AttendanceUiEvent()
    data class LocationCaptured(val location: LocationData) : AttendanceUiEvent()
    object RetryLocationCapture : AttendanceUiEvent()
    data class LocationError(val error: String) : AttendanceUiEvent()
    object MarkAttendanceVerifiedSession : AttendanceUiEvent()
    object ContinueWithCapturedLocation : AttendanceUiEvent()
    object CancelLocationCapture : AttendanceUiEvent()
    data class RetryVerifySession(val sessionCode: String, val unitCode: String) : AttendanceUiEvent()
    data object RetryMarkAttendance : AttendanceUiEvent()
}

// View Events
sealed class AttendanceEvent {
    data class ShowErrorMessage(val message: String) : AttendanceEvent()
}
