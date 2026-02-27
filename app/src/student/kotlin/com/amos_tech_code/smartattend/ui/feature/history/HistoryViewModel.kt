package com.amos_tech_code.smartattend.ui.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.AttendanceSessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val attendanceSessionRepository: AttendanceSessionRepository,
    session: ClassTrackSession
) : ViewModel() {

    private val _filterState = MutableStateFlow(HistoryFilterState())
    val filterState = _filterState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _event = Channel<AttendanceHistoryEvent>()
    val event = _event.receiveAsFlow()

    init {
        if(!session.isAttendanceSynced()) {
            refresh()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val attendancePagingData: Flow<PagingData<StudentAttendanceRecordEntity>> =
        filterState.flatMapLatest { state ->
            getAttendancePagingData(state)
        }.cachedIn(viewModelScope)


    private fun getAttendancePagingData(filterState: HistoryFilterState): Flow<PagingData<StudentAttendanceRecordEntity>> {
        return attendanceSessionRepository.getAttendancePagingSource()
            .map { pagingData ->
                pagingData.filter { record ->
                    filterState.predicate(record)
                }
            }
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
