package com.amos_tech_code.smartattend.data.local.room_db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus

@Entity(tableName = "attendance_session_history")
data class AttendanceSessionHistoryEntity(
    @PrimaryKey
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
