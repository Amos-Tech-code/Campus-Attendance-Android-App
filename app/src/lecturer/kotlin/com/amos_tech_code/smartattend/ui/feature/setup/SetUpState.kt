package com.amos_tech_code.smartattend.ui.feature.setup

import com.amos_tech_code.smartattend.domain.response.DepartmentSuggestion
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UnitSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion
import java.util.UUID

// State Classes
data class SetupUiState(
    // University Section
    val universityName: String = "",
    val universityNameError: String? = null,
    val selectedUniversityId: String? = null,
    val universitySuggestions: List<UniversitySuggestion> = emptyList(),
    val showUniversitySuggestions: Boolean = false,

    // Academic Info
    val academicYear: String = "",
    val selectedSemester: Int = 1,

    // Programmes
    val programmes: List<ProgrammeUiState> = emptyList(),
    val showAddProgrammeForm: Boolean = false,
    val showAddUnitForm: Boolean = false,
    val addProgrammeState: AddProgrammeState = AddProgrammeState(),
    val addUnitState: AddUnitState = AddUnitState(),

    // Loading & Validation
    val isLoading: Boolean = false,
    val isLoadingSuggestions: Boolean = false,
    val isSetupValid: Boolean = false,

    // Form visibility
    val activeProgrammeId: String? = null,
    val isEditingProgramme: Boolean = false
)

data class ProgrammeUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val nameError: String? = null,
    val selectedProgrammeId: String? = null,
    val programmeSuggestions: List<ProgrammeSuggestion> = emptyList(),
    val showProgrammeSuggestions: Boolean = false,

    // Department
    val departmentName: String = "",
    val departmentNameError: String? = null,
    val selectedDepartmentId: String? = null,
    val departmentSuggestions: List<DepartmentSuggestion> = emptyList(),
    val showDepartmentSuggestions: Boolean = false,

    val yearOfStudy: Int = 1,
    val expectedStudentCount: String = "",
    val units: List<UnitUiState> = emptyList(),
    val isExpanded: Boolean = true
)

data class UnitUiState(
    val id: String = UUID.randomUUID().toString(),
    val code: String = "",
    val codeError: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val selectedUnitId: String? = null,
    val lectureDay: String = "",
    val lectureTime: String = "",
    val lectureVenue: String = ""
)

data class AddProgrammeState(
    val programmeName: String = "",
    val programmeNameError: String? = null,
    val selectedProgrammeId: String? = null,
    val departmentName: String = "",
    val departmentNameError: String? = null,
    val selectedDepartmentId: String? = null,
    val yearOfStudy: Int = 1,
    val yearOfStudyError: String? = null,
    val expectedStudentCount: Int = 0,
    val expectedStudentCountError: String? = null
)

data class AddUnitState(
    val programmeId: String = "",
    val unitCode: String = "",
    val unitCodeError: String? = null,
    val selectedUnitId: String? = null,
    val unitSuggestions: List<UnitSuggestion> = emptyList(), // Add this
    val showUnitSuggestions: Boolean = false, // Add this
    val unitName: String = "",
    val unitNameError: String? = null,
    val lectureDay: String = "",
    val lectureTime: String = "",
    val lectureVenue: String = ""
)