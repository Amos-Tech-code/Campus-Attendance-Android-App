package com.amos_tech_code.smartattend.ui.feature.attendance

import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse

// UI Events
sealed class AttendanceUiEvent {
    data class VerifySession(val sessionCode: String, val unitCode: String) : AttendanceUiEvent()
    data class MarkAttendance(val request: MarkAttendanceRequest) : AttendanceUiEvent()
    data class ProgrammeSelected(val programmeId: String) : AttendanceUiEvent()
    data class QRCodeScanned(val qrData: String) : AttendanceUiEvent()
    data class UpdateSessionCode(val sessionCode: String) : AttendanceUiEvent()
    data class UpdateUnitCode(val unitCode: String) : AttendanceUiEvent()
    object ResetState : AttendanceUiEvent()
    object RequestLocation : AttendanceUiEvent()
    data class LocationCaptured(val location: LocationData) : AttendanceUiEvent()
    object RetryLocationCapture : AttendanceUiEvent()
    data class LocationError(val error: String) : AttendanceUiEvent()
}

// View Events
sealed class AttendanceEvent {

    data class ShowErrorMessage(val message: String) : AttendanceEvent()

}
