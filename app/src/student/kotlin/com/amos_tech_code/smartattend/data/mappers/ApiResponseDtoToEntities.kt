package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room.entities.AcademicTermEmbedded
import com.amos_tech_code.smartattend.data.local.room.entities.ProgrammeEmbedded
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.data.local.room.entities.StudentEnrollmentEntity
import com.amos_tech_code.smartattend.data.local.room.entities.UniversityEmbedded
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.StudentEnrollmentSource
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.StudentAttendanceRecordDto
import com.amos_tech_code.smartattend.domain.response.StudentEnrollmentResponse
import com.amos_tech_code.smartattend.utils.toEpochMillisStrict

fun StudentEnrollmentResponse.toEntity() =
    StudentEnrollmentEntity(
        enrollmentId = enrollmentId,
        registrationNumber = registrationNumber,
        fullName = fullName,
        university = UniversityEmbedded(
            id = university.id,
            name = university.name
        ),
        programme = ProgrammeEmbedded(
            id = programme.id,
            name = programme.name
        ),
        academicTerm = AcademicTermEmbedded(
            id = academicTerm.id,
            academicYear = academicTerm.academicYear,
            semester = academicTerm.semester,
            isActive = academicTerm.isActive
        ),
        yearOfStudy = yearOfStudy,
        enrollmentDate = enrollmentDate,
        enrollmentSource = StudentEnrollmentSource.valueOf(enrollmentSource),
        isActive = isActive
    )

fun StudentAttendanceRecordDto.toEntity() =
    StudentAttendanceRecordEntity(
        sessionId = sessionId,
        unitCode = unitCode,
        unitName = unitName,
        sessionTitle = sessionTitle,
        sessionType = sessionType,
        attendanceMethodUsed = attendanceMethodUsed,
        status = status,
        attendedAt = attendedAt,
        isSuspicious = isSuspicious,
        suspiciousReason = suspiciousReason
    )


fun MarkAttendanceResponse.toEntity() =
    StudentAttendanceRecordEntity(
        sessionId = sessionId,
        unitCode = sessionDetails?.unitCode ?: "",
        unitName = sessionDetails?.unitName ?: "",
        sessionTitle = sessionDetails?.sessionTitle,
        sessionType = sessionDetails?.sessionType ?: AttendanceSessionType.REGULAR,
        attendanceMethodUsed = sessionDetails?.attendanceMethod ?: AttendanceMethod.ANY,
        status = sessionDetails?.sessionStatus ?: AttendanceSessionStatus.ACTIVE,
        attendedAt = attendedAt.toEpochMillisStrict(),
        isSuspicious = flags.isNotEmpty(),
        suspiciousReason = flags.joinToString { it.type.name }
    )
