package com.amos_tech_code.smartattend.ui.feature.session_history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class SessionHistoryViewModel : ViewModel() {

    private val _state = MutableStateFlow<SessionHistoryState>(SessionHistoryState.Loading)
    val state: StateFlow<SessionHistoryState> = _state

    private val _event = Channel<SessionHistoryEvent>()
    val event = _event.receiveAsFlow()

    fun fetchData() {

    }

}