package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSignInRequest(
    val idToken: String
)