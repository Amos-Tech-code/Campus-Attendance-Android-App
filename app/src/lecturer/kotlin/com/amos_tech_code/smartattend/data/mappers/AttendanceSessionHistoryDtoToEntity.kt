package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity
import com.amos_tech_code.smartattend.domain.response.AttendanceSessionHistoryDto
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.utils.toEpochMillisStrict
import kotlin.time.ExperimentalTime

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

@OptIn(ExperimentalTime::class)
fun StartAttendanceSessionResponse.toEntity() : AttendanceSessionHistoryEntity {

    return AttendanceSessionHistoryEntity(
        sessionId = this.sessionId,
        title = this.title,
        unitCode = this.unit.code,
        unitName = this.unit.name,
        sessionType = this.sessionType.name,
        attendanceMethod = this.method,
        status = this.status,
        startedAt = this.timeInfo.startTime.toEpochMillisStrict(),
        endedAt = this.timeInfo.endTime.toEpochMillisStrict()
    )
}
