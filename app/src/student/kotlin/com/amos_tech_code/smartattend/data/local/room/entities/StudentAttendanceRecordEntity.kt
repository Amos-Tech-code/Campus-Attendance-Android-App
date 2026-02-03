package com.amos_tech_code.smartattend.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType

@Entity(tableName = "student_attendance_records")
data class StudentAttendanceRecordEntity(
    @PrimaryKey
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
