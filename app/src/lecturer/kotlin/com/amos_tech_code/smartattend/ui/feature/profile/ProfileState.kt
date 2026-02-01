package com.amos_tech_code.smartattend.ui.feature.profile

// Profile State
data class ProfileState(
    val isLoading: Boolean = false,
    val isSwitchingInstitution: Boolean = false,
    val errorMessage: String? = null,
    val lecturer: Lecturer = Lecturer("", ""),
    val institutions: List<Institution> = emptyList(),
    val selectedInstitution: Institution? = null,
    val teachingStats: TeachingStatisticsUiState = TeachingStatisticsUiState(),
    val showEditNameSheet: Boolean = false,
    val editingName: String = "",
    val editingNameError: String? = null,
    val bottomSheetErrorMessage: String? = null,
    val isUpdatingProfile: Boolean = false
)

data class Institution(
    val id: String,
    val name: String,
    val isActive: Boolean = false
)

data class Lecturer(
    val name: String,
    val email: String,
    val profileImage: String? = null,
    val joinDate: String? = null,
    val staffId: String? = null
)

data class TeachingStatisticsUiState(
    val totalCourses: Int = 0,
    val totalExpectedStudents: Int = 0,
    val currentSemester: String = "",
    val totalProgrammes: Int = 0,
    val totalDepartments: Int = 0,
    val activeInstitution: String = "",
    val isInstitutionActive: Boolean = false
)