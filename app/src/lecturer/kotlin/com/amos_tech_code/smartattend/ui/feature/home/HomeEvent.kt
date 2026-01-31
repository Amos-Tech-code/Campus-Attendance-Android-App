package com.amos_tech_code.smartattend.ui.feature.home
sealed class HomeEvent {
    data class ShowErrorMessage(val message: String) : HomeEvent()
    data object NavigateToSetup : HomeEvent()
    data object NavigateToStartSession : HomeEvent()
    data object NavigateToSessionHistory : HomeEvent()
    data class NavigateToSessionHistoryDetails(val sessionId: String) : HomeEvent()
    data object NavigateToNotifications : HomeEvent()
    data object NavigateToSettings : HomeEvent()
    data object NavigateToStudentLookup : HomeEvent()
    data object NavigateToExport : HomeEvent()
    data class NavigateToExportSessionAttendance(val sessionId: String) : HomeEvent()
    data object RefreshComplete : HomeEvent()
}