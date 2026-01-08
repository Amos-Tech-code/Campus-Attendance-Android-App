package com.amos_tech_code.smartattend.domain.models

// Domain Models
data class University(
    val id: String,
    val name: String,
    val isActive: Boolean = false,
    val programmes: List<Programme> = emptyList()
)

data class Programme(
    val id: String,
    val name: String,
    val departmentId: String,
    val departmentName: String,
    val yearOfStudy: Int,
    val expectedStudentCount: Int,
    val units: List<UnitModel> = emptyList()
)

data class UnitModel(
    val id: String,
    val code: String,
    val name: String,
    val semester: Int,
    val lectureDay: String? = null,
    val lectureTime: String? = null,
    val lectureVenue: String? = null
)
