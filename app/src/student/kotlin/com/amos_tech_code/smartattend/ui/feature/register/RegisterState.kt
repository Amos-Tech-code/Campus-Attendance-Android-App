package com.amos_tech_code.smartattend.ui.feature.register

//sealed class RegisterState {
//    data object Nothing : RegisterState()
//    data object Loading : RegisterState()
//    data object Success : RegisterState()
//    data class Error(val message: String) : RegisterState()
//}
data class RegisterState(
    val fullName: String = "",
    val regNo: String = "",
    val isLoading: Boolean = false,
    val fullNameError: String? = null,
    val regNoError: String? = null
)