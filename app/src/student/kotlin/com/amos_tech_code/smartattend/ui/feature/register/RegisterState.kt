package com.amos_tech_code.smartattend.ui.feature.register

data class RegisterState(
    val fullName: String = "",
    val regNo: String = "",
    val isLoading: Boolean = false,
    val fullNameError: String? = null,
    val regNoError: String? = null
)