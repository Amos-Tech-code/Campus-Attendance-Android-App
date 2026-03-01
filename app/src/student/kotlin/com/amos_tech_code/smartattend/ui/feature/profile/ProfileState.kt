package com.amos_tech_code.smartattend.ui.feature.profile

import com.amos_tech_code.smartattend.data.local.room.entities.StudentEnrollmentEntity
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion
import com.amos_tech_code.smartattend.utils.formatDate

// Profile Screen State
data class ProfileScreenState(
    val student: Student = Student(),
    val deviceInfo: DeviceInfoUiState = DeviceInfoUiState(),
    val enrollment: EnrollmentUiState? = null,
    val universitySuggestions: List<UniversitySuggestion> = emptyList(),
    val programmeSuggestions: List<ProgrammeSuggestion> = emptyList(),
    val showEnrollmentPrompt: Boolean = false,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isLoggingOut: Boolean = false
)

data class Student(
    val name: String = "",
    val registrationNo: String = "",
    val university: String = "",
    val programme: String = "",
    val yearOfStudy: Int = 0,
    val semester: String = "",
)

data class DeviceInfoUiState(
    val deviceId: String = "",
    val deviceModel: String = "",
    val lastLogin: String = "",
    val isRegisteredDevice: Boolean = true,
    val registrationDate: String = ""
)

data class EnrollmentUiState(
    val enrollmentId: String,
    val university: String,
    val programme: String,
    val yearOfStudy: Int,
    val semester: String,
    val enrollmentDate: String,
    val isActive: Boolean
)

fun StudentEnrollmentEntity.toUiState(): EnrollmentUiState {
    return EnrollmentUiState(
        enrollmentId = enrollmentId,
        university = university.name,
        programme = programme.name,
        yearOfStudy = yearOfStudy,
        semester = "Semester ${academicTerm.semester}",
        enrollmentDate = enrollmentDate.formatDate(),
        isActive = isActive
    )
}
