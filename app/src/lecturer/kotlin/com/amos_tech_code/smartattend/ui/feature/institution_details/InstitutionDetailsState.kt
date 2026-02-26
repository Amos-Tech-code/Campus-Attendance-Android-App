package com.amos_tech_code.smartattend.ui.feature.institution_details

import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.DepartmentEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.domain.request.NewAcademicTermDraft
import com.amos_tech_code.smartattend.domain.request.NewUnitDraft

// State
data class InstitutionDetailsState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val institution: UniversityWithProgrammesAndUnits? = null,
    val activeTerm: AcademicTermEntity? = null,
    val departments: List<DepartmentEntity> = emptyList(),
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
}

data class PendingChanges(
    val universityName: String? = null,
    val updatedProgrammes: Map<String, ProgrammeEdit> = emptyMap(),
    val newProgrammes: List<ProgrammeEdit> = emptyList(),
    val removedProgrammeIds: Set<String> = emptySet(),
    val updatedUnits: Map<String, UnitEdit> = emptyMap(),
    val newUnits: Map<String, List<NewUnitDraft>> = emptyMap(),
    val removedUnitIds: Set<String> = emptySet(),
    val updatedAcademicTerms: Map<String, AcademicTermEdit> = emptyMap(),
    val newAcademicTerms: List<NewAcademicTermDraft> = emptyList() // Add this for new terms
)

data class ProgrammeEdit(
    val id: String,
    val name: String,
    val departmentId: String? = null,
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
    data class AddProgramme(val programme: ProgrammeEdit) : InstitutionDetailsAction()
    data class RemoveProgramme(val programmeId: String) : InstitutionDetailsAction()

    // Unit actions
    data class UpdateUnit(val unit: UnitEdit) : InstitutionDetailsAction()
    data class AddUnit(val unit: NewUnitDraft, val programmeId: String) : InstitutionDetailsAction()
    data class RemoveUnit(val unitId: String) : InstitutionDetailsAction()

    // Term actions
    //data class UpdateAcademicTerm(val term: AcademicTermEdit) : InstitutionDetailsAction()
    data class AddAcademicTerm(val term: NewAcademicTermDraft) : InstitutionDetailsAction()

    data class SetActiveTerm(val termId: String) : InstitutionDetailsAction()

    // Save/Cancel
    data object SaveChanges : InstitutionDetailsAction()
    data object CancelChanges : InstitutionDetailsAction()
    data object ConfirmDelete : InstitutionDetailsAction()
    data object DismissDelete : InstitutionDetailsAction()
    data object DeleteInstitution : InstitutionDetailsAction()

    // Dismiss messagesa
    data object DismissError : InstitutionDetailsAction()
    data object DismissSuccess : InstitutionDetailsAction()
}