package com.amos_tech_code.smartattend.ui.feature.start_session

import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.domain.models.response.StartAttendanceSessionResponse

data class SessionState(
    val universityId: String = "",
    val selectedProgrammes: List<Programme> = emptyList(),
    val selectedUnit: UnitModel? = null,
    val availableUnits: List<UnitModel> = emptyList(),
    val durationMinutes: Int = 30,
    val allowedRadius: Int = 50,
    val requireLocation: Boolean = true,
    val teachingVenue: LocationData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showProgrammeSelection: Boolean = false,
    val showUnitSelection: Boolean = false,
    val isCapturingLocation: Boolean = false,
    val locationError: String? = null
)




// New state for session success
data class SessionSuccessState(
    val sessionResponse: StartAttendanceSessionResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
