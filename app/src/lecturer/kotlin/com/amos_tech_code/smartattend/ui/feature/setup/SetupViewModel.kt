package com.amos_tech_code.smartattend.ui.feature.setup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AcademicSetUpRepository
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.request.AcademicSetUpRequest
import com.amos_tech_code.smartattend.domain.request.DepartmentSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.ProgrammeSetupRequest
import com.amos_tech_code.smartattend.domain.request.ProgrammeSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UnitSetupRequest
import com.amos_tech_code.smartattend.domain.request.UnitSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UniversitySuggestionRequest
import com.amos_tech_code.smartattend.domain.response.DepartmentSuggestion
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UnitSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.NeutralVariant50
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel(
    private val session: ClassTrackProSession,
    private val academicSetUpRepository: AcademicSetUpRepository
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val _events = Channel<SetupEvent>()
    val events = _events.receiveAsFlow()

    private var debounceJob: Job? = null

    fun onIntent(intent: SetupUiEvent) {
        when (intent) {
            is SetupUiEvent.UniversityNameChanged -> onUniversityNameChanged(intent.name)
            is SetupUiEvent.UniversitySelected -> onUniversitySelected(intent.suggestion)
            is SetupUiEvent.DepartmentNameChanged -> onDepartmentNameChanged(intent.programmeId, intent.name)
            is SetupUiEvent.DepartmentSelected -> onDepartmentSelected(intent.programmeId, intent.suggestion)
            is SetupUiEvent.AcademicYearChanged -> onAcademicYearChanged(intent.year)
            is SetupUiEvent.SemesterChanged -> onSemesterChanged(intent.semester)
            is SetupUiEvent.AddProgramme -> onAddProgramme()
            is SetupUiEvent.ProgrammeNameChanged -> onProgrammeNameChanged(intent.programmeId, intent.name)
            is SetupUiEvent.ProgrammeSelected -> onProgrammeSelected(intent.programmeId, intent.suggestion)
            is SetupUiEvent.OnYearOfStudyChanged -> onProgrammeYearOfStudyChange(intent.programmeId, intent.year)
            is SetupUiEvent.NoOfExpectedStudentsChanged -> onProgrammeStudentNoChange(intent.programmeId, intent.count)
            is SetupUiEvent.ToggleProgrammeExpanded -> onToggleProgrammeExpanded(intent.programmeId)
            is SetupUiEvent.RemoveProgramme -> onRemoveProgramme(intent.programmeId)
            is SetupUiEvent.ShowAddUnitForm -> onShowAddUnitForm(intent.programmeId)
            is SetupUiEvent.UnitCodeChanged -> onUnitCodeChanged(intent.code)
            is SetupUiEvent.UnitNameChanged -> onUnitNameChanged(intent.name)
            is SetupUiEvent.UnitLectureDayChanged -> onUnitLectureDayChanged(intent.day)
            is SetupUiEvent.UnitLectureTimeChanged -> onUnitLectureTimeChanged(intent.time)
            is SetupUiEvent.UnitLectureVenueChanged -> onUnitLectureVenueChanged(intent.venue)
            is SetupUiEvent.UnitSelected -> onUnitSelected(intent.suggestion)
            is SetupUiEvent.SaveUnit -> onSaveUnit()
            is SetupUiEvent.CancelAddUnit -> onCancelAddUnit()
            is SetupUiEvent.RemoveUnit -> onRemoveUnit(intent.programmeId, intent.unitId)
            is SetupUiEvent.CompleteSetup -> onCompleteSetup()
        }
    }

    // University actions
    fun onUniversityNameChanged(name: String) {
        _uiState.update { it.copy(universityName = name) }

        if (name.length < 2) {
            _uiState.update { it.copy(showUniversitySuggestions = false) }
            return
        }

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            if (name.length >= 2) {
                fetchUniversitySuggestions(name)
            }
        }
    }

    fun onUniversitySelected(suggestion: UniversitySuggestion) {
        _uiState.update {
            it.copy(
                universityName = suggestion.name,
                selectedUniversityId = suggestion.id,
                showUniversitySuggestions = false
            )
        }
        validateSetup()
    }

    // Department actions for specific programme
    fun onDepartmentNameChanged(programmeId: String, name: String) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(departmentName = name)
                    } else {
                        programme
                    }
                }
            )
        }

        val universityId = _uiState.value.selectedUniversityId
        if (name.length < 2 || universityId == null) return

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            fetchDepartmentSuggestions(programmeId, universityId, name)
        }
    }

    fun onDepartmentSelected(programmeId: String, suggestion: DepartmentSuggestion) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(
                            departmentName = suggestion.name,
                            selectedDepartmentId = suggestion.id,
                            showDepartmentSuggestions = false
                        )
                    } else {
                        programme
                    }
                }
            )
        }
    }

    // Academic info
    fun onAcademicYearChanged(year: String) {
        _uiState.update { it.copy(academicYear = year) }
        validateSetup()
    }

    fun onSemesterChanged(semester: Int) {
        _uiState.update { it.copy(selectedSemester = semester) }
    }

    // Programme actions
    fun onAddProgramme() {
        val newProgramme = ProgrammeUiState()
        _uiState.update {
            it.copy(
                programmes = it.programmes + newProgramme,
                activeProgrammeId = newProgramme.id
            )
        }
    }

    fun onProgrammeNameChanged(programmeId: String, name: String) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(name = name)
                    } else {
                        programme
                    }
                }
            )
        }

        if (name.length >= 2 && _uiState.value.selectedUniversityId != null) {
            viewModelScope.launch {
                delay(300)
                fetchProgrammeSuggestions(programmeId, name)
            }
        }
    }

    fun onProgrammeSelected(programmeId: String, suggestion: ProgrammeSuggestion) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(
                            name = suggestion.name,
                            selectedProgrammeId = suggestion.id,
                            showProgrammeSuggestions = false,
                            departmentName = suggestion.departmentName ?: "",
                            selectedDepartmentId = suggestion.departmentId
                        )
                    } else {
                        programme
                    }
                }
            )
        }
    }

    fun onProgrammeYearOfStudyChange(programmeId: String, year: Int) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(yearOfStudy = year)
                    } else {
                        programme
                    }
                }
            )
        }
    }

    private fun onProgrammeStudentNoChange(programmeId: String, count: String) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(expectedStudentCount = count)
                    } else programme
                }
            )
        }
    }
    fun onRemoveProgramme(programmeId: String) {
        _uiState.update {
            it.copy(
                programmes = it.programmes.filter { it.id != programmeId },
            )
        }
        validateSetup()
    }

    fun onToggleProgrammeExpanded(programmeId: String) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(isExpanded = !programme.isExpanded)
                    } else {
                        programme
                    }
                }
            )
        }
    }

    // Unit actions
    fun onShowAddUnitForm(programmeId: String) {
        _uiState.update {
            it.copy(
                showAddUnitForm = true,
                addUnitState = AddUnitState(programmeId = programmeId)
            )
        }
    }

    fun onUnitCodeChanged(unitCode: String) {
        _uiState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(unitCode = unitCode)
            )
        }

        if (unitCode.length >= 2 && _uiState.value.selectedUniversityId != null) {
            viewModelScope.launch {
                delay(300)
                fetchUnitSuggestions(unitCode)
            }
        }
    }

    fun onUnitNameChanged(unitName: String) {
        _uiState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(unitName = unitName)
            )
        }
    }

    fun onUnitLectureDayChanged(day: String) {
        _uiState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(lectureDay = day)
            )
        }
    }

    fun onUnitLectureTimeChanged(time: String) {
        _uiState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(lectureTime = time)
            )
        }
    }

    fun onUnitLectureVenueChanged(venue: String) {
        _uiState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(lectureVenue = venue)
            )
        }
    }

    fun onUnitSelected(suggestion: UnitSuggestion) {
        _uiState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(
                    unitCode = suggestion.code,
                    unitName = suggestion.name,
                    selectedUnitId = suggestion.id,
                    showUnitSuggestions = false
                )
            )
        }
    }

    fun onSaveUnit() {
        val unitState = _uiState.value.addUnitState
        val errors = mutableListOf<String>()

        if (unitState.unitCode.isBlank()) errors.add("Unit code is required")
        if (unitState.unitName.isBlank()) errors.add("Unit name is required")

        if (errors.isNotEmpty()) {
            viewModelScope.launch {
                _events.send(SetupEvent.ShowErrorMessage(errors.joinToString(", ")))
            }
            return
        }

        val newUnit = UnitUiState(
            code = unitState.unitCode,
            name = unitState.unitName,
            selectedUnitId = unitState.selectedUnitId,
            lectureDay = unitState.lectureDay,
            lectureTime = unitState.lectureTime,
            lectureVenue = unitState.lectureVenue
        )

        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == unitState.programmeId) {
                        programme.copy(units = programme.units + newUnit)
                    } else {
                        programme
                    }
                },
                showAddUnitForm = false,
                addUnitState = AddUnitState()
            )
        }
        validateSetup()
        viewModelScope.launch {
            _events.send(SetupEvent.ShowSuccessMessage("Unit added successfully"))
        }
    }

    fun onCancelAddUnit() {
        _uiState.update {
            it.copy(
                showAddUnitForm = false,
                addUnitState = AddUnitState()
            )
        }
    }

    fun onRemoveUnit(programmeId: String, unitId: String) {
        _uiState.update { state ->
            state.copy(
                programmes = state.programmes.map { programme ->
                    if (programme.id == programmeId) {
                        programme.copy(units = programme.units.filter { it.id != unitId })
                    } else {
                        programme
                    }
                }
            )
        }
        validateSetup()
    }

    // Setup completion
    fun onCompleteSetup() {
        if (!_uiState.value.isSetupValid) {
            viewModelScope.launch {
                _events.send(SetupEvent.ShowErrorMessage("Please complete all required fields"))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val request = createAcademicSetupRequest()
                val result = academicSetUpRepository.uploadAcademicSetUp(request)

                when(result) {
                    is ApiResult.Success -> {

                        _uiState.update { it.copy(isLoading = false) }
                        session.setSetupComplete(true)
                        _events.send(SetupEvent.SetupComplete)
                        _events.send(SetupEvent.ShowSuccessMessage("Academic setup completed successfully!"))
                    }
                    is ApiResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false) }
                        val errorMessage = when(result.error) {
                            is ApiError.NetworkError -> "Network error. Please check your connection."
                            is ApiError.HttpError -> "Server error: ${result.error.message}"
                            is ApiError.UnknownError -> "An unexpected error occurred"
                        }
                        _events.send(SetupEvent.ShowErrorMessage(errorMessage))
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(SetupEvent.ShowErrorMessage("Failed to save setup: ${e.message}"))
            }
        }
    }

    // Helper methods
    private fun createAcademicSetupRequest(): AcademicSetUpRequest {
        val state = _uiState.value
        return AcademicSetUpRequest(
            universityId = state.selectedUniversityId,
            universityName = state.universityName,
            academicYear = state.academicYear,
            semester = state.selectedSemester,
            programmes = state.programmes.map { programme ->
                ProgrammeSetupRequest(
                    programmeId = programme.selectedProgrammeId,
                    programmeName = programme.name,
                    departmentId = programme.selectedDepartmentId,
                    departmentName = programme.departmentName,
                    yearOfStudy = programme.yearOfStudy,
                    expectedStudentCount = programme.expectedStudentCount.toInt(),
                    units = programme.units.map { unit ->
                        UnitSetupRequest(
                            unitId = unit.selectedUnitId,
                            code = unit.code,
                            name = unit.name,
                            semester = state.selectedSemester,
                            lectureDay = unit.lectureDay.takeIf { it.isNotBlank() },
                            lectureTime = unit.lectureTime.takeIf { it.isNotBlank() },
                            lectureVenue = unit.lectureVenue.takeIf { it.isNotBlank() }
                        )
                    }
                )
            }
        )
    }

    private suspend fun fetchUniversitySuggestions(query: String) {
        _uiState.update { it.copy(isLoadingSuggestions = true) }

        val result = academicSetUpRepository.fetchMatchingUniversities(
            UniversitySuggestionRequest(query = query)
        )

        when(result) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(
                        universitySuggestions = result.data,
                        showUniversitySuggestions = result.data.isNotEmpty(),
                        isLoadingSuggestions = false
                    )
                }
            }
            is ApiResult.Failure -> {
                _uiState.update {
                    it.copy(
                        showUniversitySuggestions = false,
                        isLoadingSuggestions = false
                    )
                }
            }
        }
    }

    private suspend fun fetchDepartmentSuggestions(programmeId: String, universityId: String, query: String) {
        val result = academicSetUpRepository.fetchMatchingDepartments(
            DepartmentSuggestionRequest(universityId = universityId, query = query)
        )

        when(result) {
            is ApiResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        programmes = state.programmes.map { programme ->
                            if (programme.id == programmeId) {
                                programme.copy(
                                    departmentSuggestions = result.data,
                                    showDepartmentSuggestions = result.data.isNotEmpty()
                                )
                            } else {
                                programme
                            }
                        }
                    )
                }
            }
            is ApiResult.Failure -> {
                // Handle error silently for suggestions
            }
        }
    }

    private suspend fun fetchProgrammeSuggestions(programmeId: String, query: String) {
        val universityId = _uiState.value.selectedUniversityId ?: return

        val result = academicSetUpRepository.fetchMatchingProgrammes(
            ProgrammeSuggestionRequest(
                universityId = universityId,
                query = query,
            )
        )

        when(result) {
            is ApiResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        programmes = state.programmes.map { programme ->
                            if (programme.id == programmeId) {
                                programme.copy(
                                    programmeSuggestions = result.data,
                                    showProgrammeSuggestions = result.data.isNotEmpty()
                                )
                            } else {
                                programme
                            }
                        }
                    )
                }
            }
            is ApiResult.Failure -> {
                // Handle error silently for suggestions
            }
        }
    }

    private suspend fun fetchUnitSuggestions(query: String) {
        val universityId = _uiState.value.selectedUniversityId ?: return

        // Get the current programme ID for context
        val programmeId = _uiState.value.addUnitState.programmeId
        val programme = _uiState.value.programmes.find { it.id == programmeId }
        val departmentId = programme?.selectedDepartmentId

        _uiState.update { it.copy(isLoadingSuggestions = true) }

        val result = academicSetUpRepository.fetchMatchingUnits(
            UnitSuggestionRequest(
                universityId = universityId,
                departmentId = departmentId,
                programmeId = programme?.selectedProgrammeId,
                query = query
            )
        )

        when(result) {
            is ApiResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        addUnitState = state.addUnitState.copy(
                            unitSuggestions = result.data,
                            showUnitSuggestions = result.data.isNotEmpty()
                        ),
                        isLoadingSuggestions = false
                    )
                }
            }
            is ApiResult.Failure -> {
                _uiState.update { state ->
                    state.copy(
                        addUnitState = state.addUnitState.copy(
                            showUnitSuggestions = false
                        ),
                        isLoadingSuggestions = false
                    )
                }
            }
        }
    }

    private fun validateSetup() {
        val state = _uiState.value
        val isValid = state.universityName.isNotBlank() &&
                state.academicYear.isNotBlank() &&
                state.programmes.isNotEmpty() &&
                state.programmes.all { programme ->
                    programme.name.isNotBlank() &&
                            programme.expectedStudentCount.isNotBlank() &&
                            programme.departmentName.isNotBlank() &&
                            programme.units.isNotEmpty() &&
                            programme.units.all { unit ->
                                unit.code.isNotBlank() && unit.name.isNotBlank()
                            }
                }

        _uiState.update { it.copy(isSetupValid = isValid) }
    }

}


