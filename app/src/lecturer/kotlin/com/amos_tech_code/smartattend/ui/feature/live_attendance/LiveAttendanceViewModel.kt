package com.amos_tech_code.smartattend.ui.feature.live_attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.LiveAttendanceUpdate
import com.amos_tech_code.smartattend.data.repositories.AttendanceRepository
import com.amos_tech_code.smartattend.domain.request.RemoveAttendanceRecordRequest
import com.amos_tech_code.smartattend.ui.feature.start_session.StartSessionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class LiveAttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val startSessionViewModel: StartSessionViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(LiveAttendanceState())
    val state: StateFlow<LiveAttendanceState> = _state

    private val _event = Channel<LiveAttendanceEvent>()
    val event = _event.receiveAsFlow()

    private var sseJob: Job? = null

    init {
        loadActiveSession()
    }

    fun onAction(action: LiveAttendanceUIEvent) {
        when (action) {
            is LiveAttendanceUIEvent.Reconnect -> reconnect()
            is LiveAttendanceUIEvent.ShowSessionDetails -> showSessionDetails()
            is LiveAttendanceUIEvent.HideSessionDetails -> hideSessionDetails()
            is LiveAttendanceUIEvent.EndSession -> endSession(action.sessionId)
            is LiveAttendanceUIEvent.RemoveFlaggedStudent -> removeFlaggedStudent(action.studentId, action.name)
            is LiveAttendanceUIEvent.RefreshData -> refreshData()
            is LiveAttendanceUIEvent.ApplyFilter -> applyFilter(action.programmeId)
            is LiveAttendanceUIEvent.ToggleFlaggedFilter -> toggleFlaggedFilter(action.enabled)
            is LiveAttendanceUIEvent.ApplySort -> applySort(action.sortBy)
            is LiveAttendanceUIEvent.ToggleSortOrder -> toggleSortOrder(action.sortOrder)
            is LiveAttendanceUIEvent.ClearFilters -> clearFilters()
            is LiveAttendanceUIEvent.ClearSort -> clearSort()
        }
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val result = attendanceRepository.getActiveSession()
                when (result) {
                    is ApiResult.Success -> {
                        val session = result.data
                        _state.update { state ->
                            state.copy(
                                session = session,
                                isLoading = false,
                                lastUpdate = getCurrentTime()
                            )
                        }

                        // Start SSE connection if session exists
                        startSSEConnection(session.sessionId)
                    }
                    is ApiResult.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        handleApiError(result.error)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _event.send(LiveAttendanceEvent.ShowErrorMessage("Failed to load session: ${e.message}"))
            }
        }
    }

    private fun startSSEConnection(sessionId: String) {
        sseJob?.cancel()

        _state.update { it.copy(connectionState = ConnectionState.CONNECTING) }

        sseJob = viewModelScope.launch {
            attendanceRepository.observeLiveAttendance(sessionId)
                .catch { error ->
                    _state.update { it.copy(connectionState = ConnectionState.DISCONNECTED) }
                    _event.send(LiveAttendanceEvent.ShowErrorMessage("Live stream error: Something went wrong."))

                    // Auto-reconnect after 10 seconds
                    delay(10000)
                    if (_state.value.session != null) {
                        startSSEConnection(sessionId)
                    }
                }
                .collect { update ->
                    _state.update { it.copy(
                        connectionState = ConnectionState.CONNECTED,
                        lastUpdate = getCurrentTime()
                    ) }

                    when (update) {
                        is LiveAttendanceUpdate.InitialState -> {
                            _state.update { state ->
                                state.updateFromSnapshot(update.snapshot)
                            }
                        }
                        is LiveAttendanceUpdate.AttendanceMarked -> {
                            _state.update { state ->
                                state.updateFromAttendanceEvent(update.event)
                            }
                            // Optional: Play notification sound/vibration
                            //notifyNewAttendance()
                        }
                    }
                }
        }
    }

    private fun endSession(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val result = attendanceRepository.endActiveSession(sessionId)
                when (result) {
                    is ApiResult.Success -> {
                        sseJob?.cancel()
                        startSessionViewModel.clearSuccessState()
                        _event.send(LiveAttendanceEvent.SessionEndedSuccessfully)
                        // SessionEnded will be handled by UI navigation
                    }
                    is ApiResult.Failure -> {
                        handleApiError(result.error)
                    }
                }
            } catch (e: Exception) {
                _event.send(LiveAttendanceEvent.ShowErrorMessage("Failed to end session"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun removeFlaggedStudent(studentId: String, studentName: String) {
        viewModelScope.launch {
            try {
                val request = RemoveAttendanceRecordRequest(
                    sessionId = _state.value.session?.sessionId ?: "",
                    studentId = studentId
                )
                when (val result = attendanceRepository.removeFlaggedStudent(request)) {
                    is ApiResult.Success -> {
                        _event.send(LiveAttendanceEvent.ShowSuccessMessage("Student $studentName removed successfully"))
                    }
                    is ApiResult.Failure -> {
                        handleApiError(result.error)
                    }
                }
            } catch (e: Exception) {
                _event.send(LiveAttendanceEvent.ShowErrorMessage("Failed to resolve flag for student: $studentName"))
            }
        }
    }

    private fun applyFilter(programmeId: String?) {
        _state.update { state ->
            state.copy(
                filterOptions = state.filterOptions.copy(
                    selectedProgrammeId = programmeId,
                    // Reset year when changing programme
                    selectedYear = null
                )
            )
        }
    }

    private fun toggleFlaggedFilter(enabled: Boolean) {
        _state.update { state ->
            state.copy(
                filterOptions = state.filterOptions.copy(
                    showOnlyFlagged = enabled
                )
            )
        }
    }

    private fun applySort(sortBy: SortBy) {
        _state.update { state ->
            state.copy(
                sortOptions = state.sortOptions.copy(sortBy = sortBy)
            )
        }
    }

    private fun toggleSortOrder(sortOrder: SortOrder) {
        _state.update { state ->
            state.copy(
                sortOptions = state.sortOptions.copy(sortOrder = sortOrder)
            )
        }
    }

    private fun clearFilters() {
        _state.update { state ->
            state.copy(
                filterOptions = FilterOptions()
            )
        }
    }

    private fun clearSort() {
        _state.update { state ->
            state.copy(
                sortOptions = SortOptions()
            )
        }
    }

    private fun reconnect() {
        _state.value.session?.sessionId?.let { sessionId ->
            _state.update { it.copy(connectionState = ConnectionState.CONNECTING) }
            startSSEConnection(sessionId)
        }
    }

    private fun refreshData() {
        loadActiveSession()
    }

    private fun handleApiError(error: ApiError) {
        val message = when (error) {
            is ApiError.NetworkError -> "Network error: ${error.exception.message}"
            is ApiError.HttpError -> {
                "Error ${error.statusCode}: ${error.message}"
            }
            is ApiError.UnknownError -> "Unknown error: ${error.throwable.message}"
        }
        _event.trySend(LiveAttendanceEvent.ShowErrorMessage(message))
    }

    // Public methods
    private fun showSessionDetails() {
        if (_state.value.session != null) {
            _state.update { it.copy(showSessionDetails = true) }
        } else {
            _event.trySend(LiveAttendanceEvent.ShowErrorMessage("Session not found"))
        }
    }

    private fun hideSessionDetails() {
        _state.update { it.copy(showSessionDetails = false) }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        sseJob?.cancel()
    }
}