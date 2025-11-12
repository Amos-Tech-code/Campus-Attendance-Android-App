package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class StudentLoginRequest(
    val registrationNumber: String,
    val deviceInfo: DeviceInfo
)