// -------------------------------------- //

data class LecturerSession(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val sessionCode: String,
    val time: String,
    val location: String,
    val status: SessionStatus,
    val lecturer: String
)

enum class SessionStatus {
    ACTIVE, UPCOMING, COMPLETED, MISSED;

    val displayName: String
        get() = when (this) {
            ACTIVE -> "Active Now"
            UPCOMING -> "Upcoming"
            COMPLETED -> "Completed"
            MISSED -> "Missed"
        }
}

data class LecturerActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: String,
    val courseName: String? = null,
    val status: ActivityStatus = ActivityStatus.INFO
)

enum class ActivityType {
    ATTENDANCE_MARKED, ATTENDANCE_FAILED, NEW_SESSION, DEVICE_CHANGE, SYSTEM_ALERT
}

enum class ActivityStatus {
    SUCCESS, WARNING, ERROR, INFO
}

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, PENDING
}

data class Student(
    val name: String = "",
    val registrationNo: String = "",
    val email: String = "",
    val department: String = "",
    val semester: String = "",
    val profileImage: String? = null
)


data class AttendanceRecord(
    val id: String,
    val sessionId: String,
    val courseName: String,
    val courseCode: String,
    val date: String,
    val time: String,
    val status: AttendanceStatus,
    val method: AttendanceMethod,
    val location: String? = null,
    val distance: Int? = null,
    val deviceVerified: Boolean = true,
    val locationVerified: Boolean = true,
    val verified: Boolean = false,
    val lecturerName: String = "",
    val sessionDuration: String = "60 min"
) {
    // Helper property for display
    val displayDateTime: String
        get() = "$date • $time"

    // Helper property for status color
    val statusColor: Color
        get() = when (status) {
            AttendanceStatus.PRESENT -> PresentColor
            AttendanceStatus.ABSENT -> AbsentColor
            AttendanceStatus.LATE -> PendingColor
            AttendanceStatus.PENDING -> NeutralVariant50
        }

    // Helper property for status icon
    val statusIcon: ImageVector
        get() = when (status) {
            AttendanceStatus.PRESENT -> Icons.Default.CheckCircle
            AttendanceStatus.ABSENT -> Icons.Default.Cancel
            AttendanceStatus.LATE -> Icons.Default.Schedule
            AttendanceStatus.PENDING -> Icons.Default.Pending
        }
}