package com.amos_tech_code.smartattend.ui.feature.live_attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AttendanceRepository
import com.amos_tech_code.smartattend.ui.feature.start_session.StartSessionViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiveAttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val startSessionViewModel: StartSessionViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(LiveAttendanceState())
    val state: StateFlow<LiveAttendanceState> = _state

    private val _event = Channel<LiveAttendanceEvent>()
    val event = _event.receiveAsFlow()

    init {
        fetchAttendanceData()
    }

    fun fetchAttendanceData() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = attendanceRepository.getActiveSession()

                when (result) {
                    is ApiResult.Success -> {
                        _state.update {
                            it.copy(
                                session = result.data,
                                isLoading = false
                            )
                        }
                    }

                    is ApiResult.Failure -> {
                        when (val error = result.error) {
                            is ApiError.NetworkError -> {
                                _event.send(LiveAttendanceEvent.ShowErrorMessage("Network error: ${error.exception.message}"))
                            }
                            is ApiError.HttpError -> {
                                _event.send(LiveAttendanceEvent.ShowErrorMessage("HTTP error: ${error.statusCode} - ${error.message}"))
                            }
                            is ApiError.UnknownError -> {
                                _event.send(LiveAttendanceEvent.ShowErrorMessage("Unknown error: Please try again ${error.throwable.message}"))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _event.send(LiveAttendanceEvent.ShowErrorMessage("Failed to fetch attendance data"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun endSession(sessionId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = attendanceRepository.endActiveSession(sessionId)
                when (result) {
                    is ApiResult.Success -> {
                        _event.send(LiveAttendanceEvent.SessionEnded)
                        startSessionViewModel.clearSuccessState()
                    }
                    is ApiResult.Failure -> {
                        when (val error = result.error) {
                            is ApiError.NetworkError -> {
                                _event.send(LiveAttendanceEvent.ShowErrorMessage("Network error: ${error.exception.message}"))
                            }
                            is ApiError.HttpError -> {
                                _event.send(LiveAttendanceEvent.ShowErrorMessage("HTTP error: ${error.statusCode} - ${error.message}"))
                            }
                            is ApiError.UnknownError -> {
                                _event.send(LiveAttendanceEvent.ShowErrorMessage("Unknown error: Please try again"))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _event.send(LiveAttendanceEvent.ShowErrorMessage("Failed to end session. Please try again."))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showQrCodeState() {
        if (_state.value.session != null) {
            _state.update { it.copy(showQrCode = true) }
        } else {
            _event.trySend(LiveAttendanceEvent.ShowErrorMessage("Session not found"))
        }
    }

    fun hideQrCodeState() {
        _state.update { it.copy(showQrCode = false) }
    }



}