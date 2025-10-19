package com.amos_tech_code.smartattend.domain.models.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSignInRequest(
    val idToken: String
)