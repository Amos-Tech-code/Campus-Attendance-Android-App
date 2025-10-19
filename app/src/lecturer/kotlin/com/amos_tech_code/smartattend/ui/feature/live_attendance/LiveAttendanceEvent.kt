package com.amos_tech_code.smartattend.ui.feature.live_attendance

sealed class LiveAttendanceEvent {

    data class ShowErrorMessage(val message: String) : LiveAttendanceEvent()

    object SessionEnded : LiveAttendanceEvent()
}
