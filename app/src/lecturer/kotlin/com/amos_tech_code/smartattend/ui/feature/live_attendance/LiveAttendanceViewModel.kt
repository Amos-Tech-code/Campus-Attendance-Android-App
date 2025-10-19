package com.amos_tech_code.smartattend.ui.feature.live_attendance

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class LiveAttendanceViewModel : ViewModel() {

    private val _state = MutableStateFlow(LiveAttendanceState())
    val state: StateFlow<LiveAttendanceState> = _state

    private val _event = Channel<LiveAttendanceEvent>()
    val event = _event.receiveAsFlow()

    fun fetchData() {

    }

    fun endSession() {
        _event.trySend(LiveAttendanceEvent.SessionEnded)

    }

}