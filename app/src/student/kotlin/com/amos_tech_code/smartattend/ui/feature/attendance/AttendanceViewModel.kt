package com.amos_tech_code.smartattend.ui.feature.attendance

import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.ui.feature.home.AttendanceMethod
import com.amos_tech_code.smartattend.ui.feature.home.AttendanceStatus
import com.amos_tech_code.smartattend.ui.feature.home.RecentAttendance
import com.amos_tech_code.smartattend.ui.feature.home.Session
import com.amos_tech_code.smartattend.ui.feature.home.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AttendanceViewModel : ViewModel() {

    private val _attendanceState = MutableStateFlow(StudentAttendanceState())
    val attendanceState = _attendanceState.asStateFlow()

}

// Attendance Screen State
data class StudentAttendanceState(
    val activeSessions: List<Session> = listOf(
        Session(
            id = "1",
            courseName = "Mobile Application Development",
            courseCode = "CS401",
            time = "10:00 AM - 11:30 AM",
            location = "Room 301, CS Building",
            status = SessionStatus.ACTIVE,
            lecturer = "Dr. Smith"
        )
    ),
    val recentAttendance: List<RecentAttendance> = listOf(
        RecentAttendance(
            id = "1",
            courseName = "Database Systems",
            timestamp = "Today, 08:30 AM",
            status = AttendanceStatus.PRESENT,
            method = AttendanceMethod.QR_CODE,
            location = "Room 101, CS Building"
        ),
        RecentAttendance(
            id = "2",
            courseName = "Software Engineering",
            timestamp = "Yesterday, 02:15 PM",
            status = AttendanceStatus.LATE,
            method = AttendanceMethod.MANUAL_CODE,
            location = "Room 205, Main Building"
        ),
        RecentAttendance(
            id = "3",
            courseName = "Computer Networks",
            timestamp = "2 days ago, 11:00 AM",
            status = AttendanceStatus.ABSENT,
            method = AttendanceMethod.GPS,
            location = null
        ),
        RecentAttendance(
            id = "4",
            courseName = "Algorithms",
            timestamp = "3 days ago, 09:45 AM",
            status = AttendanceStatus.PRESENT,
            method = AttendanceMethod.QR_CODE,
            location = "Room 402, CS Building"
        )
    ),
    val isLoading: Boolean = false
)