package com.amos_tech_code.smartattend.domain.response

import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceSessionHistoryResponse(
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val sessions: List<AttendanceSessionHistoryDto>
)

@Serializable
data class AttendanceSessionHistoryDto(
    val sessionId: String,
    val title: String?,
    val unitCode: String,
    val unitName: String,
    val sessionType: String,
    val attendanceMethod: AttendanceMethod,
    val status: AttendanceSessionStatus,
    val startedAt: Long,
    val endedAt: Long?
)