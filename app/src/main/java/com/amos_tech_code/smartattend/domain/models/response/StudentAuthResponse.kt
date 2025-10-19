package com.amos_tech_code.smartattend.domain.models.response

import kotlinx.serialization.Serializable

@Serializable
data class StudentAuthResponse(
    val token: String,
    val fullName: String,
    val regNumber: String,
    val userType: String,
)