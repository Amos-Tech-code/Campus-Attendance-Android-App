package com.amos_tech_code.smartattend.ui.feature.setup

import com.amos_tech_code.smartattend.domain.response.DepartmentSuggestion
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UnitSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion

// Events
sealed interface SetupEvent {
    data object SetupComplete : SetupEvent
    data class ShowErrorMessage(val message: String) : SetupEvent
    data class ShowSuccessMessage(val message: String) : SetupEvent
}

// Intents
// UI Events
sealed interface SetupUiEvent {
    data class UniversityNameChanged(val name: String) : SetupUiEvent
    data class UniversitySelected(val suggestion: UniversitySuggestion) : SetupUiEvent
    data class DepartmentNameChanged(val programmeId: String, val name: String) : SetupUiEvent
    data class DepartmentSelected(val programmeId: String, val suggestion: DepartmentSuggestion) : SetupUiEvent
    data class AcademicYearChanged(val year: String) : SetupUiEvent
    data class SemesterChanged(val semester: Int) : SetupUiEvent
    data object AddProgramme : SetupUiEvent
    data class ProgrammeNameChanged(val programmeId: String, val name: String) : SetupUiEvent
    data class ProgrammeSelected(val programmeId: String, val suggestion: ProgrammeSuggestion) : SetupUiEvent
    data class OnYearOfStudyChanged(val programmeId: String, val year: Int) : SetupUiEvent

    data class NoOfExpectedStudentsChanged(val programmeId: String, val count: String) : SetupUiEvent
    data class ToggleProgrammeExpanded(val programmeId: String) : SetupUiEvent
    data class RemoveProgramme(val programmeId: String) : SetupUiEvent
    data class ShowAddUnitForm(val programmeId: String) : SetupUiEvent
    data class UnitCodeChanged(val code: String) : SetupUiEvent

    data class UnitNameChanged(val name: String) : SetupUiEvent

    data class UnitLectureDayChanged(val day: String) : SetupUiEvent

    data class UnitLectureTimeChanged(val time: String) : SetupUiEvent

    data class UnitLectureVenueChanged(val venue: String) : SetupUiEvent

    data class UnitSelected(val suggestion: UnitSuggestion) : SetupUiEvent
    data object SaveUnit : SetupUiEvent
    data object CancelAddUnit : SetupUiEvent
    data class RemoveUnit(val programmeId: String, val unitId: String) : SetupUiEvent
    data object CompleteSetup : SetupUiEvent
}