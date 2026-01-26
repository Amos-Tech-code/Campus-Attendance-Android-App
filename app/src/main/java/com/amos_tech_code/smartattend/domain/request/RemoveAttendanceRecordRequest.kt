package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class RemoveAttendanceRecordRequest(
    val sessionId: String,
    val studentId: String
)