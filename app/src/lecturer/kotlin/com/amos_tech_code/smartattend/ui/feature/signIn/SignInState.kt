package com.amos_tech_code.smartattend.ui.feature.signIn

sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    object Error : SignInState()
}