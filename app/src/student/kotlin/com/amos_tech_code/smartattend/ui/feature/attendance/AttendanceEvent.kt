package com.amos_tech_code.smartattend.ui.feature.attendance

import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse

// UI Events
sealed class AttendanceUiEvent {
    data class VerifySession(val sessionCode: String, val secretKey: String) : AttendanceUiEvent()
    data class MarkAttendance(val request: MarkAttendanceRequest) : AttendanceUiEvent()
    data class ProgrammeSelected(val programmeId: String) : AttendanceUiEvent()
    data class QRCodeScanned(val qrData: String) : AttendanceUiEvent()
    data class UpdateLocation(val location: LocationData) : AttendanceUiEvent()
    object ResetState : AttendanceUiEvent()
}

// View Events
sealed class AttendanceEvent {
    data class ShowErrorMessage(val message: String) : AttendanceEvent()
    data class AttendanceMarkedSuccessfully(val response: MarkAttendanceResponse) : AttendanceEvent()
    object NavigateToQRScanner : AttendanceEvent()
    object NavigateToCodeEntry : AttendanceEvent()

}
