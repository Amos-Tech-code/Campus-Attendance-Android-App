package com.amos_tech_code.smartattend.ui.feature.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _event = Channel<LoginEvent>()
    val event = _event.receiveAsFlow()

    fun updateRegNo(regNo: String) {
        _state.update { it.copy(regNo = regNo) }
    }

    fun login() {

    }

    fun navigateToRegister() {
        _event.trySend(LoginEvent.NavigateToRegister)
    }

}

data class LoginState(
    val regNo: String = "",
    val isLoading: Boolean = false,
    val regNoError: String? = null,
    val showDeviceWarning: Boolean = false
)