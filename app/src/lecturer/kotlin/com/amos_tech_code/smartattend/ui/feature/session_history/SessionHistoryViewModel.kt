package com.amos_tech_code.smartattend.ui.feature.session_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.SessionRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SessionHistoryViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _event = Channel<SessionHistoryEvent>()
    val event = _event.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val pagedSessions: Flow<PagingData<SessionUiModel>> = sessionRepository
        .getGroupedSessionsPaged()
        .cachedIn(viewModelScope)

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                when(val result = sessionRepository.syncSessionHistory()) {
                    is ApiResult.Success -> {
                        _event.send(SessionHistoryEvent.RefreshComplete)
                    }
                    is ApiResult.Failure -> {
                        handleApiError(result.error)
                    }
                }
            } catch (e: Exception) {
                _event.send(SessionHistoryEvent.ShowError("Refresh failed: ${e.message}"))
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onSessionClick(sessionId: String) {
        viewModelScope.launch {
            _event.send(SessionHistoryEvent.NavigateToSessionDetail(sessionId))
        }
    }

    private fun handleApiError(error: ApiError) {
        val message = when (error) {
            is ApiError.NetworkError -> "Network error: ${error.exception.message}"
            is ApiError.HttpError -> {
                "Error ${error.statusCode}: ${error.message}"
            }
            is ApiError.UnknownError -> "Unknown error: ${error.throwable.message}"
        }
        _event.trySend(SessionHistoryEvent.ShowError(message))
    }

}



sealed class SessionUiModel {
    data class DateHeader(val dateString: String, val displayDate: String) : SessionUiModel()
    data class SessionItem(val session: AttendanceSessionHistoryEntity) : SessionUiModel()
}