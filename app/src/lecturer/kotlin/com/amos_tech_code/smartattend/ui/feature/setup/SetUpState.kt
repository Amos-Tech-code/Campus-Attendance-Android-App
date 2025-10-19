package com.amos_tech_code.smartattend.ui.feature.setup

// State Classes
data class SetupState(
    val universityName: String = "",
    val universityNameError: String? = null,
    val department: String = "",
    val departmentError: String? = null,
    val programmes: List<Programme> = emptyList(),
    val showAddProgrammeForm: Boolean = false,
    val showAddUnitForm: Boolean = false,
    val addProgrammeState: AddProgrammeState = AddProgrammeState(),
    val addUnitState: AddUnitState = AddUnitState(),
    val editingProgrammeId: String? = null,
    val isLoading: Boolean = false,
    val isSetupValid: Boolean = false
)

data class AddProgrammeState(
    val programmeName: String = "",
    val programmeNameError: String? = null,
    val department: String = "",
    val departmentError: String? = null,
    val yearOfStudy: String = "",
    val yearOfStudyError: String? = null
)

data class AddUnitState(
    val selectedProgrammeId: String = "",
    val selectedProgrammeName: String = "",
    val selectedProgrammeError: String? = null,
    val unitName: String = "",
    val unitNameError: String? = null,
    val unitCode: String = "",
    val unitCodeError: String? = null
)

data class Programme(
    val id: String,
    val name: String,
    val department: String,
    val yearOfStudy: Int,
    val units: List<TeachingUnit>
)

data class TeachingUnit(
    val id: String,
    val code: String,
    val name: String,
    val programmeId: String
)