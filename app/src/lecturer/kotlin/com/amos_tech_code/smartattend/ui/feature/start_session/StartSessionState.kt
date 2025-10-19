package com.amos_tech_code.smartattend.ui.feature.start_session

sealed class StartSessionState {
    data object Nothing : StartSessionState()
    data object Loading : StartSessionState()
    data object Success : StartSessionState()
    data class Error(val message: String) : StartSessionState()
}