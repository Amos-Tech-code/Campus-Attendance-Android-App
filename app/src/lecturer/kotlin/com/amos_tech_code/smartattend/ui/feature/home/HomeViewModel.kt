package com.amos_tech_code.smartattend.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.NotificationRepository
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repository.SessionRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel (
    private val session: ClassTrackProSession,
    private val universityRepository: UniversityRepository,
    private val academicSetUpRepository: AcademicSetUpRepository,
    private val sessionHistoryRepository: SessionRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state

    private val _event = Channel<HomeEvent>()
    val event = _event.receiveAsFlow()
    private var selectedUniversityId: String? = null

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // First, check if profile is complete
            if (!session.isProfileComplete()) {
                _state.value = HomeUiState.NoInstitutionSetup
                return@launch
            }

            // Check if academic data needs syncing
            if (!session.getAcademicSyncStatus()) {
                _state.value = HomeUiState.Loading
                try {
                    academicSetUpRepository.syncLecturerAcademics(null)
                } catch (e: Exception) {
                    _state.value = HomeUiState.Error("Failed to sync academic data: ${e.message}")
                    return@launch
                }
            }

            val notificationCountsFlow = flow<Int> {
                when (val result = notificationRepository.getNotificationCounts()) {
                    is ApiResult.Success -> emit(result.data.unread)
                    is ApiResult.Failure -> emit(0)
                }
            }


            // Now observe the data
            combine(
                universityRepository.observeAllUniversitiesWithStats(),
                sessionHistoryRepository.observeTodaysSessions(),
                notificationCountsFlow
            ) { universitiesWithStats, todaySessions, unreadNotifications ->
                // If still no universities after sync, show empty state
                if (universitiesWithStats.isEmpty()) {
                    HomeUiState.NoInstitutionSetup
                } else {
                    val activeUniversity = universitiesWithStats.find { it.university.isActive }
                    val lecturerName = session.getName() ?: "Lecturer"

                    // Auto-select first active university if none selected
                    selectedUniversityId = selectedUniversityId ?: activeUniversity?.university?.id
                            ?: universitiesWithStats.firstOrNull()?.university?.id

                    HomeUiState.SetupComplete(
                        lecturerName = lecturerName,
                        allUniversities = universitiesWithStats.sortedByDescending { it.university.isActive },
                        activeUniversity = activeUniversity,
                        todaysSessions = todaySessions,
                        totalNotifications = unreadNotifications
                    )
                }
            }
                .catch { error ->
                    _state.value = HomeUiState.Error("Failed to load data: ${error.message}")
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }

    fun refreshData() {
       loadHomeData()
    }

    fun selectUniversity(universityId: String) {
        viewModelScope.launch {
            val result = runCatching {
                universityRepository.setActiveUniversity(universityId)
            }
            when {
                result.isFailure -> _event.send(HomeEvent.ShowErrorMessage("Failed to update active institution"))
            }
        }
    }

    fun onStartSessionClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToStartSession)
        }
    }

    fun onViewSessionHistoryClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToSessionHistory)
        }
    }
    fun onViewSessionHistoryDetailsClick(sessionId: String) {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToSessionHistoryDetails(sessionId))
        }
    }

    fun onExportDataClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToExport)
        }
    }

    fun onExportSessionAttendanceClick(sessionId: String) {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToExportSessionAttendance(sessionId))
        }
    }

    fun onCompleteSetupClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToSetup)
        }
    }

    fun onNotificationsClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToNotifications)
        }
    }

    fun onAddInstitutionClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToAddInstitution)
        }
    }

    fun onStudentLookupClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToStudentLookup)
        }
    }
}

