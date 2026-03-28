package com.amos_tech_code.smartattend.ui.feature.institution_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import com.amos_tech_code.smartattend.domain.request.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InstitutionDetailsViewModel(
    private val academicSetUpRepository: AcademicSetUpRepository,
    private val universityRepository: UniversityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InstitutionDetailsState())
    val state: StateFlow<InstitutionDetailsState> = _state.asStateFlow()

    private val _events = Channel<InstitutionDetailsEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: InstitutionDetailsAction) {
        when (action) {
            is InstitutionDetailsAction.LoadInstitution -> loadInstitution(action.institutionId)
            is InstitutionDetailsAction.ToggleSection -> toggleSection(action.sectionId)
            is InstitutionDetailsAction.UpdateProgramme -> updateProgramme(action.programme)
            is InstitutionDetailsAction.AddProgramme -> addProgramme(action.programme)
            is InstitutionDetailsAction.RemoveProgramme -> removeProgramme(action.programmeId)
            is InstitutionDetailsAction.AddUnit -> addUnit(action.unit, action.programmeId)
            is InstitutionDetailsAction.RemoveUnit -> removeUnit(action.programmeId, action.unitId)
            is InstitutionDetailsAction.AddAcademicTerm -> addAcademicTerm(action.term)
            InstitutionDetailsAction.ConfirmDelete -> confirmDelete()
            InstitutionDetailsAction.DismissDelete -> dismissDelete()
            InstitutionDetailsAction.DeleteInstitution -> deleteInstitution()
            InstitutionDetailsAction.DismissError -> dismissError()
            InstitutionDetailsAction.DismissSuccess -> dismissSuccess()
        }
    }

    private fun loadInstitution(institutionId: String) {
        universityRepository
            .observeUniversitiesWithProgrammesAndUnits()
            .map { universities -> universities.find { it.university.id == institutionId } }
            .onStart { _state.update { it.copy(isLoading = true, error = null) } }
            .onEach { institution ->
                val activeTerm = universityRepository.getActiveAcademicTerm(institutionId)
                val departments = universityRepository.getDepartmentsForUniversity(institutionId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        institution = institution,
                        activeTerm = activeTerm,
                        departments = departments,
                        expandedSections = setOf("overview", "programmes")
                    )
                }
            }
            .catch {
                _state.update { it.copy(isLoading = false, error = "Failed to load institution") }
                _events.send(InstitutionDetailsEvent.ShowError("Failed to load institution details"))
            }
            .launchIn(viewModelScope)
    }

    // ============ ACADEMIC TERM OPERATIONS ============

    fun addAcademicTerm(term: NewAcademicTermDraft) {
        viewModelScope.launch {
            val state = _state.value
            val institution = state.institution ?: return@launch

            _state.update { it.copy(isSaving = true, error = null) }

            val request = AddAcademicTermRequest(
                academicYear = term.academicYear,
                semester = term.semester,
                weekCount = term.weekCount
            )

            val result = academicSetUpRepository.addAcademicTerm(institution.university.id, request)

            when (result) {
                is ApiResult.Success -> {
                    refreshInstitutionData(institution.university.id)
                    _state.update { it.copy(isSaving = false, successMessage = result.data.message) }
                    _events.send(InstitutionDetailsEvent.ShowSuccess(result.data.message ?: "Academic term added successfully"))
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    // ============ PROGRAMME OPERATIONS ============

    fun addProgramme(programme: ProgrammeEdit) {
        viewModelScope.launch {
            val state = _state.value
            val institution = state.institution ?: return@launch

            _state.update { it.copy(isSaving = true, error = null) }

            val request = AddProgrammeWithUnitsRequest(
                name = programme.name,
                departmentId = programme.departmentId,
                departmentName = programme.departmentName,
                yearOfStudy = programme.yearOfStudy,
                expectedStudentCount = programme.expectedStudentCount,
                units = emptyList() // No units when adding programme, units added separately
            )

            val result = academicSetUpRepository.addProgrammeWithUnits(institution.university.id, request)

            when (result) {
                is ApiResult.Success -> {
                    refreshInstitutionData(institution.university.id)
                    _state.update { it.copy(isSaving = false, successMessage = result.data.message) }
                    _events.send(InstitutionDetailsEvent.ShowSuccess(result.data.message ?: "Programme added successfully"))
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    fun addProgrammeWithUnits(programme: ProgrammeEdit, units: List<AddUnitToProgrammeRequest>) {
        viewModelScope.launch {
            val state = _state.value
            val institution = state.institution ?: return@launch

            _state.update { it.copy(isSaving = true, error = null) }

            val request = AddProgrammeWithUnitsRequest(
                name = programme.name,
                departmentId = programme.departmentId,
                departmentName = programme.departmentName,
                yearOfStudy = programme.yearOfStudy,
                expectedStudentCount = programme.expectedStudentCount,
                units = units
            )

            val result = academicSetUpRepository.addProgrammeWithUnits(institution.university.id, request)

            when (result) {
                is ApiResult.Success -> {
                    refreshInstitutionData(institution.university.id)
                    _state.update { it.copy(isSaving = false, successMessage = result.data.message) }
                    _events.send(InstitutionDetailsEvent.ShowSuccess(result.data.message ?: "Programme added successfully"))
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    fun updateProgramme(programme: ProgrammeEdit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val request = UpdateProgrammeDetailsRequest(
                name = programme.name,
                yearOfStudy = programme.yearOfStudy,
                expectedStudentCount = programme.expectedStudentCount,
                isActive = programme.isActive
            )

            val result = academicSetUpRepository.updateProgrammeDetails(programme.id, request)

            when (result) {
                is ApiResult.Success -> {
                    refreshInstitutionData(_state.value.institution?.university?.id ?: return@launch)
                    _state.update { it.copy(isSaving = false, successMessage = result.data.message) }
                    _events.send(InstitutionDetailsEvent.ShowSuccess(result.data.message ?: "Programme updated successfully"))
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    fun removeProgramme(programmeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val result = academicSetUpRepository.deactivateProgramme(programmeId)

            when (result) {
                is ApiResult.Success -> {
                    refreshInstitutionData(_state.value.institution?.university?.id ?: return@launch)
                    _state.update { it.copy(isSaving = false, successMessage = "Programme deactivated successfully") }
                    _events.send(InstitutionDetailsEvent.ShowSuccess("Programme deactivated successfully"))
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    // ============ UNIT OPERATIONS ============

    fun addUnit(unit: NewUnitDraft, programmeId: String) {
        viewModelScope.launch {
            val state = _state.value

            _state.update { it.copy(isSaving = true, error = null) }

            val request = AddUnitToProgrammeRequest(
                code = unit.code,
                name = unit.name,
                semester = unit.semester,
                departmentId = unit.department.departmentId,
                lectureDay = unit.lectureDay,
                lectureTime = unit.lectureTime,
                lectureVenue = unit.lectureVenue
            )

            val result = academicSetUpRepository.addUnitToProgramme(programmeId, request)

            when (result) {
                is ApiResult.Success -> {
                    refreshInstitutionData(state.institution?.university?.id ?: return@launch)
                    _state.update { it.copy(isSaving = false, successMessage = result.data.message) }
                    _events.send(InstitutionDetailsEvent.ShowSuccess(result.data.message ?: "Unit added successfully"))
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    fun removeUnit(programmeId: String, unitId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val result = academicSetUpRepository.removeUnitFromProgramme(programmeId, unitId)

            when (result) {
                is ApiResult.Success -> {
                    refreshInstitutionData(_state.value.institution?.university?.id ?: return@launch)
                    _state.update { it.copy(isSaving = false, successMessage = "Unit removed successfully") }
                    _events.send(InstitutionDetailsEvent.ShowSuccess("Unit removed successfully"))
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    // ============ UNIVERSITY OPERATIONS ============

    private fun deleteInstitution() {
        viewModelScope.launch {
            val institution = _state.value.institution ?: return@launch
            _state.update { it.copy(isSaving = true, showDeleteConfirmation = false) }

            val result = academicSetUpRepository.deactivateUniversity(institution.university.id)

            when (result) {
                is ApiResult.Success -> {
                    _events.send(InstitutionDetailsEvent.NavigateBack)
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.extractApiErrorMessage())
                    }
                    _events.send(InstitutionDetailsEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    // ============ HELPER METHODS ============

    private fun refreshInstitutionData(universityId: String) {
        loadInstitution(universityId)
    }

    private fun toggleSection(sectionId: String) {
        _state.update { state ->
            val newSet = state.expandedSections.toMutableSet()
            if (newSet.contains(sectionId)) newSet.remove(sectionId) else newSet.add(sectionId)
            state.copy(expandedSections = newSet)
        }
    }

    private fun confirmDelete() {
        _state.update { it.copy(showDeleteConfirmation = true) }
    }

    private fun dismissDelete() {
        _state.update { it.copy(showDeleteConfirmation = false) }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun dismissSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}