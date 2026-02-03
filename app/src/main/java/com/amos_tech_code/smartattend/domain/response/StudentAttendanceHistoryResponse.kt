package com.amos_tech_code.smartattend.domain.response

import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import kotlinx.serialization.Serializable

@Serializable
data class StudentAttendanceHistoryResponse(
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val records: List<StudentAttendanceRecordDto>
)

@Serializable
data class StudentAttendanceRecordDto(
    val sessionId: String,
    val unitCode: String,
    val unitName: String,
    val sessionTitle: String?,
    val sessionType: AttendanceSessionType,
    val attendanceMethodUsed: AttendanceMethod,
    val status: AttendanceSessionStatus,
    val attendedAt: Long,
    val isSuspicious: Boolean,
    val suspiciousReason: String?
)