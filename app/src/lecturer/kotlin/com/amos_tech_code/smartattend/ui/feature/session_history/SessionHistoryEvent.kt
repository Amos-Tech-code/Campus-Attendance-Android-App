package com.amos_tech_code.smartattend.ui.feature.session_history

sealed class SessionHistoryEvent {
    data class ShowErrorMessage(val message: String) : SessionHistoryEvent()
}