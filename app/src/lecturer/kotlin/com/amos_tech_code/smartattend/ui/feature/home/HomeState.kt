package com.amos_tech_code.smartattend.ui.feature.home

import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity
import com.amos_tech_code.smartattend.models.UniversityWithStats

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object NoInstitutionSetup : HomeUiState()
    data class SetupComplete(
        val lecturerName: String,
        val allUniversities: List<UniversityWithStats>,
        val activeUniversity: UniversityWithStats?,
        val todaysSessions: List<AttendanceSessionHistoryEntity>,
        val totalNotifications: Int = 0,
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}