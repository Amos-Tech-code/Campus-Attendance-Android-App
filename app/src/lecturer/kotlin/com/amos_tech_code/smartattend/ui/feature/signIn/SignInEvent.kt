package com.amos_tech_code.smartattend.ui.feature.signIn

sealed class SignInEvent {

    data class ShowErrorDialog(val message: String) : SignInEvent()

    data class ShowNetworkErrorDialog(val message: String) : SignInEvent()

    object NavigateToHome : SignInEvent()
}