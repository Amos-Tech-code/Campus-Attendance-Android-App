package com.amos_tech_code.smartattend.ui.feature.login

import com.amos_tech_code.smartattend.utils.ErrorMessageType

sealed class LoginEvent {
    data class ShowErrorMessage(val message: String, val type: ErrorMessageType = ErrorMessageType.OTHER) : LoginEvent()
    data object NavigateToHome : LoginEvent()

    data object NavigateToRegister : LoginEvent()
}