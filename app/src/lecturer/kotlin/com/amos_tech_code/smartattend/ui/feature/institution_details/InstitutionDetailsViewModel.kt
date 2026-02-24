package com.amos_tech_code.smartattend.ui.feature.institution_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import com.amos_tech_code.smartattend.domain.request.AcademicTermRef
import com.amos_tech_code.smartattend.domain.request.NewProgrammeDraft
import com.amos_tech_code.smartattend.domain.request.NewUnitDraft
import com.amos_tech_code.smartattend.domain.request.UpdateAcademicSetupRequest
import com.amos_tech_code.smartattend.domain.request.UpdateAcademicTermDto
import com.amos_tech_code.smartattend.domain.request.UpdateProgrammeSetupDto
import com.amos_tech_code.smartattend.domain.request.UpdateUnitAssignmentDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State
data class InstitutionDetailsState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val institution: UniversityWithProgrammesAndUnits? = null,
    val activeTerm: AcademicTermEntity? = null,
    val editMode: EditMode = EditMode.VIEW,
    val pendingChanges: PendingChanges = PendingChanges(),
    val expandedSections: Set<String> = emptySet(),
    val searchQuery: String = "",
    val showDeleteConfirmation: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class EditMode {
    VIEW,
    EDIT,
    BULK_EDIT
}

data class PendingChanges(
    val universityName: String? = null,
    val updatedProgrammes: Map<String, ProgrammeEdit> = emptyMap(),
    val newProgrammes: List<NewProgrammeDraft> = emptyList(),
    val removedProgrammeIds: Set<String> = emptySet(),
    val updatedUnits: Map<String, UnitEdit> = emptyMap(),
    val newUnits: List<NewUnitDraft> = emptyList(),
    val removedUnitIds: Set<String> = emptySet(),
    val updatedAcademicTerms: Map<String, AcademicTermEdit> = emptyMap()
)

data class ProgrammeEdit(
    val id: String,
    val name: String,
    val departmentId: String?,
    val departmentName: String,
    val yearOfStudy: Int,
    val expectedStudentCount: Int,
    val isActive: Boolean
)

data class UnitEdit(
    val id: String,
    val code: String,
    val name: String,
    val semester: Int,
    val lectureDay: String?,
    val lectureTime: String?,
    val lectureVenue: String?,
    val isActive: Boolean
)

data class AcademicTermEdit(
    val id: String,
    val academicYear: String,
    val semester: Int,
    val isActive: Boolean
)

// Events
sealed class InstitutionDetailsEvent {
    data class ShowMessage(val message: String) : InstitutionDetailsEvent()
    data class ShowError(val error: String) : InstitutionDetailsEvent()
    data object NavigateBack : InstitutionDetailsEvent()
}

// UI Actions
sealed class InstitutionDetailsAction {
    data class LoadInstitution(val institutionId: String) : InstitutionDetailsAction()
    data class ToggleEditMode(val mode: EditMode) : InstitutionDetailsAction()
    data class ToggleSection(val sectionId: String) : InstitutionDetailsAction()
    data class UpdateUniversityName(val name: String) : InstitutionDetailsAction()

    // Programme actions
    data class UpdateProgramme(val programme: ProgrammeEdit) : InstitutionDetailsAction()
    data class AddProgramme(val programme: NewProgrammeDraft) : InstitutionDetailsAction()
    data class RemoveProgramme(val programmeId: String) : InstitutionDetailsAction()

    // Unit actions
    data class UpdateUnit(val unit: UnitEdit) : InstitutionDetailsAction()
    data class AddUnit(val unit: NewUnitDraft, val programmeId: String) : InstitutionDetailsAction()
    data class RemoveUnit(val unitId: String) : InstitutionDetailsAction()

    // Term actions
    data class UpdateAcademicTerm(val term: AcademicTermEdit) : InstitutionDetailsAction()
    data class SetActiveTerm(val termId: String) : InstitutionDetailsAction()

    // Save/Cancel
    data object SaveChanges : InstitutionDetailsAction()
    data object CancelChanges : InstitutionDetailsAction()
    data object ConfirmDelete : InstitutionDetailsAction()
    data object DismissDelete : InstitutionDetailsAction()
    data object DeleteInstitution : InstitutionDetailsAction()

    // Dismiss messages
    data object DismissError : InstitutionDetailsAction()
    data object DismissSuccess : InstitutionDetailsAction()
}

