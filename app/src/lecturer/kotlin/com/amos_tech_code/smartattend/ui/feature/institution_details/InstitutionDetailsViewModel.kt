package com.amos_tech_code.smartattend.ui.feature.institution_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import com.amos_tech_code.smartattend.domain.request.AcademicTermRef
import com.amos_tech_code.smartattend.domain.request.DepartmentRef
import com.amos_tech_code.smartattend.domain.request.NewAcademicTermDraft
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

class InstitutionDetailsViewModel(
    private val academicSetUpRepository: AcademicSetUpRepository,
    private val universityRepository: UniversityRepository
) : ViewModel()
{

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
            //is InstitutionDetailsAction.UpdateAcademicTerm -> updateAcademicTerm(action.term)
            is InstitutionDetailsAction.SetActiveTerm -> setActiveTerm(action.termId)
            is InstitutionDetailsAction.AddAcademicTerm -> addAcademicTerm(action.term)
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
                val departments = universityRepository.getDepartmentsForUniversity(institutionId) // Add this

                _state.update {
                    it.copy(
                        isLoading = false,
                        institution = institution,
                        activeTerm = activeTerm,
                        departments = departments,
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

    private fun addProgramme(programme: ProgrammeEdit) {
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

            val isNewProgramme =
                state.pendingChanges.newProgrammes.any { it.id == programmeId }

            if (isNewProgramme) {
                // 🔹 Remove completely (it was never saved to backend)
                state.copy(
                    pendingChanges = state.pendingChanges.copy(
                        newProgrammes = state.pendingChanges.newProgrammes
                            .filterNot { it.id == programmeId },

                        // Also remove any new units attached to it
                        newUnits = state.pendingChanges.newUnits - programmeId
                    )
                )
            } else {
                // 🔹 Existing programme → mark for removal
                state.copy(
                    pendingChanges = state.pendingChanges.copy(
                        removedProgrammeIds =
                            state.pendingChanges.removedProgrammeIds + programmeId,

                        updatedProgrammes =
                            state.pendingChanges.updatedProgrammes - programmeId
                    )
                )
            }
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
        _state.update { state ->

            val existing = state.pendingChanges.newUnits[programmeId].orEmpty()

            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    newUnits = state.pendingChanges.newUnits +
                            (programmeId to (existing + unit))
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

    private fun addAcademicTerm(term: NewAcademicTermDraft) {
        _state.update { state ->
            state.copy(
                pendingChanges = state.pendingChanges.copy(
                    newAcademicTerms = state.pendingChanges.newAcademicTerms + term
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
                        _events.send(InstitutionDetailsEvent.ShowError("Failed to save changes"))
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

    private fun buildProgrammes(state: InstitutionDetailsState): List<UpdateProgrammeSetupDto> {
        val programmes = mutableListOf<UpdateProgrammeSetupDto>()
        val institution = state.institution ?: return emptyList()

        // 1. Handle EXISTING programmes being updated (ONLY programmeId, NO draft)
        institution.programmes.forEach { programmeWithUnits ->

            val programmeId = programmeWithUnits.programme.id
            val edit = state.pendingChanges.updatedProgrammes[programmeId]

            if (programmeId !in state.pendingChanges.removedProgrammeIds) {

                programmes.add(
                    UpdateProgrammeSetupDto(
                        programmeId = programmeId,
                        draft = null,
                        isActive = edit?.isActive ?: true,
                        yearOfStudy = edit?.yearOfStudy
                            ?: programmeWithUnits.programme.yearOfStudy,
                        expectedStudentCount = edit?.expectedStudentCount
                            ?: programmeWithUnits.programme.expectedStudentCount,
                        units = buildUnits(programmeId, programmeWithUnits, state)
                    )
                )
            }
        }

        // 2. Handle NEW programmes being added (ONLY draft, NO programmeId)
        state.pendingChanges.newProgrammes.forEach { programmeEdit ->

            programmes.add(
                UpdateProgrammeSetupDto(
                    programmeId = null,
                    draft = NewProgrammeDraft(
                        name = programmeEdit.name,
                        department = DepartmentRef(
                            departmentId = programmeEdit.departmentId,
                            draftName = programmeEdit.departmentName
                        )
                    ),
                    isActive = true,
                    yearOfStudy = programmeEdit.yearOfStudy,
                    expectedStudentCount = programmeEdit.expectedStudentCount,
                    units = buildNewProgrammeUnits(programmeEdit, state)
                )
            )
        }

        return programmes
    }

    private fun buildNewProgrammeUnits(
        programmeEdit: ProgrammeEdit,
        state: InstitutionDetailsState
    ): List<UpdateUnitAssignmentDto> {

        val units = mutableListOf<UpdateUnitAssignmentDto>()

        val drafts = state.pendingChanges.newUnits[programmeEdit.id].orEmpty()

        drafts.forEach { draft ->
            units.add(
                UpdateUnitAssignmentDto(
                    unitId = null,
                    draft = draft,
                    isActive = true,
                    academicTermRef = resolveAcademicTermRef(state),
                    yearOfStudy = programmeEdit.yearOfStudy,
                    lectureDay = null,
                    lectureTime = null,
                    lectureVenue = null
                )
            )
        }

        return units
    }

    private fun buildUnits(
        programmeId: String,
        programmeWithUnits: ProgrammeWithUnits,
        state: InstitutionDetailsState
    ): List<UpdateUnitAssignmentDto> {
        val units = mutableListOf<UpdateUnitAssignmentDto>()

        // 1. Handle EXISTING units being updated (ONLY unitId, NO draft)
        programmeWithUnits.units.forEach { unit ->

            val edit = state.pendingChanges.updatedUnits[unit.id]

            if (unit.id !in state.pendingChanges.removedUnitIds) {

                units.add(
                    UpdateUnitAssignmentDto(
                        unitId = unit.id,
                        draft = null,
                        isActive = edit?.isActive ?: true,
                        academicTermRef = resolveAcademicTermRef(state),
                        yearOfStudy = programmeWithUnits.programme.yearOfStudy,
                        lectureDay = edit?.lectureDay ?: unit.lectureDay,
                        lectureTime = edit?.lectureTime ?: unit.lectureTime,
                        lectureVenue = edit?.lectureVenue ?: unit.lectureVenue
                    )
                )
            }
        }

        // 2. Handle NEW units being added (ONLY draft, NO unitId)
        // Note: New units come from state.pendingChanges.newUnits
        val newUnitsForProgramme =
            state.pendingChanges.newUnits[programmeId].orEmpty()

        newUnitsForProgramme.forEach { draft ->

            units.add(
                UpdateUnitAssignmentDto(
                    unitId = null,
                    draft = draft,
                    isActive = true,
                    academicTermRef = resolveAcademicTermRef(state),
                    yearOfStudy = programmeWithUnits.programme.yearOfStudy,
                    lectureDay = null,
                    lectureTime = null,
                    lectureVenue = null
                )
            )
        }

        return units
    }

    private fun buildAcademicTerms(state: InstitutionDetailsState): List<UpdateAcademicTermDto> {
        val terms = mutableListOf<UpdateAcademicTermDto>()

        // 1. Handle EXISTING terms being updated (ONLY academicTermId, NO draft)
        state.pendingChanges.updatedAcademicTerms.values.forEach { termEdit ->
            terms.add(
                UpdateAcademicTermDto(
                    academicTermId = termEdit.id, // ONLY the ID for existing
                    draft = null, // NO draft for existing
                    isActive = termEdit.isActive
                )
            )
        }

        // 2. Handle NEW terms being added (ONLY draft, NO academicTermId)
        state.pendingChanges.newAcademicTerms.forEach { newTerm ->
            terms.add(
                UpdateAcademicTermDto(
                    academicTermId = null, // NO ID for new
                    draft = newTerm, // ONLY draft for new
                    isActive = true
                )
            )
        }

        // 3. If no terms are being sent, include the current active term
        if (terms.isEmpty() && state.activeTerm != null) {
            terms.add(
                UpdateAcademicTermDto(
                    academicTermId = state.activeTerm.id,
                    draft = null,
                    isActive = true
                )
            )
        }

        return terms
    }

    private fun resolveAcademicTermRef(
        state: InstitutionDetailsState
    ): AcademicTermRef {

        return when {
            state.activeTerm != null -> {
                AcademicTermRef(
                    academicTermId = state.activeTerm.id,
                    draft = null
                )
            }

            state.pendingChanges.newAcademicTerms.isNotEmpty() -> {
                AcademicTermRef(
                    academicTermId = null,
                    draft = state.pendingChanges.newAcademicTerms.first()
                )
            }

            else -> error("No academic term available")
        }
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