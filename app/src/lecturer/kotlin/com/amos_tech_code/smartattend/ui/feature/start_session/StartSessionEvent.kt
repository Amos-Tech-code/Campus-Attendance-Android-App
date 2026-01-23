package com.amos_tech_code.smartattend.ui.feature.start_session

import android.app.Activity
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel

sealed class StartSessionEvent {

    data class ShowErrorMessage(val message: String) : StartSessionEvent()
    object CompleteProfile : StartSessionEvent()

    object NavigateToLiveAttendance : StartSessionEvent()

    object RequestEnableGps : StartSessionEvent()
    data class LocationPermissionDenied(val shouldShowRationale: Boolean) : StartSessionEvent()
}

// UI Events
sealed class SessionUiEvent {
    data class ProgrammeSelectionChanged(val programme: Programme, val selected: Boolean) : SessionUiEvent()
    data class UnitSelected(val unit: UnitModel) : SessionUiEvent()
    data class DurationChanged(val minutes: Int) : SessionUiEvent()
    data class RadiusChanged(val radius: Int) : SessionUiEvent()
    data class WeekNumberChanged(val week: Int) : SessionUiEvent() // New
    data class TitleChanged(val title: String) : SessionUiEvent() // New
    data class SessionTypeChanged(val sessionType: AttendanceSessionType) : SessionUiEvent() // New
    object ToggleLocationRequirement : SessionUiEvent()
    object CaptureTeachingVenue : SessionUiEvent()
    data class TeachingVenueCaptured(val location: LocationData) : SessionUiEvent()
    data class LocationCaptureFailed(val error: String) : SessionUiEvent()
    data class AttendanceMethodChanged(val method: AttendanceMethod) : SessionUiEvent() // New
    object ShowProgrammeSelection : SessionUiEvent()
    object ShowUnitSelection : SessionUiEvent()
    object StartSession : SessionUiEvent()
}