// ViewModel
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
            is InstitutionDetailsAction.ToggleEditMode -> toggleEditMode(action.mode)
            is InstitutionDetailsAction.ToggleSection -> toggleSection(action.sectionId)
            is InstitutionDetailsAction.UpdateUniversityName -> updateUniversityName(action.name)
            is InstitutionDetailsAction.UpdateProgramme -> updateProgramme(action.programme)
            is InstitutionDetailsAction.AddProgramme -> addProgramme(action.programme)
            is InstitutionDetailsAction.RemoveProgramme -> removeProgramme(action.programmeId)
            is InstitutionDetailsAction.UpdateUnit -> updateUnit(action.unit)
            is InstitutionDetailsAction.AddUnit -> addUnit(action.unit, action.programmeId)
            is InstitutionDetailsAction.RemoveUnit -> removeUnit(action.unitId)
            is InstitutionDetailsAction.UpdateAcademicTerm -> updateAcademicTerm(action.term)
            is InstitutionDetailsAction.SetActiveTerm -> setActiveTerm(action.termId)
            InstitutionDetailsAction.SaveChanges -> saveChanges()
            InstitutionDetailsAction.CancelChanges -> cancelChanges()
            InstitutionDetailsAction.ConfirmDelete -> confirmDelete()
            InstitutionDetailsAction.DismissDelete -> dismissDelete()
            InstitutionDetailsAction.DeleteInstitution -> deleteInstitution()
            InstitutionDetailsAction.DismissError -> dismissError()
            InstitutionDetailsAction.DismissSuccess -> dismissSuccess()
        }
    }

    private fun loadInstitution(institutionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val institution = universityRepository.getUniversitiesWithProgrammesAndUnits()
                    .find { it.university.id == institutionId }
                val activeTerm = universityRepository.getActiveAcademicTerm(institutionId)

                _state.update {
                    it.copy(
                        isLoading = false,
                        institution = institution,
                        activeTerm = activeTerm,
                        expandedSections = setOf("overview", "programmes")
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to load institution") }
                _events.send(InstitutionDetailsEvent.ShowError("Failed to load institution details"))
            }
        }
    }

    private fun toggleEditMode(mode: EditMode) {
        if (mode == EditMode.VIEW) {
            // Cancel any pending changes when switching to view mode
            cancelChanges()
        }
        _state.update { it.copy(editMode = mode) }
    }

    private fun toggleSection(sectionId: String) {
        _state.update { state ->
            val newSet = state.expandedSections.toMutableSet()
            if (newSet.contains(sectionId)) {
                newSet.remove(sectionId)
            } else {
                newSet.add(sectionId)
            }
            state.copy(expandedSections = newSet)
        }
    }

    private fun updateUniversityName(name: String) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(universityName = name)
            )
        }
    }

    private fun updateProgramme(programme: ProgrammeEdit) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    updatedProgrammes = state.pendingChanges.updatedProgrammes +
                            (programme.id to programme)
                )
            )
        }
    }

    private fun addProgramme(programme: NewProgrammeDraft) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    newProgrammes = state.pendingChanges.newProgrammes + programme
                )
            )
        }
    }

    private fun removeProgramme(programmeId: String) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    removedProgrammeIds = state.pendingChanges.removedProgrammeIds + programmeId,
                    updatedProgrammes = state.pendingChanges.updatedProgrammes - programmeId
                )
            )
        }
    }

    private fun updateUnit(unit: UnitEdit) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    updatedUnits = state.pendingChanges.updatedUnits + (unit.id to unit)
                )
            )
        }
    }

    private fun addUnit(unit: NewUnitDraft, programmeId: String) {
        // Store unit with programme association
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    newUnits = state.pendingChanges.newUnits + unit
                )
            )
        }
    }

    private fun removeUnit(unitId: String) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    removedUnitIds = state.pendingChanges.removedUnitIds + unitId,
                    updatedUnits = state.pendingChanges.updatedUnits - unitId
                )
            )
        }
    }

    private fun updateAcademicTerm(term: AcademicTermEdit) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    updatedAcademicTerms = state.pendingChanges.updatedAcademicTerms + (term.id to term)
                )
            )
        }
    }

    private fun setActiveTerm(termId: String) {
        _state.update { state ->
            val updatedTerms = state.pendingChanges.updatedAcademicTerms.mapValues {
                it.value.copy(isActive = it.key == termId)
            }
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    updatedAcademicTerms = updatedTerms
                )
            )
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            try {
                val state = _state.value
                val institution = state.institution ?: return@launch

                // Build update request
                val request = buildUpdateRequest(state)

                // Send to server
                val result = academicSetUpRepository.updateAcademicSetUp(request)

                when (result) {
                    is ApiResult.Success -> {
                        // Reload institution data
                        loadInstitution(institution.university.id)
                        _state.update {
                            it.copy(
                                isSaving = false,
                                editMode = EditMode.VIEW,
                                pendingChanges = PendingChanges(),
                                successMessage = "Institution updated successfully"
                            )
                        }
                        _events.send(InstitutionDetailsEvent.ShowMessage("Changes saved successfully"))
                    }
                    is ApiResult.Failure -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                error = "Failed to save changes: ${result.error.extractApiErrorMessage()}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save changes: ${e.message}"
                    )
                }
            }
        }
    }

    private fun buildUpdateRequest(state: InstitutionDetailsState): UpdateAcademicSetupRequest {
        val institution = state.institution ?: error("No institution loaded")

        // Build academic terms
        val academicTerms = buildAcademicTerms(state)

        // Build programmes
        val programmes = buildProgrammes(state)

        return UpdateAcademicSetupRequest(
            universityId = institution.university.id,
            isActive = true,
            academicTerms = academicTerms,
            programmes = programmes
        )
    }

    private fun buildAcademicTerms(state: InstitutionDetailsState): List<UpdateAcademicTermDto> {
        val terms = mutableListOf<UpdateAcademicTermDto>()

        // Add existing terms with updates
        state.pendingChanges.updatedAcademicTerms.values.forEach { termEdit ->
            terms.add(
                UpdateAcademicTermDto(
                    academicTermId = termEdit.id,
                    isActive = termEdit.isActive
                )
            )
        }

        // If no active term is set, include the current active term
        if (terms.none { it.isActive } && state.activeTerm != null) {
            terms.add(
                UpdateAcademicTermDto(
                    academicTermId = state.activeTerm.id,
                    isActive = true
                )
            )
        }

        return terms
    }

    private fun buildProgrammes(state: InstitutionDetailsState): List<UpdateProgrammeSetupDto> {
        val programmes = mutableListOf<UpdateProgrammeSetupDto>()
        val institution = state.institution ?: return emptyList()

        // Add updated existing programmes
        institution.programmes.forEach { programmeWithUnits ->
            val programmeEdit = state.pendingChanges.updatedProgrammes[programmeWithUnits.programme.id]

            if (programmeWithUnits.programme.id !in state.pendingChanges.removedProgrammeIds) {
                programmes.add(
                    UpdateProgrammeSetupDto(
                        programmeId = programmeWithUnits.programme.id,
                        isActive = programmeEdit?.isActive ?: true,
                        yearOfStudy = programmeEdit?.yearOfStudy ?: programmeWithUnits.programme.yearOfStudy,
                        expectedStudentCount = programmeEdit?.expectedStudentCount
                            ?: programmeWithUnits.programme.expectedStudentCount,
                        units = buildUnits(programmeWithUnits, state)
                    )
                )
            }
        }

        // Add new programmes
        state.pendingChanges.newProgrammes.forEach { newProgramme ->
            programmes.add(
                UpdateProgrammeSetupDto(
                    draft = newProgramme,
                    isActive = true,
                    yearOfStudy = 1, // Default
                    expectedStudentCount = 0,
                    units = emptyList()
                )
            )
        }

        return programmes
    }

    private fun buildUnits(
        programmeWithUnits: ProgrammeWithUnits,
        state: InstitutionDetailsState
    ): List<UpdateUnitAssignmentDto> {
        val units = mutableListOf<UpdateUnitAssignmentDto>()

        programmeWithUnits.units.forEach { unit ->
            val unitEdit = state.pendingChanges.updatedUnits[unit.id]

            if (unit.id !in state.pendingChanges.removedUnitIds) {
                units.add(
                    UpdateUnitAssignmentDto(
                        unitId = unit.id,
                        isActive = unitEdit?.isActive ?: true,
                        academicTermRef = AcademicTermRef(
                            academicTermId = state.activeTerm?.id
                        ),
                        yearOfStudy = programmeWithUnits.programme.yearOfStudy,
                        lectureDay = unitEdit?.lectureDay ?: unit.lectureDay,
                        lectureTime = unitEdit?.lectureTime ?: unit.lectureTime,
                        lectureVenue = unitEdit?.lectureVenue ?: unit.lectureVenue
                    )
                )
            }
        }

        return units
    }

    private fun cancelChanges() {
        _state.update {
            it.copy(
                editMode = EditMode.VIEW,
                pendingChanges = PendingChanges()
            )
        }
    }

    private fun confirmDelete() {
        _state.update { it.copy(showDeleteConfirmation = true) }
    }

    private fun dismissDelete() {
        _state.update { it.copy(showDeleteConfirmation = false) }
    }

    private fun deleteInstitution() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, showDeleteConfirmation = false) }

            try {
                val institution = _state.value.institution ?: return@launch

                // Send deactivation request
                val request = UpdateAcademicSetupRequest(
                    universityId = institution.university.id,
                    isActive = false,
                    academicTerms = emptyList(),
                    programmes = emptyList()
                )

                val result = academicSetUpRepository.updateAcademicSetUp(request)

                when (result) {
                    is ApiResult.Success -> {
                        _events.send(InstitutionDetailsEvent.NavigateBack)
                    }
                    is ApiResult.Failure -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                error = "Failed to deactivate institution: ${result.error.extractApiErrorMessage()}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to deactivate institution: ${e.message}"
                    )
                }
            }
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun dismissSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}