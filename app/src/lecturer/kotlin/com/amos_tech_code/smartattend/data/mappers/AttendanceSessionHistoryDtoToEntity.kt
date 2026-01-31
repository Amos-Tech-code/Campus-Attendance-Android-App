package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity
import com.amos_tech_code.smartattend.domain.response.AttendanceSessionHistoryDto

fun AttendanceSessionHistoryDto.toEntity(): AttendanceSessionHistoryEntity {
    return AttendanceSessionHistoryEntity(
        sessionId = sessionId,
        title = title,
        unitCode = unitCode,
        unitName = unitName,
        sessionType = sessionType,
        attendanceMethod = attendanceMethod,
        status = status,
        startedAt = startedAt,
        endedAt = endedAt
    )
}
