package com.amos_tech_code.smartattend.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.repositories.SessionRepository
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel (
    private val session: ClassTrackProSession,
    private val universityRepository: UniversityRepository,
    private val sessionHistoryRepository: SessionRepository
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
            // Check if profile is complete
            if (!session.isProfileComplete()) {
                _state.value = HomeUiState.NoInstitutionSetup
                _event.send(HomeEvent.NavigateToSetup)
                return@launch
            }

            // Combine all flows
            combine(
                universityRepository.observeAllUniversitiesWithStats(),
                sessionHistoryRepository.observeTodaysSessions(),
                //sessionHistoryRepository.observeRecentSessions(5)
            ) { universitiesWithStats, todaysSessions ->
                val activeUniversities = universitiesWithStats.filter { it.university.isActive }
                val lecturerName = session.getName() ?: "Lecturer"

                if (activeUniversities.isEmpty()) {
                    HomeUiState.NoInstitutionSetup
                } else {
                    // Auto-select first active university if none selected
                    selectedUniversityId = selectedUniversityId ?: activeUniversities.firstOrNull()?.university?.id

                    HomeUiState.SetupComplete(
                        lecturerName = lecturerName,
                        activeUniversities = activeUniversities,
                        todaysSessions = todaysSessions,
                        selectedUniversity = activeUniversities.find { it.university.id == selectedUniversityId }
                    )
                }
            }
                .catch { error ->
                    HomeUiState.Error("Failed to load data: ${error.message}")
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
        selectedUniversityId = universityId
        loadHomeData()
    }

    fun onStartSession() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToStartSession)
        }
    }

    fun onStudentLookup() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToStudentLookup)
        }
    }
    fun onViewSessionHistory() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToSessionHistory)
        }
    }
    fun onViewSessionHistoryDetails(sessionId: String) {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToSessionHistoryDetails(sessionId))
        }
    }

    fun onExportData() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToExport)
        }
    }

    fun onExportSessionAttendance(sessionId: String) {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToExportSessionAttendance(sessionId))
        }
    }

    fun onCompleteSetup() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToSetup)
        }
    }

    fun onNotificationsClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToNotifications)
        }
    }

    fun onSettingsClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToSettings)
        }
    }

    fun onStudentLookupClick() {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToStudentLookup)
        }
    }
}

