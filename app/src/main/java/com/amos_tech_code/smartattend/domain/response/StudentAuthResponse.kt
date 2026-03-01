package com.amos_tech_code.smartattend.domain.response

import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import kotlinx.serialization.Serializable

@Serializable
data class StudentAuthResponse(
    val token: String,
    val fullName: String,
    val regNumber: String,
    val deviceStatus: DeviceStatus,
    val message: String,
    val lastLoginAt: String?,
)