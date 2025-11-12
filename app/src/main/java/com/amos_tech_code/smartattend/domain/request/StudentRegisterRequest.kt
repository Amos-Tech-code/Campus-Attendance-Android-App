package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class StudentRegisterRequest(
    val registrationNumber: String,
    val fullName: String,
    val deviceInfo: DeviceInfo
)

@Serializable
data class DeviceInfo(
    val deviceId: String,
    val model: String,
    val os: String,
    val fcmToken: String? = null
)