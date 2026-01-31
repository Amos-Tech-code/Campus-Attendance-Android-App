package com.amos_tech_code.smartattend.models

import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity

data class UniversityWithStats(
    val university: UniversityEntity,
    val statistics: UniversityStatistics
)

data class UniversityStatistics(
    val totalUnits: Int = 0,
    val totalExpectedStudents: Int = 0,
    val totalProgrammes: Int = 0,
    val totalDepartments: Int = 0,
    val activeTerm: AcademicTermEntity? = null
)

data class TeachingStatistics(
    val totalCourses: Int,
    val totalExpectedStudents: Int,
    val currentSemester: String,
    // Optional: Additional statistics you might want
    val totalProgrammes: Int = 0,
    val totalDepartments: Int = 0,
    val activeInstitution: String = "",
    val isInstitutionActive: Boolean = false
)