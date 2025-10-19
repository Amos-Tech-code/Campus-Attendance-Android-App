package com.amos_tech_code.smartattend.ui.feature.start_session

sealed class StartSessionEvent {
    data class ShowErrorMessage(val message: String) : StartSessionEvent()

    object CompleteProfile : StartSessionEvent()

    data class SessionStarted(val sessionId: String) : StartSessionEvent()

}