package com.amos_tech_code.smartattend.ui.feature.institution_details


// Add ShowSuccess to events
sealed class InstitutionDetailsEvent {
    data class ShowError(val error: String) : InstitutionDetailsEvent()
    data class ShowSuccess(val message: String) : InstitutionDetailsEvent()
    data object NavigateBack : InstitutionDetailsEvent()
}

// UI Actions
sealed class InstitutionDetailsAction {
    data class LoadInstitution(val institutionId: String) : InstitutionDetailsAction()
    data class ToggleSection(val sectionId: String) : InstitutionDetailsAction()

    // Programme actions
    data class AddProgramme(val programme: ProgrammeEdit) : InstitutionDetailsAction()
    data class UpdateProgramme(val programme: ProgrammeEdit) : InstitutionDetailsAction()
    data class RemoveProgramme(val programmeId: String) : InstitutionDetailsAction()

    // Unit actions
    data class AddUnit(val unit: NewUnitDraft, val programmeId: String) : InstitutionDetailsAction()
    data class RemoveUnit(val programmeId: String, val unitId: String) : InstitutionDetailsAction()

    // Term actions
    data class AddAcademicTerm(val term: NewAcademicTermDraft) : InstitutionDetailsAction()

    // University actions
    data object DeleteInstitution : InstitutionDetailsAction()
    data object ConfirmDelete : InstitutionDetailsAction()
    data object DismissDelete : InstitutionDetailsAction()
    data object DismissError : InstitutionDetailsAction()
    data object DismissSuccess : InstitutionDetailsAction()
}
