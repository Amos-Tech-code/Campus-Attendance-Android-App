package com.amos_tech_code.smartattend.ui.feature.register

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _event = Channel<RegisterEvent>()
    val event = _event.receiveAsFlow()

    fun updateFullName(fullName: String) {
        _state.update { it.copy(fullName = fullName) }
    }

    fun updateRegNo(regNo: String) {
        _state.update { it.copy(regNo = regNo) }
    }

    fun register() {

    }

    fun navigateToLogin() {
        _event.trySend(RegisterEvent.NavigateToLogin)
    }

}