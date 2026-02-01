package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AccountRepository
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.domain.models.University
import com.amos_tech_code.smartattend.domain.request.UpdateLecturerProfileRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val session: ClassTrackProSession,
    private val accountRepository: AccountRepository,
    private val academicSetUpRepository: AcademicSetUpRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _event = Channel<ProfileEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadProfileData()
        observeLecturerName()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.ToggleAddInstitution -> {
                toggleAddInstitution()
            }

            is ProfileUiEvent.SelectInstitution -> {
                selectInstitution(event.institutionId)
            }

            ProfileUiEvent.RefreshData -> {
                loadProfileData()
            }

            ProfileUiEvent.ExportProfileData -> {
                exportProfileData()
            }

            ProfileUiEvent.EditProfile -> {
                // This now triggers the bottom sheet
                onEvent(ProfileUiEvent.ShowEditNameSheet)
            }

            // Handle new events
            ProfileUiEvent.ShowEditNameSheet -> {
                _state.update {
                    it.copy(
                        showEditNameSheet = true,
                        // Pre-fill the text field with the current name
                        editingName = it.lecturer.name
                    )
                }
            }
            ProfileUiEvent.ClearBottomSheetError -> {
                _state.update { it.copy(bottomSheetErrorMessage = null) }
            }
            ProfileUiEvent.HideEditNameSheet -> {
                _state.update { it.copy(showEditNameSheet = false) }
            }
            is ProfileUiEvent.OnEditingNameChanged -> {
                onProfileNameChanged(event.name)
            }
            ProfileUiEvent.SaveEditedName -> {
                updateProfile()
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val lecturer = loadLecturerData()
                val institutions = loadInstitutions()
                val teachingStats = academicSetUpRepository.getTeachingStatistics()

                val mappedInstitutions = institutions.map { inst ->
                    Institution(
                        id = inst.id,
                        name = inst.name,
                        isActive = inst.isActive
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        lecturer = lecturer,
                        institutions = mappedInstitutions,
                        selectedInstitution = if (mappedInstitutions.size == 1) mappedInstitutions.first() else mappedInstitutions.firstOrNull { inst -> inst.isActive },
                        teachingStats = TeachingStatisticsUiState(
                            totalCourses = teachingStats.totalCourses,
                            totalExpectedStudents = teachingStats.totalExpectedStudents,
                            currentSemester = teachingStats.currentSemester,
                            totalProgrammes = teachingStats.totalProgrammes,
                            totalDepartments = teachingStats.totalDepartments,
                            activeInstitution = teachingStats.activeInstitution,
                            isInstitutionActive = teachingStats.isInstitutionActive
                        )
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
        _event.trySend(ProfileEvent.NavigateToInstitutionSetUp)
    }

    private fun selectInstitution(institutionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSwitchingInstitution = true) }

            try {

                academicSetUpRepository.setActiveUniversity(institutionId)

                val updatedInstitutions = academicSetUpRepository.getUniversities()
                val mappedInstitutions = updatedInstitutions.map { inst ->
                    Institution(
                        id = inst.id,
                        name = inst.name,
                        isActive = inst.isActive
                    )
                }

                _state.update { state ->
                    state.copy(
                        institutions = mappedInstitutions,
                        selectedInstitution = mappedInstitutions.find { it.id == institutionId },
                        isSwitchingInstitution = false
                    )
                }

                _event.send(ProfileEvent.ShowSuccessMessage("Active institution updated successfully"))

            } catch (e: Exception) {
                _state.update { it.copy(isSwitchingInstitution = false) }
                _event.send(ProfileEvent.ShowErrorMessage("Failed to update institution"))
            }
        }
    }

    private fun onProfileNameChanged(newName: String) {
        val error = when {
            newName.isBlank() -> "Name cannot be empty"
            newName.length < 3 -> "Name must be at least 3 characters"
            newName.length > 50 -> "Name must be less than 50 characters"
            else -> null
        }

        _state.update {
            it.copy(
                editingName = newName,
                editingNameError = error
            )
        }
    }

    private fun updateProfile() {
        if (state.value.editingNameError != null) {
            // Don't proceed if there's an error
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingProfile = true) }
            try {
                val newName = state.value.editingName.trim()
                val result = accountRepository.updateLecturerProfile(UpdateLecturerProfileRequest(newName))
                when (result) {
                    is ApiResult.Success -> {
                        _state.update {
                            it.copy(
                                lecturer = it.lecturer.copy(name = newName),
                                showEditNameSheet = false
                            )
                        }
                        _event.trySend(ProfileEvent.ShowSuccessMessage(result.data.message))
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = when(val error = result.error) {
                            is ApiError.NetworkError ->
                                error.exception.message ?: "Network error occurred"
                            is ApiError.HttpError -> error.message
                            is ApiError.UnknownError -> "Failed to update profile"
                        }
                        _state.update { it.copy(bottomSheetErrorMessage = errorMessage) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(bottomSheetErrorMessage = "Failed to update profile") }
            } finally {
                _state.update { it.copy(isUpdatingProfile = false) }
            }
        }
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

    private fun loadLecturerData(): Lecturer {
        return Lecturer(
            name = session.getName() ?: "",
            email = session.getEmail() ?: "",
        )
    }

    private fun observeLecturerName() {
        session.getNameFlow()
            .onEach { name ->
                _state.update { current ->
                    current.copy(
                        lecturer = current.lecturer.copy(name = name)
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    private suspend fun loadInstitutions(): List<University> {
        return academicSetUpRepository.getUniversities()
    }

    fun logOut() {
        session.clearSession()
        _event.trySend(ProfileEvent.LogOut)
    }

}