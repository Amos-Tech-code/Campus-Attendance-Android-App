package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.ui.feature.home.DeviceInfo
import com.amos_tech_code.smartattend.ui.feature.home.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow(StudentProfileState())
    val profileState = _profileState.asStateFlow()
}

// Profile Screen State
data class StudentProfileState(
    val student: Student = Student(
        name = "John Doe",
        registrationNo = "U123/2021",
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
