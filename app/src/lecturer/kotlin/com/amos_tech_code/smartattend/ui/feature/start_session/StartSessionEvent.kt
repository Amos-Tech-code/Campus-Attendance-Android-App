package com.amos_tech_code.smartattend.ui.feature.start_session

import android.app.Activity
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.LocationPermissionState
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.domain.models.response.StartAttendanceSessionResponse

sealed class StartSessionEvent {

    data class ShowErrorMessage(val message: String) : StartSessionEvent()
    object CompleteProfile : StartSessionEvent()

    object NavigateToLiveAttendance : StartSessionEvent()

    //data class RequestPermission(val state: LocationPermissionState) : StartSessionEvent()

    object RequestEnableGps : StartSessionEvent()
}

// UI Events
sealed class SessionUiEvent {
    data class ProgrammeSelectionChanged(val programme: Programme, val selected: Boolean) : SessionUiEvent()
    data class UnitSelected(val unit: UnitModel) : SessionUiEvent()
    data class DurationChanged(val minutes: Int) : SessionUiEvent()
    data class RadiusChanged(val radius: Int) : SessionUiEvent()
    object ToggleLocationRequirement : SessionUiEvent()
    class CaptureTeachingVenue(val activity: Activity) : SessionUiEvent()
    data class TeachingVenueCaptured(val location: LocationData) : SessionUiEvent()
    data class LocationCaptureFailed(val error: String) : SessionUiEvent()
    object ShowProgrammeSelection : SessionUiEvent()
    object ShowUnitSelection : SessionUiEvent()
    object StartSession : SessionUiEvent()
}
