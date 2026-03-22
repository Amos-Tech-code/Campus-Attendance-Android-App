package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class StudentLookupRequest(
    val registrationNumber: String
)