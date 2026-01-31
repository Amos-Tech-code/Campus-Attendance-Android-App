package com.amos_tech_code.smartattend.ui.feature.session_history


sealed class SessionHistoryEvent {
    data class ShowError(val message: String) : SessionHistoryEvent()
    data class NavigateToSessionDetail(val sessionId: String) : SessionHistoryEvent()
    data object RefreshComplete : SessionHistoryEvent()
}