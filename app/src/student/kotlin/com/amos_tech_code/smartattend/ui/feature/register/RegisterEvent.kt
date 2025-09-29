package com.amos_tech_code.smartattend.ui.feature.register

sealed class RegisterEvent {
    data class ShowErrorMessage(val message: String) : RegisterEvent()
    data object NavigateToLogin : RegisterEvent()
    data object NavigateToHome : RegisterEvent()
}