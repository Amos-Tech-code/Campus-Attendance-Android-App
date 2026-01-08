package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class AcademicSetUpRequest(
    val universityId: String? = null,          // Existing or null
    val universityName: String? = null,         // Required if universityId == null
    val academicYear: String,           // "2025-2026"
    val semester: Int,                  // 1 or 2

    val programmes: List<ProgrammeSetupRequest>
)


@Serializable
data class ProgrammeSetupRequest(
    val programmeId: String? = null,           // Existing or null
    val programmeName: String? = null,         // Required if programmeId == null
    val departmentId: String? = null,          // Existing or null
    val departmentName: String? = null,        // Required if departmentId == null

    val yearOfStudy: Int,               // Contextual year
    val expectedStudentCount: Int,

    val units: List<UnitSetupRequest>
)


@Serializable
data class UnitSetupRequest(
    val unitId: String? = null,                // Existing or null
    val code: String,
    val name: String,

    val semester: Int,                 // REQUIRED (1 or 2)

    val lectureDay: String? = null,     // Optional
    val lectureTime: String? = null,    // Optional
    val lectureVenue: String? = null    // Optional
)
