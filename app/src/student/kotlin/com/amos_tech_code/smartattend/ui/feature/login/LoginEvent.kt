package com.amos_tech_code.smartattend.ui.feature.login

sealed class LoginEvent {
    data class ShowErrorMessage(val message: String) : LoginEvent()
    data object NavigateToHome : LoginEvent()

    data object NavigateToRegister : LoginEvent()
}