package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.ui.feature.home.DeviceInfo
import com.amos_tech_code.smartattend.ui.feature.home.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val session: SmartAttendSession
) : ViewModel() {

    private val _profileState = MutableStateFlow(StudentProfileState())
    val profileState = _profileState.asStateFlow()

    init {
        fetchData()
    }
    fun fetchData() {
        val studentName = session.getName()
        val registrationNo = session.getRegNo()

        _profileState.update {
            it.copy(
                student = Student(
                    name = studentName ?: "",
                    registrationNo = registrationNo ?: ""
                )
            )
        }

    }

    fun logOut() {
        //session.clearSession()
    }
}

// Profile Screen State
data class StudentProfileState(
    val student: Student = Student(
        name = "",
        registrationNo = "",
        email = "john.doe@student.university.edu",
        department = "Computer Science",
        semester = "4",
        profileImage = null
    ),
    val deviceInfo: DeviceInfo = DeviceInfo(
        deviceId = "SM-G998B-7A8B9C0D",
        deviceModel = "Samsung Galaxy S21 Ultra",
        lastLogin = "2024-01-15 09:45 AM",
        isCurrentDevice = true,
        registrationDate = "2024-01-10"
    ),
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false
)
