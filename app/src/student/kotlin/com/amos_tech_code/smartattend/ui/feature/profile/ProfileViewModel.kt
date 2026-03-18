package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.mappers.toEntity
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.AccountRepository
import com.amos_tech_code.smartattend.data.repository.EnrollmentRepository
import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import com.amos_tech_code.smartattend.domain.models.StudentEnrollmentSource
import com.amos_tech_code.smartattend.domain.request.ProgrammeSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.StudentEnrollmentRequest
import com.amos_tech_code.smartattend.domain.request.UniversitySuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UpdateStudentProfileRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
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

    // Search debouncers
    private val universitySearchDebounce = MutableSharedFlow<String>()
    private val programmeSearchDebounce = MutableSharedFlow<Pair<String, String>>()

    init {
        loadInitialData()
        if (!session.isEnrolmentSynced()) {
            syncEnrollment()
        }
        observeEnrollment()
        setupUniversitySearchDebounce()
        setupProgrammeSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupUniversitySearchDebounce() {
        viewModelScope.launch {
            universitySearchDebounce
                .debounce(500) // 500ms debounce
                .distinctUntilChanged()
                .collect { query ->
                    performUniversitySearch(query)
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupProgrammeSearchDebounce() {
        viewModelScope.launch {
            programmeSearchDebounce
                .debounce(500) // 500ms debounce
                .distinctUntilChanged()
                .collect { (universityId, query) ->
                    performProgrammeSearch(universityId, query)
                }
        }
    }

    fun searchUniversities(query: String) {
        // Clear suggestions if query is too short
        if (query.length < 2) {
            _profileState.update {
                it.copy(
                    universitySuggestions = emptyList(),
                    isSearching = false
                )
            }
            return
        }

        // Update searching state
        _profileState.update { it.copy(isSearching = true) }

        // Emit to debounced flow
        viewModelScope.launch {
            universitySearchDebounce.emit(query)
        }
    }

    private fun performUniversitySearch(query: String) {
        viewModelScope.launch {
            val result = enrollmentRepository.fetchMatchingUniversities(
                UniversitySuggestionRequest(query = query)
            )

            when (result) {
                is ApiResult.Success -> {
                    _profileState.update { state ->
                        state.copy(
                            universitySuggestions = result.data,
                            isSearching = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _event.trySend(ProfileEvent.ShowError("Failed to search universities"))
                    _profileState.update { it.copy(isSearching = false) }
                }
            }
        }
    }

    fun searchProgrammes(
        universityId: String,
        query: String
    ) {
        // Clear suggestions if query is too short
        if (query.length < 2) {
            _profileState.update {
                it.copy(
                    programmeSuggestions = emptyList(),
                    isSearching = false
                )
            }
            return
        }

        // Update searching state
        _profileState.update { it.copy(isSearching = true) }

        // Emit to debounced flow
        viewModelScope.launch {
            programmeSearchDebounce.emit(universityId to query)
        }
    }

    private fun performProgrammeSearch(
        universityId: String,
        query: String
    ) {
        viewModelScope.launch {
            val result = enrollmentRepository.fetchMatchingProgrammes(
                ProgrammeSuggestionRequest(
                    universityId = universityId,
                    query = query
                )
            )

            when (result) {
                is ApiResult.Success -> {
                    _profileState.update { state ->
                        state.copy(
                            programmeSuggestions = result.data,
                            isSearching = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _event.trySend(ProfileEvent.ShowError("Failed to search programmes"))
                    _profileState.update { it.copy(isSearching = false) }
                }
            }
        }
    }

    // Clear search state when sheet is dismissed
    fun clearSearchState() {
        _profileState.update {
            it.copy(
                universitySuggestions = emptyList(),
                programmeSuggestions = emptyList(),
                isSearching = false
            )
        }
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
        val deviceStatus = session.getDeviceStatus()

        _profileState.update {
            it.copy(
                student = Student(
                    name = studentName ?: "",
                    registrationNo = registrationNo ?: ""
                ),
                deviceInfo = DeviceInfoUiState(
                    deviceId = session.getDeviceId() ?: "",
                    deviceModel = session.getDeviceModel() ?: "",
                    isRegisteredDevice = deviceStatus == DeviceStatus.ACTIVE
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

    fun updateYearOfStudy(newYear: Int) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }

            val enrollmentId = _profileState.value.enrollment?.enrollmentId
                ?: return@launch _event.send(ProfileEvent.ShowError("Enrollment not found"))

            val result = enrollmentRepository.updateYear(enrollmentId, newYear)

            when (result) {
                is ApiResult.Success -> {
                    _event.send(ProfileEvent.ShowMessage("Year updated to Year $newYear"))
                    _profileState.update { state ->
                        state.copy(
                            enrollment = result.data.toEntity().toUiState(),
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val message = result.error.extractApiErrorMessage()
                    _event.send(ProfileEvent.ShowError(message))
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
                    _event.send(ProfileEvent.ShowMessage("Enrollment deactivated"))
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
                    _event.send(ProfileEvent.ShowError(message))
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
            accountRepository.logOut()
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