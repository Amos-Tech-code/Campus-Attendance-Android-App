package com.amos_tech_code.smartattend.ui.feature.live_attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.LiveAttendanceUpdate
import com.amos_tech_code.smartattend.data.repositories.AttendanceRepository
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

    fun onAction(action: LiveAttendanceAction) {
        when (action) {
            is LiveAttendanceAction.LoadSession -> loadActiveSession()
            is LiveAttendanceAction.Reconnect -> reconnect()
            is LiveAttendanceAction.ShowQrCode -> showQrCodeState()
            is LiveAttendanceAction.HideQrCode -> hideQrCodeState()
            is LiveAttendanceAction.EndSession -> endSession(action.sessionId)
            is LiveAttendanceAction.ResolveFlag -> resolveFlag(action.studentId, action.name)
            is LiveAttendanceAction.RefreshData -> refreshData()
            is LiveAttendanceAction.ApplyFilter -> applyFilter(action.programmeId)
            is LiveAttendanceAction.ToggleFlaggedFilter -> toggleFlaggedFilter(action.enabled)
            is LiveAttendanceAction.ApplySort -> applySort(action.sortBy)
            is LiveAttendanceAction.ToggleSortOrder -> toggleSortOrder(action.sortOrder)
            is LiveAttendanceAction.ClearFilters -> clearFilters()
            is LiveAttendanceAction.ClearSort -> clearSort()
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
                            notifyNewAttendance()
                        }
                    }
                }
        }
    }

    fun endSession(sessionId: String) {
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

    fun resolveFlag(studentId: String, studentName: String) {
        viewModelScope.launch {
            try {
                when (val result = attendanceRepository.resolveFlaggedStudent(studentId)) {
                    is ApiResult.Success -> {
                        _event.send(LiveAttendanceEvent.ShowSuccessMessage("Flag resolved successfully"))
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

    fun reconnect() {
        _state.value.session?.sessionId?.let { sessionId ->
            _state.update { it.copy(connectionState = ConnectionState.CONNECTING) }
            startSSEConnection(sessionId)
        }
    }

    fun refreshData() {
        loadActiveSession()
    }

    private fun handleApiError(error: ApiError) {
        val message = when (error) {
            is ApiError.NetworkError -> "Network error: ${error.exception.message}"
            is ApiError.HttpError -> {
                if (error.statusCode == 404) {
                    //_event.trySend(LiveAttendanceEvent.NoActiveSession)
                    "No active session found"
                } else {
                    "HTTP error ${error.statusCode}: ${error.message}"
                }
            }
            is ApiError.UnknownError -> "Unknown error: ${error.throwable.message}"
        }
        _event.trySend(LiveAttendanceEvent.ShowErrorMessage(message))
    }

    private fun notifyNewAttendance() {
        // Optional: Implement notification sound/vibration
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    // Public methods
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

    override fun onCleared() {
        super.onCleared()
        sseJob?.cancel()
    }
}