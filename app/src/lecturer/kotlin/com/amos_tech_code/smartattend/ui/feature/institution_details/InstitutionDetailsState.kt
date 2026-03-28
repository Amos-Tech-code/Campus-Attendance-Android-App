package com.amos_tech_code.smartattend.ui.feature.institution_details

import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.DepartmentEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits

data class InstitutionDetailsState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val institution: UniversityWithProgrammesAndUnits? = null,
    val activeTerm: AcademicTermEntity? = null,
    val departments: List<DepartmentEntity> = emptyList(),
    val expandedSections: Set<String> = emptySet(),
    val error: String? = null,
    val successMessage: String? = null,
    val showDeleteConfirmation: Boolean = false
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

data class NewUnitDraft(
    val code: String,
    val name: String,
    val semester: Int,
    val lectureDay: String? = null,
    val lectureTime: String? = null,
    val lectureVenue: String? = null,
    val department: DepartmentRef
)

data class DepartmentRef(
    val departmentId: String? = null,
    val draftName: String? = null
)

data class NewAcademicTermDraft(
    val academicYear: String,
    val semester: Int,
    val weekCount: Int = 14
)
