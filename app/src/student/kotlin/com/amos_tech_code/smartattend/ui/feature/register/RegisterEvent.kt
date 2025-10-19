package com.amos_tech_code.smartattend.ui.feature.register

import com.amos_tech_code.smartattend.utils.ErrorMessageType

sealed class RegisterEvent {
    data class ShowErrorMessage(val message: String, val type: ErrorMessageType = ErrorMessageType.OTHER) : RegisterEvent()
    data object NavigateToLogin : RegisterEvent()
    data object NavigateToHome : RegisterEvent()
}