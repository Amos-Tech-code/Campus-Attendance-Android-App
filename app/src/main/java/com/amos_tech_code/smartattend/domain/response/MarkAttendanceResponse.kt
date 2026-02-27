package com.amos_tech_code.smartattend.domain.response

import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.FlagType
import com.amos_tech_code.smartattend.domain.models.SeverityLevel
import kotlinx.serialization.Serializable

@Serializable
data class MarkAttendanceResponse(
    val success: Boolean,
    val sessionId: String,
    val programmeId: String? = null,
    val verification: VerificationResult,
    val flags: List<AttendanceFlag> = emptyList(),
    val requiresProgrammeSelection: Boolean = false,
    val availableProgrammes: List<ProgrammeInfoResponse> = emptyList(),
    val sessionDetails: SessionDetailsResponse? = null,
    val attendedAt: String,
    val message: String? = null
)

@Serializable
data class SessionDetailsResponse(
    val sessionStatus: AttendanceSessionStatus,
    val attendanceMethod: AttendanceMethod,
    val sessionType: AttendanceSessionType,
    val unitCode: String,
    val unitName: String,
    val sessionTitle: String? = null,
)

@Serializable
data class VerificationResult(
    val locationVerified: Boolean,
    val deviceVerified: Boolean,
    val methodVerified: Boolean,
    val attendanceTimeVerified: Boolean,
    val overallVerified: Boolean
)

@Serializable
data class AttendanceFlag(
    val type: FlagType,
    val message: String,
    val severity: SeverityLevel
)

@Serializable
data class ProgrammeInfoResponse(
    val id: String,
    val name: String,
    val department: String,
    val yearOfStudy: Int
)

@Serializable
data class VerifyAttendanceResponse(
    val requiresProgrammeSelection: Boolean,
    val availableProgrammes: List<ProgrammeInfoResponse>,
    val requiresLocation: Boolean,
    val sessionInfo: SessionInfo
)

@Serializable
data class SessionInfo(
    val sessionId: String,
    val unitName: String,
    val unitCode: String,
    val lecturerName: String
)