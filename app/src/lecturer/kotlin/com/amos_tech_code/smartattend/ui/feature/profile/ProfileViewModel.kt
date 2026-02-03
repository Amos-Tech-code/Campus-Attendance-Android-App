package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AccountRepository
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.domain.request.UpdateLecturerProfileRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val session: ClassTrackProSession,
    private val accountRepository: AccountRepository,
    private val academicSetUpRepository: AcademicSetUpRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _event = Channel<ProfileEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadProfileData()
        observeLecturerData()
        observeInstitutions()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val lecturer = loadLecturerData()
                val institutions = academicSetUpRepository.getUniversities()
                val activeInstitution = institutions.find { it.isActive }

                _state.update {
                    it.copy(
                        isLoading = false,
                        lecturer = lecturer,
                        institutions = institutions,
                        activeInstitution = activeInstitution,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile: ${e.message}"
                    )
                }
                _event.send(ProfileEvent.ShowErrorMessage("Failed to load profile"))
            }
        }
    }

    private fun loadLecturerData(): Lecturer {
        return Lecturer(
            name = session.getName() ?: "",
            email = session.getEmail() ?: "",
            joinDate = session.getProfileCreatedAt()
        )
    }

    private fun observeLecturerData() {
        viewModelScope.launch {
            session.getNameFlow()
                .catch { _event.send(ProfileEvent.ShowErrorMessage("Failed to load profile")) }
                .collect { name ->
                    _state.update { current ->
                        current.copy(
                            lecturer = current.lecturer.copy(name = name)
                        )
                    }
                }
        }
    }

    private fun observeInstitutions() {
        viewModelScope.launch {
            academicSetUpRepository.observeUniversities()
                .catch {
                    _event.send(ProfileEvent.ShowErrorMessage("Failed to load data"))
                }
                .collect { institutions ->
                    val activeInstitution = institutions.find { it.isActive }
                    _state.update {
                        it.copy(
                            institutions = institutions,
                            activeInstitution = activeInstitution
                        )
                    }
                }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.ToggleAddInstitution -> {
                _event.trySend(ProfileEvent.NavigateToInstitutionSetUp)
            }

            is ProfileUiEvent.SelectInstitution -> {
                switchActiveInstitution(event.institutionId)
            }

            ProfileUiEvent.RefreshData -> {
                loadProfileData()
            }

            ProfileUiEvent.ExportProfileData -> {
                exportProfileData()
            }

            ProfileUiEvent.EditProfile -> {
                _state.update {
                    it.copy(
                        showEditNameSheet = true,
                        editingName = it.lecturer.name,
                        editingNameError = null,
                        bottomSheetErrorMessage = null
                    )
                }
            }

            ProfileUiEvent.ClearBottomSheetError -> {
                _state.update { it.copy(bottomSheetErrorMessage = null) }
            }

            ProfileUiEvent.HideEditNameSheet -> {
                _state.update {
                    it.copy(
                        showEditNameSheet = false,
                        editingNameError = null,
                        bottomSheetErrorMessage = null
                    )
                }
            }

            is ProfileUiEvent.OnEditingNameChanged -> {
                validateName(event.name)
            }

            ProfileUiEvent.SaveEditedName -> {
                updateProfileName()
            }

            ProfileUiEvent.LogOut -> {
                confirmLogout()
            }

            ProfileUiEvent.ManageNotifications -> {
                _event.trySend(ProfileEvent.NavigateToNotifications)
            }

            ProfileUiEvent.ManageSecurity -> {
                _event.trySend(ProfileEvent.NavigateToSecuritySettings)
            }

            ProfileUiEvent.ViewAppInfo -> {
                // Show app info dialog
                //_event.send(ProfileEvent.ShowSuccessMessage("App Version: ${_state.value.appVersion}"))
            }

            ProfileUiEvent.ManageData -> {
                _event.trySend(ProfileEvent.NavigateToDataManagement)
            }

            ProfileUiEvent.ManagePreferences -> {
                _event.trySend(ProfileEvent.NavigateToPreferences)
            }
        }
    }

    private fun validateName(newName: String) {
        val error = when {
            newName.isBlank() -> "Name cannot be empty"
            newName.length < 3 -> "Name must be at least 3 characters"
            newName.length > 50 -> "Name must be less than 50 characters"
            !newName.matches(Regex("^[\\p{L} .'-]+\$")) -> "Please enter a valid name"
            else -> null
        }

        _state.update {
            it.copy(
                editingName = newName,
                editingNameError = error
            )
        }
    }

    private fun switchActiveInstitution(institutionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSwitchingInstitution = true) }

            try {
                academicSetUpRepository.setActiveUniversity(institutionId)
                _state.update { it.copy(isSwitchingInstitution = false) }
                _event.send(ProfileEvent.ShowSuccessMessage("Institution switched successfully"))
            } catch (_: Exception) {
                _state.update { it.copy(isSwitchingInstitution = false) }
                _event.send(ProfileEvent.ShowErrorMessage("Failed to switch institution"))
            }
        }
    }

    private fun updateProfileName() {
        if (_state.value.editingNameError != null) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUpdatingProfile = true) }
            try {
                val newName = _state.value.editingName.trim()
                val result = accountRepository.updateLecturerProfile(
                    UpdateLecturerProfileRequest(newName)
                )

                when (result) {
                    is ApiResult.Success -> {
                        _state.update {
                            it.copy(
                                lecturer = it.lecturer.copy(name = newName),
                                showEditNameSheet = false,
                                isUpdatingProfile = false
                            )
                        }
                        _event.send(ProfileEvent.ShowSuccessMessage("Profile updated successfully"))
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = when(val error = result.error) {
                            is ApiError.NetworkError -> "Network error. Please check connection"
                            is ApiError.HttpError -> error.message
                            is ApiError.UnknownError -> "Failed to update profile"
                        }
                        _state.update {
                            it.copy(
                                bottomSheetErrorMessage = errorMessage,
                                isUpdatingProfile = false
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        bottomSheetErrorMessage = "Failed to update profile",
                        isUpdatingProfile = false
                    )
                }
            }
        }
    }

    private fun exportProfileData() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            try {
                // Export personal data
//                val exportData = PersonalDataExport(
//                    lecturer = _state.value.lecturer,
//                    institutions = _state.value.institutions,
//                    exportDate = System.currentTimeMillis()
//                )
//
//                // Create and share export file
//                val fileName = "SmartAttend_Profile_${System.currentTimeMillis()}.json"
//                _event.send(ProfileEvent.ExportData(exportData, fileName))

            } catch (_: Exception) {
                _event.send(ProfileEvent.ShowErrorMessage("Failed to export data"))
            } finally {
                _state.update { it.copy(isExporting = false) }
            }
        }
    }

    private fun confirmLogout() {
        viewModelScope.launch {
            session.clearSession()
            _event.send(ProfileEvent.LogOut)
        }
    }

    fun navigateToInstitutionDetail(institutionId: String) {
        viewModelScope.launch {
            _event.send(ProfileEvent.NavigateToInstitutionDetail(institutionId))
        }
    }
}