package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.ui.feature.home.Student
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val session: ClassTrackSession
) : ViewModel() {

    private val _profileState = MutableStateFlow(StudentProfileState())
    val profileState = _profileState.asStateFlow()

    private val _event = Channel<ProfileEvent>()
    val event = _event.receiveAsFlow()

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
                ),
                deviceInfo = DeviceInfoUiState(
                    deviceId = session.getDeviceId() ?: "",
                    deviceModel = session.getDeviceModel() ?: "",
                    //lastLogin = session.getLastLogin() ?: "",
                    //isCurrentDevice = session.isCurrentDevice() ?: false,
                    //registrationDate = session.getRegistrationDate() ?: ""
                )
            )
        }

    }

    fun logOut() {
        _profileState.update {
            it.copy(isLoggingOut = true)
        }
        session.clearSession()
        _profileState.update {
            it.copy(isLoggingOut = false)
        }
        _event.trySend(ProfileEvent.NavigateToLogin)
    }
}

// Profile Screen State
data class StudentProfileState(
    val student: Student = Student(
        name = "",
        registrationNo = "",
        email = "",
        department = "",
        semester = "",
        profileImage = null
    ),
    val deviceInfo: DeviceInfoUiState = DeviceInfoUiState(
        deviceId = "",
        deviceModel = "",
        lastLogin = "",
        isCurrentDevice = true,
        registrationDate = ""
    ),
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false
)

data class DeviceInfoUiState(
    val deviceId: String = "",
    val deviceModel: String = "",
    val lastLogin: String = "",
    val isCurrentDevice: Boolean = true,
    val registrationDate: String = ""
)
