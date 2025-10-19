package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _event = Channel<ProfileEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadProfileData()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.ToggleAddInstitution -> {
                toggleAddInstitution()
            }

            is ProfileUiEvent.NewInstitutionNameChanged -> {
                updateNewInstitutionName(event.name)
            }

            is ProfileUiEvent.NewInstitutionDepartmentChanged -> {
                updateNewInstitutionDepartment(event.department)
            }

            is ProfileUiEvent.NewInstitutionCampusChanged -> {
                updateNewInstitutionCampus(event.campus)
            }

            ProfileUiEvent.SaveNewInstitution -> {
                saveNewInstitution()
            }

            ProfileUiEvent.CancelAddInstitution -> {
                cancelAddInstitution()
            }

            is ProfileUiEvent.SelectInstitution -> {
                selectInstitution(event.institutionId)
            }

            ProfileUiEvent.EditProfile -> {
                editProfile()
            }

            ProfileUiEvent.RefreshData -> {
                loadProfileData()
            }

            ProfileUiEvent.ExportProfileData -> {
                exportProfileData()
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Simulate API calls
                delay(1200)

                val lecturer = loadLecturerData()
                val institutions = loadInstitutions()
                val teachingStats = loadTeachingStatistics()

                _state.update {
                    it.copy(
                        isLoading = false,
                        lecturer = lecturer,
                        institutions = institutions,
                        selectedInstitution = institutions.firstOrNull { inst -> inst.isActive },
                        teachingStats = teachingStats
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile data: ${e.message}"
                    )
                }
                _event.send(ProfileEvent.ShowErrorMessage("Failed to load profile data"))
            }
        }
    }

    private fun toggleAddInstitution() {
        _state.update { it.copy(showAddInstitution = !it.showAddInstitution) }
    }

    private fun updateNewInstitutionName(name: String) {
        _state.update { state ->
            state.copy(
                newInstitutionState = state.newInstitutionState.copy(
                    name = name,
                    nameError = if (name.isBlank()) "Institution name is required" else null
                )
            )
        }
    }

    private fun updateNewInstitutionDepartment(department: String) {
        _state.update { state ->
            state.copy(
                newInstitutionState = state.newInstitutionState.copy(
                    department = department,
                    departmentError = if (department.isBlank()) "Department is required" else null
                )
            )
        }
    }

    private fun updateNewInstitutionCampus(campus: String) {
        _state.update { state ->
            state.copy(
                newInstitutionState = state.newInstitutionState.copy(
                    campus = campus
                )
            )
        }
    }

    private fun saveNewInstitution() {
        viewModelScope.launch {
            val newInstitutionState = _state.value.newInstitutionState

            // Validate inputs
            if (newInstitutionState.name.isBlank() || newInstitutionState.department.isBlank()) {
                _state.update { state ->
                    state.copy(
                        newInstitutionState = state.newInstitutionState.copy(
                            nameError = if (newInstitutionState.name.isBlank()) "Institution name is required" else null,
                            departmentError = if (newInstitutionState.department.isBlank()) "Department is required" else null
                        )
                    )
                }
                return@launch
            }

            _state.update { state ->
                state.copy(
                    newInstitutionState = state.newInstitutionState.copy(isLoading = true),
                    isSavingInstitution = true
                )
            }

            try {
                // Simulate API call
                delay(1000)

                val newInstitution = Institution(
                    id = "inst_${System.currentTimeMillis()}",
                    name = newInstitutionState.name,
                    department = newInstitutionState.department,
                    campus = newInstitutionState.campus.ifBlank { "Main Campus" },
                    isActive = false
                )

                val updatedInstitutions = _state.value.institutions + newInstitution

                _state.update { state ->
                    state.copy(
                        institutions = updatedInstitutions,
                        showAddInstitution = false,
                        newInstitutionState = NewInstitutionState(),
                        isSavingInstitution = false
                    )
                }

                _event.send(ProfileEvent.InstitutionUpdated)
                _event.send(ProfileEvent.ShowSuccessMessage("Institution added successfully"))

            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        newInstitutionState = state.newInstitutionState.copy(isLoading = false),
                        isSavingInstitution = false
                    )
                }
                _event.send(ProfileEvent.ShowErrorMessage("Failed to add institution: ${e.message}"))
            }
        }
    }

    private fun cancelAddInstitution() {
        _state.update { state ->
            state.copy(
                showAddInstitution = false,
                newInstitutionState = NewInstitutionState()
            )
        }
    }

    private fun selectInstitution(institutionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Simulate API call to set active institution
                delay(800)

                val updatedInstitutions = _state.value.institutions.map { institution ->
                    institution.copy(isActive = institution.id == institutionId)
                }

                _state.update { state ->
                    state.copy(
                        institutions = updatedInstitutions,
                        selectedInstitution = updatedInstitutions.find { it.id == institutionId },
                        isLoading = false
                    )
                }

                _event.send(ProfileEvent.InstitutionUpdated)
                _event.send(ProfileEvent.ShowSuccessMessage("Active institution updated"))

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _event.send(ProfileEvent.ShowErrorMessage("Failed to update institution"))
            }
        }
    }

    private fun editProfile() {
        // Navigate to edit profile screen
        _event.trySend(ProfileEvent.NavigateToEditProfile)
    }

    private fun exportProfileData() {
        viewModelScope.launch {
            try {
                // Simulate export process
                delay(1500)
                _event.send(ProfileEvent.ShowSuccessMessage("Profile data exported successfully"))

            } catch (e: Exception) {
                _event.send(ProfileEvent.ShowErrorMessage("Failed to export profile data"))
            }
        }
    }

    // Mock data loaders
    private suspend fun loadLecturerData(): Lecturer {
        return Lecturer(
            name = "Dr. Sarah Johnson",
            email = "sarah.johnson@university.edu",
            institution = "University of Technology",
            department = "Computer Science",
            staffId = "CS-2021-045",
            officeLocation = "Room 301, CS Building",
            profileImage = null,
            joinDate = "2021-08-15"
        )
    }

    private suspend fun loadInstitutions(): List<Institution> {
        return listOf(
            Institution(
                id = "inst_1",
                name = "University of Technology",
                department = "Computer Science",
                campus = "Main Campus",
                isActive = true
            ),
            Institution(
                id = "inst_2",
                name = "City College",
                department = "Software Engineering",
                campus = "Downtown Campus",
                isActive = false
            )
        )
    }

    private suspend fun loadTeachingStatistics(): TeachingStatistics {
        return TeachingStatistics(
            totalCourses = 8,
            totalStudents = 245,
            totalSessions = 156,
            averageAttendance = 87.5f,
            currentSemester = "Spring 2024",
            teachingSince = "2021"
        )
    }
}