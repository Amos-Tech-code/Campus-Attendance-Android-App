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
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AcademicSetUpRepository
import com.amos_tech_code.smartattend.domain.request.AcademicSetupUpRequest
import com.amos_tech_code.smartattend.domain.request.ProgrammeRequest
import com.amos_tech_code.smartattend.domain.request.UnitRequest
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.NeutralVariant50
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel(
    private val session: SmartAttendSession,
    private val academicSetUpRepository: AcademicSetUpRepository
) : ViewModel() {

    private val _setupState = MutableStateFlow(SetupState())
    val setupState: StateFlow<SetupState> = _setupState

    private val _event = Channel<SetUpEvents>()
    val event = _event.receiveAsFlow()

    fun onUniversityNameChange(name: String) {
        _setupState.update { state ->
            state.copy(
                universityName = name,
                universityNameError = if (name.isBlank()) "University name is required" else null
            )
        }
        validateSetup()
    }

    fun onProgrammeNameChange(name: String) {
        _setupState.update { state ->
            state.copy(
                addProgrammeState = state.addProgrammeState.copy(
                    programmeName = name,
                    programmeNameError = if (name.isBlank()) "Programme name is required" else null
                )
            )
        }
    }

    fun onDepartmentChange(department: String) {
        _setupState.update { state ->
            state.copy(
                addProgrammeState = state.addProgrammeState.copy(
                    department = department,
                    departmentError = if (department.isBlank()) "Department is required" else null
                )
            )
        }
    }

    fun onYearOfStudyChange(year: String) {
        _setupState.update { state ->
            state.copy(
                addProgrammeState = state.addProgrammeState.copy(
                    yearOfStudy = year,
                    yearOfStudyError = if (year.isBlank() || year.toInt() !in 1..6 || !year.matches(Regex("\\d+"))) "Valid year of study is required" else null
                )
            )
        }
    }

    fun onUnitNameChange(name: String) {
        _setupState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(
                    unitName = name,
                    unitNameError = if (name.isBlank()) "Unit name is required" else null
                )
            )
        }
    }

    fun onUnitCodeChange(code: String) {
        _setupState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(
                    unitCode = code,
                    unitCodeError = if (code.isBlank()) "Unit code is required" else null
                )
            )
        }
    }

    fun onSelectedProgrammeChange(programmeId: String) {
        val programme = _setupState.value.programmes.find { it.id == programmeId }
        _setupState.update { state ->
            state.copy(
                addUnitState = state.addUnitState.copy(
                    selectedProgrammeId = programmeId,
                    selectedProgrammeName = programme?.name ?: "",
                    selectedProgrammeError = if (programmeId.isBlank()) "Please select a programme" else null
                )
            )
        }
    }

    fun onShowAddProgrammeForm() {
        _setupState.update { state ->
            state.copy(
                showAddProgrammeForm = true,
                showAddUnitForm = false,
                addProgrammeState = AddProgrammeState() // Reset form
            )
        }
    }

    fun onShowAddUnitForm(programmeId: String? = null) {
        val initialState = if (programmeId != null) {
            val programme = _setupState.value.programmes.find { it.id == programmeId }
            AddUnitState(
                selectedProgrammeId = programmeId,
                selectedProgrammeName = programme?.name ?: ""
            )
        } else {
            AddUnitState()
        }

        _setupState.update { state ->
            state.copy(
                showAddUnitForm = true,
                showAddProgrammeForm = false,
                addUnitState = initialState
            )
        }
    }

    fun onCancelAddUnit() {
        _setupState.update { state ->
            state.copy(
                showAddUnitForm = false,
                addUnitState = AddUnitState()
            )
        }
    }

    fun onSaveProgramme() {
        val programmeState = _setupState.value.addProgrammeState

        // Validate programme form
        val errors = mutableListOf<String>()
        if (programmeState.programmeName.isBlank()) errors.add("Programme name is required")
        if (programmeState.department.isBlank()) errors.add("Department is required")
        if (programmeState.yearOfStudy.isBlank() || programmeState.yearOfStudy.toInt() !in 1..6 || !programmeState.yearOfStudy.matches(Regex("\\d+"))) {
            errors.add("Valid year of study is required")
        }

        if (errors.isNotEmpty()) {
            viewModelScope.launch {
                _event.send(SetUpEvents.ShowErrorMessage(errors.joinToString(", ")))
            }
            return
        }

        val editingProgrammeId = _setupState.value.editingProgrammeId

        if (editingProgrammeId != null) {
            // Update existing programme
            _setupState.update { state ->
                state.copy(
                    programmes = state.programmes.map { programme ->
                        if (programme.id == editingProgrammeId) {
                            programme.copy(
                                name = programmeState.programmeName,
                                department = programmeState.department,
                                yearOfStudy = programmeState.yearOfStudy.toInt()
                            )
                        } else {
                            programme
                        }
                    },
                    showAddProgrammeForm = false,
                    addProgrammeState = AddProgrammeState(),
                    editingProgrammeId = null
                )
            }
        } else {
            // Add new programme
            val newProgramme = Programme(
                id = System.currentTimeMillis().toString(),
                name = programmeState.programmeName,
                department = programmeState.department,
                yearOfStudy = programmeState.yearOfStudy.toInt(),
                units = emptyList()
            )

            _setupState.update { state ->
                state.copy(
                    programmes = state.programmes + newProgramme,
                    showAddProgrammeForm = false,
                    addProgrammeState = AddProgrammeState()
                )
            }
        }
        validateSetup()
    }

    fun onCancelAddProgramme() {
        _setupState.update { state ->
            state.copy(
                showAddProgrammeForm = false,
                addProgrammeState = AddProgrammeState(),
                editingProgrammeId = null
            )
        }
    }

    fun onSaveUnit() {
        val unitState = _setupState.value.addUnitState

        // Validate unit form
        val errors = mutableListOf<String>()
        if (unitState.selectedProgrammeId.isBlank()) errors.add("Please select a programme")
        if (unitState.unitName.isBlank()) errors.add("Unit name is required")
        if (unitState.unitCode.isBlank()) errors.add("Unit code is required")

        if (errors.isNotEmpty()) {
            viewModelScope.launch {
                _event.send(SetUpEvents.ShowErrorMessage(errors.joinToString(", ")))
            }
            return
        }

        val newUnit = TeachingUnit(
            id = System.currentTimeMillis().toString(),
            code = unitState.unitCode,
            name = unitState.unitName,
            programmeId = unitState.selectedProgrammeId
        )

        _setupState.update { state ->
            val updatedProgrammes = state.programmes.map { programme ->
                if (programme.id == unitState.selectedProgrammeId) {
                    programme.copy(units = programme.units + newUnit)
                } else {
                    programme
                }
            }
            state.copy(
                programmes = updatedProgrammes,
                showAddUnitForm = false,
                addUnitState = AddUnitState()
            )
        }
        validateSetup()
    }

    fun onEditProgramme(programmeId: String) {
        val programme = _setupState.value.programmes.find { it.id == programmeId }
        programme?.let {
            _setupState.update { state ->
                state.copy(
                    showAddProgrammeForm = true,
                    showAddUnitForm = false,
                    addProgrammeState = AddProgrammeState(
                        programmeName = it.name,
                        department = it.department,
                        yearOfStudy = it.yearOfStudy.toString()
                    ),
                    editingProgrammeId = programmeId
                )
            }
        }
    }

    fun onDeleteProgramme(programmeId: String) {
        _setupState.update { state ->
            state.copy(
                programmes = state.programmes.filter { it.id != programmeId }
            )
        }
        validateSetup()
    }

    fun onDeleteUnit(programmeId: String, unitId: String) {
        _setupState.update { state ->
            val updatedProgrammes = state.programmes.map { programme ->
                if (programme.id == programmeId) {
                    programme.copy(units = programme.units.filter { it.id != unitId })
                } else {
                    programme
                }
            }
            state.copy(programmes = updatedProgrammes)
        }
        validateSetup()
    }

    fun onCompleteSetup() {
        if (!_setupState.value.isSetupValid) {
            viewModelScope.launch {
                _event.send(SetUpEvents.ShowErrorMessage("Please complete all required fields"))
            }
            return
        }

        _setupState.update { it -> it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = academicSetUpRepository.uploadAcademicSetUp(
                    AcademicSetupUpRequest(
                        universityName = _setupState.value.universityName,
                        programmes = _setupState.value.programmes.map { programme ->
                            ProgrammeRequest(
                                name = programme.name,
                                department = programme.department,
                                yearOfStudy = programme.yearOfStudy,
                                units = programme.units.map { unit ->
                                    UnitRequest(
                                        code = unit.code,
                                        name = unit.name
                                    )
                                }
                            )
                        }
                    )
                )

                when(result) {
                    is ApiResult.Success -> {
                        // Launch independent sync
                        launch(SupervisorJob() + Dispatchers.IO) {
                            academicSetUpRepository.syncLecturerAcademics()
                        }
                        _setupState.update { it.copy(isLoading = false) }
                        session.setSetupComplete(true)
                        _event.send(SetUpEvents.SetupComplete)
                    }
                    is ApiResult.Failure -> {
                        _setupState.update { it.copy(isLoading = false) }
                        when(result.error) {
                            is ApiError.NetworkError -> {
                                _event.send(SetUpEvents.ShowErrorMessage("Network error: ${result.error.exception.message}"))
                            }
                            is ApiError.HttpError -> {
                                _event.send(SetUpEvents.ShowErrorMessage("HTTP error: ${result.error.message}"))
                            }
                            is ApiError.UnknownError -> {
                                _event.send(SetUpEvents.ShowErrorMessage("Unknown error: ${result.error.throwable.message}"))
                            }

                        }
                    }
                }
            } catch (e: Exception) {
                _setupState.update { it -> it.copy(isLoading = false) }
                _event.send(SetUpEvents.ShowErrorMessage("Failed to save setup: ${e.message}"))
            }

        }


    }

    private fun validateSetup() {
        val state = _setupState.value
        val isValid = state.universityName.isNotBlank() &&
                state.programmes.isNotEmpty() &&
                state.programmes.any { it.units.isNotEmpty() }

        _setupState.update { it.copy(isSetupValid = isValid) }
    }

}







// -------------------------------------- //

data class Session(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val sessionCode: String,
    val time: String,
    val location: String,
    val status: SessionStatus,
    val lecturer: String
)

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


data class Course(
    val id: String,
    val name: String,
    val code: String,
    val description: String? = null
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

enum class AttendanceMethod {
    QR_CODE, MANUAL_CODE, GPS, LECTURER_MANUAL
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