package com.amos_tech_code.smartattend.ui.feature.home

import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import com.amos_tech_code.smartattend.utils.formatDate
import com.amos_tech_code.smartattend.utils.formatDateTime

// Data classes for home screen
data class StudentHomeState(
    val studentName: String = "",
    val registrationNo: String = "",
    val deviceStatus: DeviceStatus = DeviceStatus.ACTIVE,
    val showDeviceWarning: Boolean = false,
    val programme: String = "",
    val currentYear: Int = 0,
    val currentSemester: Int = 0,
    val todaySessions: List<TodaySession> = emptyList(),
    val totalSessions: Int = 0,
    val attendedSessions: Int = 0,
    val attendanceRate: Int = 0,
    val currentStreak: Int = 0,
    val lastStatsUpdate: Long? = null,
    val recentAttendance: List<RecentAttendance> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

data class TodaySession(
    val id: String,
    val unitCode: String,
    val unitName: String,
    val startTime: String,
    val endTime: String,
    val type: String,
    val status: String,
    val isOngoing: Boolean
)

data class RecentAttendance(
    val unitName: String,
    val unitCode: String,
    val date: String,
    val time: String,
    val method: AttendanceMethod
)

// Extension functions for conversion
fun StudentAttendanceRecordEntity.toTodaySession(): TodaySession {
    return TodaySession(
        id = sessionId,
        unitCode = unitCode,
        unitName = unitName,
        startTime = attendedAt.formatDateTime(),
        endTime = (attendedAt + 3600000).formatDateTime(),
        type = sessionType.name,
        status = status.name,
        isOngoing = status == AttendanceSessionStatus.ACTIVE
    )
}

fun StudentAttendanceRecordEntity.toRecentAttendance(): RecentAttendance {
    return RecentAttendance(
        unitName = unitName,
        unitCode = unitCode,
        date = attendedAt.formatDate(),
        time = attendedAt.formatDateTime(),
        method = attendanceMethodUsed
    )
}