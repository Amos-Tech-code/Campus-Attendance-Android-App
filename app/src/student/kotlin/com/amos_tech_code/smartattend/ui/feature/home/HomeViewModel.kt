package com.amos_tech_code.smartattend.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repository.AttendanceSessionRepository
import com.amos_tech_code.smartattend.data.repository.EnrollmentRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Update ViewModel to support home screen data
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
        observeStats()
        refreshStatsIfNeeded()
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

        // Load dashboard data
        viewModelScope.launch {
            loadDashboardData()
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            attendanceRepository.observeStats().collect { stats ->
                stats?.let {
                    _homeState.update { state ->
                        state.copy(
                            totalSessions = it.totalSessions,
                            attendedSessions = it.attendedSessions,
                            attendanceRate = if (it.totalSessions > 0)
                                (it.attendedSessions * 100) / it.totalSessions
                            else 0,
                            currentStreak = it.currentStreak
                        )
                    }
                }
            }
        }
    }

    private fun refreshStatsIfNeeded() {
        viewModelScope.launch {
            attendanceRepository.refreshStats()
        }
    }

    private suspend fun loadDashboardData() {
        // Get today's sessions from local database
        val todaySessions = attendanceRepository.getTodaySessions()

        // Calculate attendance stats from local records as fallback
        val totalSessions = attendanceRepository.getTotalSessionsCount()
        val attendedSessions = attendanceRepository.getAttendedSessionsCount()
        val attendanceRate = if (totalSessions > 0) {
            (attendedSessions * 100) / totalSessions
        } else 0
        val currentStreak = attendanceRepository.getCurrentStreak()

        // Get recent attendance
        val recentAttendance = attendanceRepository.getRecentAttendance(5)

        _homeState.update {
            it.copy(
                todaySessions = todaySessions.map { session -> session.toTodaySession() },
                totalSessions = totalSessions,
                attendedSessions = attendedSessions,
                attendanceRate = attendanceRate,
                currentStreak = currentStreak, // Fixed: Added the value
                recentAttendance = recentAttendance.map { it.toRecentAttendance() },
                isLoading = false
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
                // Sync with server
                val syncResult = attendanceRepository.syncStudentAttendanceRecords()
                // Refresh stats from API
                val statsResult = attendanceRepository.refreshStats()

                when {
                    syncResult is ApiResult.Success && statsResult is ApiResult.Success -> {
                        loadDashboardData()
                        _event.send(HomeEvent.ShowSuccessMessage("Dashboard updated"))
                    }
                    syncResult is ApiResult.Success -> {
                        loadDashboardData()
                        _event.send(HomeEvent.ShowSuccessMessage("Dashboard updated (offline mode)"))
                    }
                    else -> {
                        _event.send(HomeEvent.ShowErrorMessage("Failed to sync"))
                    }
                }
            } catch (e: Exception) {
                _event.send(HomeEvent.ShowErrorMessage("Refresh failed: ${e.message}"))
            } finally {
                _homeState.update { it.copy(isRefreshing = false) }
            }
        }
    }
}

