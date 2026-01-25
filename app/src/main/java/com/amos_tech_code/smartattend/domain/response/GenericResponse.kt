package com.amos_tech_code.smartattend.domain.response

import kotlinx.serialization.Serializable

@Serializable
data class GenericResponse(
    val statusCode: Int,
    val message: String
)
