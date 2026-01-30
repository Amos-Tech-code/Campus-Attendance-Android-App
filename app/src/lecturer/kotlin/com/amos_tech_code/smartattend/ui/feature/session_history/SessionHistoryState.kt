package com.amos_tech_code.smartattend.ui.feature.session_history

sealed class SessionHistoryState {
    data object Loading : SessionHistoryState()
    data object Success : SessionHistoryState()
    data class Error(val message: String) : SessionHistoryState()
}