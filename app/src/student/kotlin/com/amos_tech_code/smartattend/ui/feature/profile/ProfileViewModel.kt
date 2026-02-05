package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.mappers.toEntity
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repositories.AccountRepository
import com.amos_tech_code.smartattend.data.repository.EnrollmentRepository
import com.amos_tech_code.smartattend.domain.models.StudentEnrollmentSource
import com.amos_tech_code.smartattend.domain.request.ProgrammeSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.StudentEnrollmentRequest
import com.amos_tech_code.smartattend.domain.request.UniversitySuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UpdateStudentProfileRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val session: ClassTrackSession,
    private val enrollmentRepository: EnrollmentRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileScreenState())
    val profileState = _profileState.asStateFlow()

    private val _event = Channel<ProfileEvent>()
    val events = _event.receiveAsFlow()

    private val enrollmentFlow = enrollmentRepository.getActiveEnrollmentFlow()

    init {
        loadInitialData()
        observeEnrollment()
    }

    private fun loadInitialData() {
        fetchProfileData()
    }

    private fun observeEnrollment() {
        viewModelScope.launch {
            enrollmentFlow.collect { enrollment ->
                enrollment?.let {
                    _profileState.update { state ->
                        state.copy(
                            student = state.student.copy(
                                name = it.fullName,
                                registrationNo = it.registrationNumber,
                                university = it.university.name,
                                programme = it.programme.name,
                                yearOfStudy = it.yearOfStudy,
                                semester = "Semester ${it.academicTerm.semester}"
                            ),
                            enrollment = it.toUiState()
                        )
                    }
                } ?: run {
                    _profileState.update { state ->
                        state.copy(
                            enrollment = null,
                            showEnrollmentPrompt = true
                        )
                    }
                }
            }
        }
    }

    fun fetchProfileData() {
        val studentName = session.getName()
        val registrationNo = session.getRegNo()

        _profileState.update {
            it.copy(
                student = Student(
                    name = studentName ?: "",
                    registrationNo = registrationNo ?: ""
                ),
                deviceInfo = DeviceInfoUiState(
                    deviceId = session.getDeviceId() ?: "",
                    deviceModel = session.getDeviceModel() ?: "",
                    isCurrentDevice = true
                )
            )
        }
    }

    fun syncEnrollment() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }

            val result = enrollmentRepository.syncActiveEnrollment()

            when (result) {
                is ApiResult.Success -> {
                    _event.trySend(ProfileEvent.ShowMessage("Enrollment synced successfully"))
                }
                is ApiResult.Failure -> {
                    val message = result.error.extractApiErrorMessage()
                    _event.trySend(ProfileEvent.ShowError(message))
                }
            }

            _profileState.update { it.copy(isLoading = false) }
        }
    }

    fun searchUniversities(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _profileState.update { it.copy(isLoading = true) }

                val result = enrollmentRepository.fetchMatchingUniversities(
                    UniversitySuggestionRequest(query = query)
                )

                when (result) {
                    is ApiResult.Success -> {
                        _profileState.update { state ->
                            state.copy(
                                universitySuggestions = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        _event.trySend(ProfileEvent.ShowError("Failed to search universities"))
                        _profileState.update { it.copy(isLoading = false) }
                    }
                }
            } else {
                _profileState.update { it.copy(universitySuggestions = emptyList()) }
            }
        }
    }

    fun searchProgrammes(
        universityId: String,
        query: String,
        departmentId: String? = null
    ) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _profileState.update { it.copy(isLoading = true) }

                val result = enrollmentRepository.fetchMatchingProgrammes(
                    ProgrammeSuggestionRequest(
                        universityId = universityId,
                        query = query,
                    )
                )

                when (result) {
                    is ApiResult.Success -> {
                        _profileState.update { state ->
                            state.copy(
                                programmeSuggestions = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        _event.trySend(ProfileEvent.ShowError("Failed to search programmes"))
                        _profileState.update { it.copy(isLoading = false) }
                    }
                }
            } else {
                _profileState.update { it.copy(programmeSuggestions = emptyList()) }
            }
        }
    }

    fun enrollStudent(
        universityId: String,
        universityName: String,
        programmeId: String,
        programmeName: String
    ) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }

            val request = StudentEnrollmentRequest(
                universityId = universityId,
                programmeId = programmeId,
                enrollmentSource = StudentEnrollmentSource.SELF
            )

            val result = enrollmentRepository.enroll(request)

            when (result) {
                is ApiResult.Success -> {
                    _event.trySend(ProfileEvent.ShowMessage("Successfully enrolled in $programmeName"))
                    _profileState.update { state ->
                        state.copy(
                            enrollment = result.data.toEntity().toUiState(),
                            showEnrollmentPrompt = false,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val message = result.error.extractApiErrorMessage()
                    _event.trySend(ProfileEvent.ShowError(message))
                    _profileState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun updateYearOfStudy(enrollmentId: String, newYear: Int) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }

            val result = enrollmentRepository.updateYear(enrollmentId, newYear)

            when (result) {
                is ApiResult.Success -> {
                    _event.trySend(ProfileEvent.ShowMessage("Year updated to Year $newYear"))
                    _profileState.update { state ->
                        state.copy(
                            enrollment = result.data.toEntity().toUiState(),
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val message = result.error.extractApiErrorMessage()
                    _event.trySend(ProfileEvent.ShowError(message))
                    _profileState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun deactivateEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }

            val result = enrollmentRepository.deactivateEnrollment(enrollmentId)

            when (result) {
                is ApiResult.Success -> {
                    _event.trySend(ProfileEvent.ShowMessage("Enrollment deactivated"))
                    _profileState.update { state ->
                        state.copy(
                            enrollment = null,
                            showEnrollmentPrompt = true,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val message = result.error.extractApiErrorMessage()
                    _event.trySend(ProfileEvent.ShowError(message))
                    _profileState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun updateProfile(request: UpdateStudentProfileRequest) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }

            val result = accountRepository.updateStudentProfile(request)

            when (result) {
                is ApiResult.Success -> {
                    _event.trySend(ProfileEvent.ShowMessage("Profile updated successfully"))
                    fetchProfileData()
                }
                is ApiResult.Failure -> {
                    val message = result.error.extractApiErrorMessage()
                    _event.trySend(ProfileEvent.ShowError(message))
                }
            }

            _profileState.update { it.copy(isLoading = false) }
        }
    }

    fun logOut() {
        _profileState.update {
            it.copy(isLoggingOut = true)
        }

        viewModelScope.launch {
            session.clearSession()
            _profileState.update {
                it.copy(isLoggingOut = false)
            }
            _event.trySend(ProfileEvent.NavigateToLogin)
        }
    }

    fun showEditProfile() {
        _event.trySend(ProfileEvent.ShowEditProfile)
    }

    fun showEnrollmentInfo() {
        _event.trySend(ProfileEvent.ShowEnrollmentInfo)
    }

    fun showEnrollmentSheet() {
        _event.trySend(ProfileEvent.ShowEnrollmentSheet)
    }

    fun showYearUpdateSheet() {
        _event.trySend(ProfileEvent.ShowYearUpdateSheet)
    }

    fun showDeactivateDialog() {
        _event.trySend(ProfileEvent.ShowDeactivateDialog)
    }

    fun clearSuggestions() {
        _profileState.update {
            it.copy(
                universitySuggestions = emptyList(),
                programmeSuggestions = emptyList()
            )
        }
    }
}