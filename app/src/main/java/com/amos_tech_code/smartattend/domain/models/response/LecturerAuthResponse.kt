package com.amos_tech_code.smartattend.domain.models.response

import kotlinx.serialization.Serializable

@Serializable
data class LecturerAuthResponse(
    val token: String,
    val email: String,
    val name: String,
    val profileComplete: Boolean,
    val userType: String
)