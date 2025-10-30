package com.amos_tech_code.smartattend.ui.feature.live_attendance

import com.amos_tech_code.smartattend.domain.models.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.ui.feature.setup.AttendanceMethod
import com.amos_tech_code.smartattend.ui.feature.setup.Session
import com.amos_tech_code.smartattend.ui.feature.setup.Student

// Live Attendance State
data class LiveAttendanceState(
    val session: StartAttendanceSessionResponse? = null,
    val presentStudents: List<StudentAttendance> = emptyList(),
    val flaggedStudents: List<StudentAttendance> = emptyList(),
    val attendanceStats: AttendanceStats = AttendanceStats(),
    val showQrCode: Boolean = false,
    val isLoading: Boolean = false
)

data class StudentAttendance(
    val student: Student,
    val timestamp: String,
    val method: AttendanceMethod,
    val distance: Int? = null,
    val deviceVerified: Boolean = true,
    val locationVerified: Boolean = true
)

data class AttendanceStats(
    val totalStudents: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val attendancePercentage: Float = 0f
)