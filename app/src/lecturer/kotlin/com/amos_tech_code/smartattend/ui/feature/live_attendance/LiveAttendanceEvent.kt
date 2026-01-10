package com.amos_tech_code.smartattend.ui.feature.live_attendance

// Events for ViewModel
sealed class LiveAttendanceEvent {
    data class ShowErrorMessage(val message: String) : LiveAttendanceEvent()
    data class ShowSuccessMessage(val message: String) : LiveAttendanceEvent()
    data class ShowInfoMessage(val message: String) : LiveAttendanceEvent()
    object SessionEndedSuccessfully : LiveAttendanceEvent()
}

// UI Actions
sealed class LiveAttendanceAction {
    object LoadSession : LiveAttendanceAction()
    object Reconnect : LiveAttendanceAction()
    object ShowQrCode : LiveAttendanceAction()
    object HideQrCode : LiveAttendanceAction()
    data class EndSession(val sessionId: String) : LiveAttendanceAction()
    data class ResolveFlag(val studentId: String, val name: String) : LiveAttendanceAction()
    object RefreshData : LiveAttendanceAction()
    data class ApplyFilter(val programmeId: String?) : LiveAttendanceAction()
    data class ToggleFlaggedFilter(val enabled: Boolean) : LiveAttendanceAction()
    data class ApplySort(val sortBy: SortBy) : LiveAttendanceAction()
    data class ToggleSortOrder(val sortOrder: SortOrder) : LiveAttendanceAction()
    object ClearFilters : LiveAttendanceAction()
    object ClearSort : LiveAttendanceAction()
}
