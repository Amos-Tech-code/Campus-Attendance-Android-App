package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStudentProfileRequest(
    val fullName: String,
    val registrationNumber: String
)

@Serializable
data class UpdateLecturerProfileRequest(
    val fullName: String
)