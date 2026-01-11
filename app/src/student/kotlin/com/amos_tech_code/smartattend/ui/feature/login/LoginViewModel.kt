package com.amos_tech_code.smartattend.ui.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AuthRepository
import com.amos_tech_code.smartattend.domain.request.StudentLoginRequest
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import com.amos_tech_code.smartattend.utils.ErrorMessageType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val session: ClassTrackSession
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _event = Channel<LoginEvent>()
    val event = _event.receiveAsFlow()

    fun updateRegNo(regNo: String) {
        _state.update {
            it.copy(
                regNo = regNo.uppercase().trim(), // Auto-format registration number
                regNoError = if (regNo.isNotEmpty()) null else it.regNoError
            )
        }
    }

    fun login() {
        if (!validateInput()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val deviceInfo = deviceInfoProvider.getDeviceInfo()
                // ✅ Send login request
                val result = authRepository.studentLogin(
                    request = StudentLoginRequest(
                        registrationNumber = state.value.regNo,
                        deviceInfo = deviceInfo
                    )
                )

                when (result) {
                    is ApiResult.Success -> {
                        session.saveStudentSession(
                            token = result.data.token,
                            name = result.data.fullName,
                            regNo = result.data.regNumber,
                            deviceInfo = deviceInfo
                        )
                        _state.update { it.copy(isLoading = false) }
                        _event.trySend(LoginEvent.NavigateToHome)
                    }

                    is ApiResult.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        when(result.error) {
                            is ApiError.HttpError -> {
                                _event.trySend(LoginEvent.ShowErrorMessage(result.error.message))
                            }
                            is ApiError.NetworkError -> {
                                _event.trySend(LoginEvent.ShowErrorMessage(result.error.exception.message ?: "Network Error. Please check your internet connection.", type = ErrorMessageType.NETWORK))
                            }
                            is ApiError.UnknownError -> {
                                _event.trySend(LoginEvent.ShowErrorMessage(result.error.throwable.message ?: "An Unknown error occurred. Please try again later."))
                            }
                        }

                    }
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _event.trySend(LoginEvent.ShowErrorMessage(
                    message = e.message ?: "An Unknown error occurred. Please try again later.")
                )
            }
        }
    }

    fun navigateToRegister() {
        _event.trySend(LoginEvent.NavigateToRegister)
    }

    private fun validateInput(): Boolean {
        val regNo = state.value.regNo.trim()

        if (regNo.isEmpty() || regNo.length < 4) {
            _state.update { it.copy(regNoError = "Valid registration number required") }
            return false
        }

        _state.update { it.copy(regNoError = null) }
        return true
    }

}