package com.amos_tech_code.smartattend.ui.feature.login

data class LoginState(
    val regNo: String = "",
    val isLoading: Boolean = false,
    val regNoError: String? = null,
)