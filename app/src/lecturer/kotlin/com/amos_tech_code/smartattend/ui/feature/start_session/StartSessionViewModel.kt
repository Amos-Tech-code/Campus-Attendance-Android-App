package com.amos_tech_code.smartattend.ui.feature.start_session

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.ui.feature.setup.Course
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class StartSessionViewModel(
    private val session: SmartAttendSession
) : ViewModel() {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state

    private val _event = Channel<StartSessionEvent>()
    val event = _event.receiveAsFlow()

    var showCompleteProfileDialog = mutableStateOf(false)
        private set

    init {
        checkProfileCompletion()
    }

    private fun checkProfileCompletion() {
        val isProfileComplete = session.isProfileComplete()
        if (!isProfileComplete) {
            showCompleteProfileDialog.value = true
        }

    }

    fun startSession() {
        // Testing
        _event.trySend(StartSessionEvent.SessionStarted("0"))
    }

    fun navigateToCompleteProfile() {
        _event.trySend(StartSessionEvent.CompleteProfile)
    }

}


// Session State
data class SessionState(
    val selectedCourse: Course? = null,
    val durationMinutes: Int = 30,
    val allowedRadius: Int = 50,
    val requireLocation: Boolean = true,
    val verifyDevices: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)