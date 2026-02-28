package com.amos_tech_code.smartattend.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.AttendanceSessionRepository
import com.amos_tech_code.smartattend.data.repository.EnrollmentRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val session: ClassTrackSession,
    private val attendanceRepository: AttendanceSessionRepository,
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(StudentHomeState())
    val homeState: StateFlow<StudentHomeState> = _homeState.asStateFlow()

    private val _event = Channel<HomeEvent>()
    val event = _event.receiveAsFlow()

    init {
        fetchUserData()
        observeEnrollment()
        observeStats() // Primary source of truth
        checkAndRefreshStats() // Check if stats need refresh
    }

    private fun fetchUserData() {
        val studentName = session.getName()
        val registrationNo = session.getRegNo()

        _homeState.update {
            it.copy(
                studentName = studentName ?: "",
                registrationNo = registrationNo ?: ""
            )
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            attendanceRepository.observeStats().collect { statsEntity ->
                if (statsEntity != null) {
                    // We have stats from DAO - use them
                    _homeState.update { state ->
                        state.copy(
                            totalSessions = statsEntity.totalSessions,
                            attendedSessions = statsEntity.attendedSessions,
                            attendanceRate = if (statsEntity.totalSessions > 0)
                                (statsEntity.attendedSessions * 100) / statsEntity.totalSessions
                            else 0,
                            currentStreak = statsEntity.currentStreak,
                            lastStatsUpdate = statsEntity.lastUpdated,
                            isLoading = false
                        )
                    }

                    // If total sessions is zero, trigger a refresh from API
                    if (statsEntity.totalSessions == 0) {
                        refreshStatsFromApi()
                    }
                } else {
                    // No stats in DAO - show loading and trigger refresh
                    _homeState.update {
                        it.copy(isLoading = true)
                    }
                    refreshStatsFromApi()
                }
            }
        }
    }

    private fun checkAndRefreshStats() {
        viewModelScope.launch {
            // Check if stats are stale (older than 1 hour)
            val currentState = _homeState.value
            val shouldRefresh = currentState.lastStatsUpdate == null ||
                    System.currentTimeMillis() - (currentState.lastStatsUpdate ?: 0) > 3600000

            if (shouldRefresh) {
                refreshStatsFromApi()
            }
        }
    }

    private suspend fun refreshStatsFromApi() {
        // Set loading state if needed
        if (_homeState.value.totalSessions == 0) {
            _homeState.update {
                it.copy(isLoading = true)
            }
        }

        // Refresh stats from API (this will update the stats DAO)
        when (val result = attendanceRepository.refreshStats()) {
            is ApiResult.Success -> {
                // Stats updated successfully - they will be observed through the flow
                _homeState.update {
                    it.copy(isLoading = false)
                }
                // Also load today's sessions (these come from attendance records, not stats)
                loadTodaySessions()
            }
            is ApiResult.Failure -> {
                // API call failed - show error but keep existing stats if any
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                _event.send(HomeEvent.ShowErrorMessage("Failed to refresh stats: ${result.error.extractApiErrorMessage()}"))
            }
        }
    }

    private suspend fun loadTodaySessions() {
        // Today's sessions are separate from stats - these come from attendance records
        val todaySessions = attendanceRepository.getTodaySessions()
        _homeState.update {
            it.copy(
                todaySessions = todaySessions.map { session -> session.toTodaySession() }
            )
        }
    }

    private fun observeEnrollment() {
        viewModelScope.launch {
            enrollmentRepository.getActiveEnrollmentFlow().collect { enrollment ->
                _homeState.update {
                    it.copy(
                        currentYear = enrollment?.yearOfStudy ?: 0,
                        currentSemester = enrollment?.academicTerm?.semester ?: 0,
                        programme = enrollment?.programme?.name ?: ""
                    )
                }
            }
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _homeState.update { it.copy(isRefreshing = true) }

            try {
                // Sync attendance records first (these are separate from stats)
                val syncResult = attendanceRepository.syncStudentAttendanceRecords()

                // Then refresh stats from API (this updates the stats DAO)
                val statsResult = attendanceRepository.refreshStats()

                when {
                    statsResult is ApiResult.Success -> {
                        // Stats will be updated via observeStats flow
                        loadTodaySessions()
                        _homeState.update {
                            it.copy(
                                isRefreshing = false,
                                isLoading = false
                            )
                        }
                        _event.send(HomeEvent.ShowSuccessMessage("Dashboard updated"))
                    }
                    syncResult is ApiResult.Success -> {
                        // Only sync succeeded but stats refresh failed
                        _homeState.update { it.copy(isRefreshing = false) }
                        _event.send(HomeEvent.ShowErrorMessage("Failed to refresh stats, but attendance synced"))
                    }
                    else -> {
                        _homeState.update { it.copy(isRefreshing = false) }
                        _event.send(HomeEvent.ShowErrorMessage("Failed to sync dashboard"))
                    }
                }
            } catch (e: Exception) {
                _homeState.update { it.copy(isRefreshing = false) }
                _event.send(HomeEvent.ShowErrorMessage("Refresh failed: ${e.message}"))
            }
        }
    }


}

