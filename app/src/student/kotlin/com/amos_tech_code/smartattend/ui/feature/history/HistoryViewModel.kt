package com.amos_tech_code.smartattend.ui.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.AttendanceSessionRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// First, update the ViewModel to handle session status filtering correctly
class HistoryViewModel(
    private val attendanceSessionRepository: AttendanceSessionRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(HistoryFilterState())
    val filterState = _filterState.asStateFlow()

    private val _statsState = MutableStateFlow(AttendanceStats())
    val statsState = _statsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _event = Channel<AttendanceHistoryEvent>()
    val event = _event.receiveAsFlow()

    fun getAttendancePagingData(filterState: HistoryFilterState): Flow<PagingData<StudentAttendanceRecordEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            )
        ) {
            attendanceSessionRepository.getAttendancePagingSource()
        }.flow
            .map { pagingData ->
                pagingData.filter { record ->
                    filterState.predicate(record)
                }
            }
            .map { pagingData ->
                // Update stats based on visible data
                updateStatsFromData(pagingData)
                pagingData
            }
            .cachedIn(viewModelScope)
    }

    private fun updateStatsFromData(pagingData: PagingData<StudentAttendanceRecordEntity>) {
        // This is a simplified implementation - in real app, you'd want to query stats separately
        val data = pagingData
        // Calculate stats from the data (this is just a placeholder)
        _statsState.value = AttendanceStats(
            totalSessions = 0,
            attendedSessions = 0,
            scheduledSessions = 0
        )
    }

    fun updateFilter(newState: HistoryFilterState) {
        _filterState.value = newState
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                val result = attendanceSessionRepository.syncStudentAttendanceRecords()

                when(result) {
                    is ApiResult.Success -> {
                        _event.send(AttendanceHistoryEvent.RefreshComplete)
                    }
                    is ApiResult.Failure -> {
                        val message = result.error.extractApiErrorMessage()
                        _event.send(AttendanceHistoryEvent.ShowError(message))
                    }
                }

            } catch (_: Exception) {
                _event.send(AttendanceHistoryEvent.ShowError("Failed to refresh"))
            } finally {
                // Ensure the refreshing state is reset even if an error occurs
                _isRefreshing.value = false
            }
        }
    }


}
