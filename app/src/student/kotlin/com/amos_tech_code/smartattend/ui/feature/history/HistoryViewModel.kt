package com.amos_tech_code.smartattend.ui.feature.history

import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.ui.feature.home.AttendanceMethod
import com.amos_tech_code.smartattend.ui.feature.home.AttendanceRecord
import com.amos_tech_code.smartattend.ui.feature.home.AttendanceStatus
import com.amos_tech_code.smartattend.ui.feature.home.CourseAttendance
import com.amos_tech_code.smartattend.ui.feature.home.OverallStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {

    private val _historyState = MutableStateFlow(AttendanceHistoryState())
    val historyState = _historyState.asStateFlow()

}

// History Screen State
data class AttendanceHistoryState(
    val overallStats: OverallStats = OverallStats(
        overallPercentage = 85.5f,
        presentDays = 17,
        absentDays = 2,
        lateDays = 1,
        totalDays = 20,
        streak = 5
    ),
    val courseAttendance: List<CourseAttendance> = listOf(
        CourseAttendance(
            courseId = "1",
            courseName = "Mobile Application Development",
            courseCode = "CS401",
            present = 8,
            absent = 1,
            total = 9,
            percentage = 88.9f
        ),
        CourseAttendance(
            courseId = "2",
            courseName = "Software Engineering",
            courseCode = "CS402",
            present = 7,
            absent = 1,
            total = 8,
            percentage = 87.5f
        ),
        CourseAttendance(
            courseId = "3",
            courseName = "Database Systems",
            courseCode = "CS301",
            present = 9,
            absent = 0,
            total = 9,
            percentage = 100f
        ),
        CourseAttendance(
            courseId = "4",
            courseName = "Computer Networks",
            courseCode = "CS302",
            present = 6,
            absent = 1,
            total = 7,
            percentage = 85.7f
        ),
        CourseAttendance(
            courseId = "5",
            courseName = "Algorithms",
            courseCode = "CS201",
            present = 5,
            absent = 0,
            total = 5,
            percentage = 100f
        )
    ),
    val recentRecords: List<AttendanceRecord> = listOf(
        AttendanceRecord(
            id = "1",
            courseName = "Mobile Application Development",
            date = "2024-01-15",
            time = "10:05 AM",
            status = AttendanceStatus.PRESENT,
            method = AttendanceMethod.QR_CODE,
            location = "Room 301, CS Building",
            verified = true
        ),
        AttendanceRecord(
            id = "2",
            courseName = "Software Engineering",
            date = "2024-01-15",
            time = "02:10 PM",
            status = AttendanceStatus.LATE,
            method = AttendanceMethod.MANUAL_CODE,
            location = "Room 205, Main Building",
            verified = true
        ),
        AttendanceRecord(
            id = "3",
            courseName = "Database Systems",
            date = "2024-01-14",
            time = "08:15 AM",
            status = AttendanceStatus.PRESENT,
            method = AttendanceMethod.QR_CODE,
            location = "Room 101, CS Building",
            verified = true
        ),
        AttendanceRecord(
            id = "4",
            courseName = "Computer Networks",
            date = "2024-01-13",
            time = "11:00 AM",
            status = AttendanceStatus.ABSENT,
            method = AttendanceMethod.GPS,
            location = null,
            verified = false
        ),
        AttendanceRecord(
            id = "5",
            courseName = "Algorithms",
            date = "2024-01-12",
            time = "09:50 AM",
            status = AttendanceStatus.PRESENT,
            method = AttendanceMethod.QR_CODE,
            location = "Room 402, CS Building",
            verified = true
        ),
        AttendanceRecord(
            id = "6",
            courseName = "Mobile Application Development",
            date = "2024-01-12",
            time = "10:00 AM",
            status = AttendanceStatus.PRESENT,
            method = AttendanceMethod.QR_CODE,
            location = "Room 301, CS Building",
            verified = true
        )
    ),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = false
)

// Filter for History Screen
enum class HistoryFilter {
    ALL, PRESENT, ABSENT, LATE, THIS_WEEK, THIS_MONTH
}
