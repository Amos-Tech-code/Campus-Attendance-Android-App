package com.amos_tech_code.smartattend.ui.feature.home

import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val session: SmartAttendSession,
) : ViewModel() {

    private val _homeState = MutableStateFlow(StudentHomeState())
    val homeState: StateFlow<StudentHomeState> = _homeState

    private val _event = Channel<HomeEvent>()
    val event = _event.receiveAsFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        val studentName = session.getName()
        val registrationNo = session.getRegNo()

        _homeState.update {
            it.copy(
                studentName = studentName ?: "",
                registrationNo = registrationNo ?: ""
            )
        }

    }

}


// Home Screen State
data class StudentHomeState(
    val studentName: String = "",
    val registrationNo: String = "",
    val todaySessions: List<Session> = listOf(
        Session(
            id = "1",
            courseName = "Mobile Application Development",
            courseCode = "CS401",
            time = "10:00 AM - 11:30 AM",
            location = "Room 301, CS Building",
            status = SessionStatus.ACTIVE,
            lecturer = "Dr. Smith"
        ),
        Session(
            id = "2",
            courseName = "Software Engineering",
            courseCode = "CS402",
            time = "02:00 PM - 03:30 PM",
            location = "Room 205, Main Building",
            status = SessionStatus.UPCOMING,
            lecturer = "Prof. Johnson"
        ),
        Session(
            id = "3",
            courseName = "Database Systems",
            courseCode = "CS301",
            time = "08:00 AM - 09:30 AM",
            location = "Room 101, CS Building",
            status = SessionStatus.COMPLETED,
            lecturer = "Dr. Williams"
        )
    ),
    val attendanceStats: AttendanceStats = AttendanceStats(
        overallPercentage = 85.0f,
        presentCount = 17,
        absentCount = 2,
        lateCount = 1,
        totalSessions = 20
    ),
    val recentActivities: List<Activity> = listOf(
        Activity(
            id = "1",
            type = ActivityType.ATTENDANCE_MARKED,
            title = "Attendance Marked",
            description = "Successfully marked attendance for Mobile App Development",
            timestamp = "2 hours ago",
            courseName = "CS401",
            status = ActivityStatus.SUCCESS
        ),
        Activity(
            id = "2",
            type = ActivityType.NEW_SESSION,
            title = "New Session Available",
            description = "Software Engineering session starts at 2:00 PM",
            timestamp = "4 hours ago",
            courseName = "CS402",
            status = ActivityStatus.INFO
        ),
        Activity(
            id = "3",
            type = ActivityType.ATTENDANCE_FAILED,
            title = "Attendance Failed",
            description = "GPS location mismatch for Database Systems",
            timestamp = "1 day ago",
            courseName = "CS301",
            status = ActivityStatus.ERROR
        ),
        Activity(
            id = "4",
            type = ActivityType.DEVICE_CHANGE,
            title = "New Device Login",
            description = "Logged in from new device - flagged for review",
            timestamp = "2 days ago",
            status = ActivityStatus.WARNING
        )
    ),
    val unreadNotifications: Int = 2
)


// Data Classes to represent the data
data class Session(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val time: String,
    val location: String,
    val status: SessionStatus,
    val lecturer: String
)

enum class SessionStatus {
    ACTIVE, UPCOMING, COMPLETED, MISSED;

    val displayName: String
        get() = when (this) {
            ACTIVE -> "Active Now"
            UPCOMING -> "Upcoming"
            COMPLETED -> "Completed"
            MISSED -> "Missed"
        }
}

data class Activity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: String,
    val courseName: String? = null,
    val status: ActivityStatus = ActivityStatus.INFO
)

enum class ActivityType {
    ATTENDANCE_MARKED, ATTENDANCE_FAILED, NEW_SESSION, DEVICE_CHANGE, SYSTEM_ALERT
}

enum class ActivityStatus {
    SUCCESS, WARNING, ERROR, INFO
}

data class AttendanceStats(
    val overallPercentage: Float = 0f,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val totalSessions: Int = 0
)

data class RecentAttendance(
    val id: String,
    val courseName: String,
    val timestamp: String,
    val status: AttendanceStatus,
    val method: AttendanceMethod,
    val location: String? = null
)

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, PENDING
}

enum class AttendanceMethod {
    QR_CODE, MANUAL_CODE, GPS, LECTURER_MANUAL
}

data class CourseAttendance(
    val courseId: String,
    val courseName: String,
    val courseCode: String,
    val present: Int,
    val absent: Int,
    val total: Int,
    val percentage: Float
)

data class AttendanceRecord(
    val id: String,
    val courseName: String,
    val date: String,
    val time: String,
    val status: AttendanceStatus,
    val method: AttendanceMethod,
    val location: String?,
    val verified: Boolean
)

data class OverallStats(
    val overallPercentage: Float,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,
    val totalDays: Int,
    val streak: Int
)

data class Student(
    val name: String = "",
    val registrationNo: String = "",
    val email: String = "",
    val department: String = "",
    val semester: String = "",
    val profileImage: String? = null
)

data class DeviceInfo(
    val deviceId: String = "",
    val deviceModel: String = "",
    val lastLogin: String = "",
    val isCurrentDevice: Boolean = true,
    val registrationDate: String = ""
)