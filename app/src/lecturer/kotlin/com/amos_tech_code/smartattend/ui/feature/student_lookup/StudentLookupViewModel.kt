package com.amos_tech_code.smartattend.ui.feature.student_lookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.StudentLookupRepository
import com.amos_tech_code.smartattend.domain.request.StudentLookupRequest
import com.amos_tech_code.smartattend.domain.response.StudentLookupResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class StudentLookupViewModel(
    private val studentLookupRepository: StudentLookupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StudentLookupState())
    val state = _state.asStateFlow()

    private val _event = Channel<StudentLookupEvent>()
    val event = _event.receiveAsFlow()

    fun updateRegistrationNumber(value: String) {
        _state.update {
            it.copy(
                registrationNumber = value,
                isSearchEnabled = value.trim().length >= 8, // Minimum 8 characters for reg number
                error = null
            )
        }
    }

    fun searchStudent() {
        val regNo = _state.value.registrationNumber.trim()
        if (regNo.isEmpty()) {
            viewModelScope.launch {
                _event.send(StudentLookupEvent.ShowError("Please enter registration number"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = studentLookupRepository.studentLookup(
                StudentLookupRequest(registrationNumber = regNo)
            )

            when (result) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            studentData = result.data,
                            error = null
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            studentData = null,
                            error = result.error.extractApiErrorMessage()
                        )
                    }
                    _event.send(StudentLookupEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    fun clearSearch() {
        _state.update {
            StudentLookupState()
        }
        viewModelScope.launch {
            _event.send(StudentLookupEvent.ClearData)
        }
    }

    fun onApproveDeviceClicked() {
        _event.trySend(StudentLookupEvent.NavigateToDeviceApproval)
    }
}

data class StudentLookupState(
    val registrationNumber: String = "",
    val isLoading: Boolean = false,
    val studentData: StudentLookupResponse? = null,
    val error: String? = null,
    val isSearchEnabled: Boolean = false
)

sealed class StudentLookupEvent {
    data class ShowError(val message: String) : StudentLookupEvent()
    data class ShowSuccess(val message: String) : StudentLookupEvent()
    object ClearData : StudentLookupEvent()
    object NavigateToDeviceApproval : StudentLookupEvent()
}