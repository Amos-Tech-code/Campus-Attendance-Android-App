package com.amos_tech_code.smartattend.domain.response

import kotlinx.serialization.Serializable

@Serializable
data class GenericErrorResponse(
    val statusCode: Int,
    val message: String
)
