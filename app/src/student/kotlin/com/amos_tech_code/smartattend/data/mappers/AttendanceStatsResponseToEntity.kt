package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceStatsEntity
import com.amos_tech_code.smartattend.domain.response.AttendanceStatsResponse

// Extension function to convert to Entity
fun AttendanceStatsResponse.toEntity(): StudentAttendanceStatsEntity {
    return StudentAttendanceStatsEntity(
        totalSessions = this.totalSessions,
        attendedSessions = this.attendedSessions,
        currentStreak = this.currentStreak,
    )
}