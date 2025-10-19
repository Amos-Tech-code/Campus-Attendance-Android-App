package com.amos_tech_code.smartattend.ui.feature.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AuthRepository
import com.amos_tech_code.smartattend.domain.models.request.StudentRegisterRequest
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import com.amos_tech_code.smartattend.utils.ErrorMessageType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val session: SmartAttendSession
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _event = Channel<RegisterEvent>()
    val event = _event.receiveAsFlow()

    fun updateFullName(fullName: String) {
        _state.update { it.copy(
            fullName = fullName,
            fullNameError = if (fullName.isNotEmpty()) null else it.fullNameError
        ) }
    }

    fun updateRegNo(regNo: String) {
        _state.update {
            it.copy(
                regNo = regNo.uppercase().trim(),
                regNoError = if (regNo.isNotEmpty()) null else it.regNoError
            )
        }
    }

    fun register() {
        if (!validateFullName(state.value.fullName) || !validateRegNo(state.value.regNo)) return
        // ✅ Continue with registration logic
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val deviceInfo = deviceInfoProvider.getDeviceInfo()
                val result = authRepository.studentRegister(
                    StudentRegisterRequest(
                        fullName = state.value.fullName.trim(),
                        registrationNumber = state.value.regNo,
                        deviceInfo = deviceInfo
                    )
                )
                when (result) {
                    is ApiResult.Success -> {
                        session.saveStudentSession(
                            token = result.data.token,
                            name = result.data.fullName,
                            regNo = result.data.regNumber
                        )
                        _state.update { it.copy(isLoading = false) }
                        _event.trySend(RegisterEvent.NavigateToHome)
                    }
                    is ApiResult.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        when (result.error) {
                            is ApiError.HttpError -> {
                                _event.trySend(RegisterEvent.ShowErrorMessage(result.error.message))
                            }
                            is ApiError.NetworkError -> {
                                _event.trySend(RegisterEvent.ShowErrorMessage(result.error.exception.message ?: "Network Error. Please check your internet connection.", type = ErrorMessageType.NETWORK))

                            }
                            is ApiError.UnknownError -> {
                                _event.trySend(RegisterEvent.ShowErrorMessage(result.error.throwable.message ?: "An Unknown error occurred. Please try again"))
                            }
                        }
                    }

                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _event.trySend(RegisterEvent.ShowErrorMessage(e.message ?: "Something went wrong. Please try again."))

            }
        }

    }

    fun navigateToLogin() {
        _event.trySend(RegisterEvent.NavigateToLogin)
    }

    fun validateFullName(name: String): Boolean {
        return when {
            name.isBlank() -> {
                _state.update { it.copy(fullNameError = "Full name is required") }
                false
            }
            name.length < 3 -> {
                _state.update { it.copy(fullNameError = "Name must be at least 3 characters") }
                false
            }
            name.length > 50 -> {
                _state.update { it.copy(fullNameError = "Name must not exceed 50 characters") }
                false
            }
            !Regex("^[a-zA-Z\\s.'-]+\$").matches(name) -> {
                _state.update { it.copy(fullNameError = "Name can only contain letters, spaces, hyphens, and apostrophes") }
                false
            }
            name.trim().split("\\s+".toRegex()).size < 2 -> {
                _state.update { it.copy(fullNameError = "Please enter your full names (first and last name)") }
                false
            }
            else -> {
                _state.update { it.copy(fullNameError = null) } // Clear error if valid
                true
            }
        }
    }

    fun validateRegNo(regNo: String): Boolean {
        val regex = Regex(
            pattern = """^[A-Z]{2,5}\d{0,3}[-/]\d{3,5}[-/]\d{4}$""",
            option = RegexOption.IGNORE_CASE
        )

        return when {
            regNo.isBlank() -> {
                _state.update { it.copy(regNoError = "Registration number is required") }
                false
            }
            regNo.length < 5 -> {
                _state.update { it.copy(regNoError = "Registration number too short") }
                false
            }
            regNo.length > 25 -> {
                _state.update { it.copy(regNoError = "Registration number too long") }
                false
            }
            !regex.matches(regNo.trim()) -> {
                _state.update { it.copy(regNoError = "Invalid registration number format") }
                false
            }
            else -> {
                _state.update { it.copy(regNoError = null) }
                true
            }
        }
    }

}
