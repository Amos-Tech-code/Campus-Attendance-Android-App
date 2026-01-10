package com.amos_tech_code.smartattend.data.network.utils

import com.amos_tech_code.smartattend.domain.response.AttendanceMarkedEventDto
import com.amos_tech_code.smartattend.domain.response.LiveAttendanceSnapshot

sealed interface LiveAttendanceUpdate {
    data class InitialState(
        val snapshot: LiveAttendanceSnapshot
    ) : LiveAttendanceUpdate

    data class AttendanceMarked(
        val event: AttendanceMarkedEventDto
    ) : LiveAttendanceUpdate
}
