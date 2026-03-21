package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class StudentDeviceChangeRequest(
    val deviceInfo: DeviceInfoDto,
    val reason: String? = null
)

@Serializable
data class DeviceInfoDto(
    val deviceId: String,
    val model: String,
    val os: String,
    val fcmToken: String?
)

@Serializable
data class DeviceChangeApprovalRequest(
    val requestId: String,
    val approve: Boolean,
    val rejectionReason: String? = null
)