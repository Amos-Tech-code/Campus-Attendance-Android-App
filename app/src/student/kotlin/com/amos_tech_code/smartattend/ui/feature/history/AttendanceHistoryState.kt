package com.amos_tech_code.smartattend.ui.feature.history

import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType

data class HistoryFilterState(
    val selectedUnit: String? = null,
    val selectedStatus: AttendanceSessionStatus? = null,
    val selectedType: AttendanceSessionType? = null,
    val selectedMethod: AttendanceMethod? = null,
    val showSuspiciousOnly: Boolean = false,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST
) {
    fun predicate(record: StudentAttendanceRecordEntity): Boolean {
        return when {
            selectedUnit != null && record.unitCode != selectedUnit -> false
            selectedStatus != null && record.status != selectedStatus -> false
            selectedType != null && record.sessionType != selectedType -> false
            selectedMethod != null && record.attendanceMethodUsed != selectedMethod -> false
            showSuspiciousOnly && !record.isSuspicious -> false
            else -> true
        }
    }
}

enum class SortOrder {
    NEWEST_FIRST, OLDEST_FIRST
}

data class AttendanceStats(
    val totalSessions: Int = 0,
    val attendedSessions: Int = 0,
    val scheduledSessions: Int = 0
)
