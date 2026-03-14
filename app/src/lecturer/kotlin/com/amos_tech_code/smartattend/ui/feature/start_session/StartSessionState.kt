package com.amos_tech_code.smartattend.ui.feature.start_session

import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse

data class SessionState(
    val universityId: String = "",
    val title: String = "",
    val sessionType: AttendanceSessionType = AttendanceSessionType.REGULAR,
    val weekNumber: Int = 1, // New: Week number
    val selectedProgrammes: List<Programme> = emptyList(),
    val selectedUnit: UnitModel? = null,
    val availableProgrammes: List<Programme> = emptyList(),
    val availableUnits: List<UnitModel> = emptyList(),
    val attendanceMethod: AttendanceMethod = AttendanceMethod.QR_CODE,
    val durationMinutes: Int = 30,
    val allowedRadius: Int = 50,
    val requireLocation: Boolean = false,
    val teachingVenue: LocationData? = null,
    val isLoading: Boolean = false, // For session start
    val isLoadingAcademic: Boolean = false, // For academic data loading
    val errorMessage: String? = null, // For session errors
    val academicError: String? = null, // For academic data errors
    val showProgrammeSelection: Boolean = false,
    val showUnitSelection: Boolean = false,
    val isCapturingLocation: Boolean = false,
    val locationError: String? = null
)


// State for session success
data class SessionSuccessState(
    val sessionResponse: StartAttendanceSessionResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
