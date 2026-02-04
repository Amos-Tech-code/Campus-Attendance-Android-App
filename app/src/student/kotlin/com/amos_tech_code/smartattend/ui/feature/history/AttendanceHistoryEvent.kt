package com.amos_tech_code.smartattend.ui.feature.history

sealed class AttendanceHistoryEvent {
    data class ShowError(val message: String) : AttendanceHistoryEvent()
    data object RefreshComplete : AttendanceHistoryEvent()
}