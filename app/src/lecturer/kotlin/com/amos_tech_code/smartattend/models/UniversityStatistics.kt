package com.amos_tech_code.smartattend.data.models

import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity

data class UniversityStatistics(
    val totalUnits: Int,
    val totalExpectedStudents: Int,
    val totalProgrammes: Int,
    val totalDepartments: Int,
    val activeTerm: AcademicTermEntity?
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