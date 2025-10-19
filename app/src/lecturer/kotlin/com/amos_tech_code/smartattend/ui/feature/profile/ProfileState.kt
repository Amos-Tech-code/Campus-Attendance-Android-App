package com.amos_tech_code.smartattend.ui.feature.profile

//sealed class ProfileState {
//    data object Nothing : ProfileState()
//    data object Loading : ProfileState()
//    data object Success : ProfileState()
//    data class Error(val message: String) : ProfileState()
//}
data class ProfileState(
    // Lecturer Profile
    val lecturer: Lecturer = Lecturer(),

    // Institution Management
    val institutions: List<Institution> = emptyList(),
    val selectedInstitution: Institution? = null,
    val showAddInstitution: Boolean = false,
    val newInstitutionState: NewInstitutionState = NewInstitutionState(),

    // Teaching Statistics
    val teachingStats: TeachingStatistics = TeachingStatistics(),

    // Loading States
    val isLoading: Boolean = false,
    val isSavingInstitution: Boolean = false,
    val isUpdatingProfile: Boolean = false,

    // Error States
    val errorMessage: String? = null
)

data class NewInstitutionState(
    val name: String = "",
    val department: String = "",
    val campus: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val departmentError: String? = null
)


data class Institution(
    val id: String,
    val name: String,
    val department: String,
    val campus: String,
    val isActive: Boolean = false
)

data class Lecturer(
    val name: String = "",
    val email: String = "",
    val institution: String = "",
    val department: String = "",
    val staffId: String = "",
    val officeLocation: String? = null,
    val profileImage: String? = null,
    val joinDate: String? = null
)

data class TeachingStatistics(
    val totalCourses: Int = 0,
    val totalStudents: Int = 0,
    val totalSessions: Int = 0,
    val averageAttendance: Float = 0f,
    val currentSemester: String = "",
    val teachingSince: String = ""
)