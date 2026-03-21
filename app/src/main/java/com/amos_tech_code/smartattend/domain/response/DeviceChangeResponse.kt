package com.amos_tech_code.smartattend.domain.response

import com.amos_tech_code.smartattend.domain.models.DeviceChangeStatus
import com.amos_tech_code.smartattend.domain.request.DeviceInfoDto
import kotlinx.serialization.Serializable


@Serializable
data class DeviceChangeRequestResponse(
    val requestId: String,
    val status: DeviceChangeStatus,
    val message: String,
    val requestedAt: String
)

@Serializable
data class PendingDeviceChangeDto(
    val requestId: String,
    val studentId: String,
    val studentName: String,
    val studentRegNo: String,
    val oldDeviceId: String,
    val newDeviceInfo: DeviceInfoDto,
    val reason: String?,
    val requestedAt: String
)

@Serializable
data class DeviceChangeHistoryDto(
    val requestId: String,
    val status: DeviceChangeStatus,
    val oldDeviceId: String,
    val newDeviceInfo: DeviceInfoDto,
    val reason: String?,
    val requestedAt: String,
    val reviewedBy: String?,
    val reviewedAt: String?,
    val rejectionReason: String?
)