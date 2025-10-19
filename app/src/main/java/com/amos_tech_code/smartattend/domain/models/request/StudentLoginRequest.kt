package com.amos_tech_code.smartattend.domain.models.request

import kotlinx.serialization.Serializable

@Serializable
data class StudentLoginRequest(
    val registrationNumber: String,
    val deviceInfo: DeviceInfo
)