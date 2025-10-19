package com.amos_tech_code.smartattend.ui.feature.home

import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.ui.feature.setup.ActivityStatus
import com.amos_tech_code.smartattend.ui.feature.setup.ActivityType
import com.amos_tech_code.smartattend.ui.feature.setup.LecturerActivity
import com.amos_tech_code.smartattend.ui.feature.setup.LecturerSession
import com.amos_tech_code.smartattend.ui.feature.setup.SessionStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val session: SmartAttendSession
) : ViewModel() {

    private val _state = MutableStateFlow(LecturerHomeState())
    val state: StateFlow<LecturerHomeState> = _state

    private val _event = Channel<HomeEvent>()
    val event = _event.receiveAsFlow()

    init {
        fetchUserSession()
    }

    fun fetchUserSession() {

        val lecturerName = session.getName() ?: ""
        val profileCompleted = session.isProfileComplete()

        _state.update { it.copy(
            lecturerName = lecturerName,
            isProfileComplete = profileCompleted,
            quickStats = LecturerQuickStats(
                totalStudents = 100,
                activeSessions = 20,
                attendanceRate = 75f,
                pendingActions = 5
            ),
            todaySessions = listOf(
                LecturerSession(
                    id = "1",
                    courseName = "Introduction to Programming",
                    time = "10:00 am",
                    location = "Room 101",
                    status = SessionStatus.ACTIVE,
                    courseCode = "CS101",
                    sessionCode = "ABC123",
                    lecturer = this.state.value.lecturerName,
                ),
            ),
            recentActivities = listOf(
                LecturerActivity(
                    id = "1",
                    title = "Attendance Marked",
                    description = "Student John Doe marked attendance",
                    timestamp = "2 hours ago",
                    type = ActivityType.ATTENDANCE_MARKED,
                    status = ActivityStatus.SUCCESS
                )
            ),
            unreadNotifications = 5
        ) }
     }


    fun navigateToCompleteProfile() {
        _event.trySend(HomeEvent.CompleteProfile)
    }

}


// Home State
data class LecturerHomeState(
    val lecturerName: String = "",
    val isProfileComplete: Boolean = true,
    val institution: String = "Murang'a University of Technology",
    val quickStats: LecturerQuickStats = LecturerQuickStats(),
    val todaySessions: List<LecturerSession> = emptyList(),
    val recentActivities: List<LecturerActivity> = emptyList(),
    val unreadNotifications: Int = 0
)

data class LecturerQuickStats(
    val totalStudents: Int = 0,
    val activeSessions: Int = 0,
    val attendanceRate: Float = 0f,
    val pendingActions: Int = 0
)
