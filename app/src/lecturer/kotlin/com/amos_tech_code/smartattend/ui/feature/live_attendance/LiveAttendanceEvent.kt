package com.amos_tech_code.smartattend.ui.feature.live_attendance

// Events for ViewModel
sealed class LiveAttendanceEvent {
    data class ShowErrorMessage(val message: String) : LiveAttendanceEvent()
    data class ShowSuccessMessage(val message: String) : LiveAttendanceEvent()
    data class ShowInfoMessage(val message: String) : LiveAttendanceEvent()
    object SessionEndedSuccessfully : LiveAttendanceEvent()
}

// UI Actions
sealed class LiveAttendanceUIEvent {
    //object LoadSession : LiveAttendanceUIEvent()
    object Reconnect : LiveAttendanceUIEvent()
    object ShowSessionDetails : LiveAttendanceUIEvent()
    object HideSessionDetails : LiveAttendanceUIEvent()
    data class EndSession(val sessionId: String) : LiveAttendanceUIEvent()
    data class RemoveFlaggedStudent(val studentId: String, val name: String) : LiveAttendanceUIEvent()
    object RefreshData : LiveAttendanceUIEvent()
    data class ApplyFilter(val programmeId: String?) : LiveAttendanceUIEvent()
    data class ToggleFlaggedFilter(val enabled: Boolean) : LiveAttendanceUIEvent()
    data class ApplySort(val sortBy: SortBy) : LiveAttendanceUIEvent()
    data class ToggleSortOrder(val sortOrder: SortOrder) : LiveAttendanceUIEvent()
    object ClearFilters : LiveAttendanceUIEvent()
    object ClearSort : LiveAttendanceUIEvent()
}
