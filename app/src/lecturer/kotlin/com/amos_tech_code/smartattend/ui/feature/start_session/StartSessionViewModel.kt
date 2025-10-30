package com.amos_tech_code.smartattend.ui.feature.start_session

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repositories.AttendanceRepository
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.domain.models.request.AttendanceMethodRequest
import com.amos_tech_code.smartattend.domain.models.request.StartSessionRequest
import com.amos_tech_code.smartattend.services.LocationService
import com.amos_tech_code.smartattend.utils.LocationServiceException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StartSessionViewModel(
    private val session: SmartAttendSession,
    val locationService: LocationService,
    private val attendanceRepository: AttendanceRepository,
    private val academicSetUpRepository: AcademicSetUpRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state

    private val _successState = MutableStateFlow(SessionSuccessState())
    val successState: StateFlow<SessionSuccessState> = _successState

    private val _event = Channel<StartSessionEvent>()
    val event = _event.receiveAsFlow()

    var showCompleteProfileDialog = mutableStateOf(false)
        private set

    var allProgrammes: List<Programme> = emptyList()

    init {
        loadAcademicSetup()
    }

    fun onEvent(event: SessionUiEvent) {
        when (event) {
            is SessionUiEvent.ProgrammeSelectionChanged -> {
                updateProgrammeSelection(event.programme, event.selected)
            }
            is SessionUiEvent.UnitSelected -> {
                _state.update { it.copy(selectedUnit = event.unit) }
            }
            is SessionUiEvent.DurationChanged -> {
                _state.update { it.copy(durationMinutes = event.minutes) }
            }
            is SessionUiEvent.RadiusChanged -> {
                _state.update { it.copy(allowedRadius = event.radius) }
            }
            SessionUiEvent.ToggleLocationRequirement -> {
                val newRequireLocation = !_state.value.requireLocation
                _state.update { it.copy(requireLocation = newRequireLocation) }

                // If turning off location, clear the teaching venue
                if (!newRequireLocation) {
                    _state.update { it.copy(teachingVenue = null) }
                }
            }

            is SessionUiEvent.CaptureTeachingVenue -> {
                captureTeachingVenue(event.activity)
            }

            is SessionUiEvent.TeachingVenueCaptured -> {
                _state.update {
                    it.copy(
                        teachingVenue = event.location,
                        isCapturingLocation = false,
                        locationError = null
                    )
                }
            }

            is SessionUiEvent.LocationCaptureFailed -> {
                _state.update {
                    it.copy(
                        isCapturingLocation = false,
                        locationError = event.error
                    )
                }
                _event.trySend(StartSessionEvent.ShowErrorMessage("Failed to capture location: ${event.error}"))
            }

            SessionUiEvent.ShowProgrammeSelection -> {
                _state.update { it.copy(showProgrammeSelection = true) }
            }
            SessionUiEvent.ShowUnitSelection -> {
                _state.update { it.copy(showUnitSelection = true) }
            }
            SessionUiEvent.StartSession -> {
                startSession()
            }
        }
    }

    private fun startSession() {
        val state = _state.value

        // Validation
        if (state.selectedProgrammes.isEmpty()) {
            _event.trySend(StartSessionEvent.ShowErrorMessage("Please select at least one programme"))
            return
        }

        if (state.selectedUnit == null) {
            _event.trySend(StartSessionEvent.ShowErrorMessage("Please select a unit"))
            return
        }
        // Location validation when required
        if (state.requireLocation) {
            if (state.teachingVenue == null) {
                _event.trySend(StartSessionEvent.ShowErrorMessage("Please capture teaching venue location"))
                return
            }

            // Additional validation: location shouldn't be too old
            val locationAge = System.currentTimeMillis() - state.teachingVenue.timestamp
            if (locationAge > 30 * 60 * 1000) {
                _event.trySend(StartSessionEvent.ShowErrorMessage("Location is too old. Please recapture current location"))
                return
            }
        }

        // Start session logic
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true)}
            try {
                val result = attendanceRepository.startAttendanceSession(
                    StartSessionRequest(
                        universityId = state.universityId,
                        programmeIds = state.selectedProgrammes.map { it.id },
                        unitId = state.selectedUnit.id,
                        method = AttendanceMethodRequest.QR_CODE,
                        locationLat = state.teachingVenue?.latitude,
                        locationLng = state.teachingVenue?.longitude,
                        radiusMeters = state.allowedRadius,
                        durationMinutes = state.durationMinutes
                    )
                )

                when (result) {
                    is ApiResult.Failure -> {
                        when(val error = result.error) {
                            is ApiError.HttpError -> {
                                _event.send(StartSessionEvent.ShowErrorMessage("Failed to start session. ${error.message}"))
                            }
                            is ApiError.NetworkError -> {
                                _event.send(StartSessionEvent.ShowErrorMessage("Failed to start session. ${error.exception.message}"))
                            }
                            is ApiError.UnknownError -> {
                                _event.send(StartSessionEvent.ShowErrorMessage("Failed to start session. Please try again"))
                            }
                        }
                    }
                    is ApiResult.Success -> {
                        _successState.update {
                            SessionSuccessState(sessionResponse = result.data)
                        }
                    }
                }

            } catch (e: Exception) {
                _event.send(StartSessionEvent.ShowErrorMessage("Failed to start session."))
            } finally {
                _state.update { it.copy(isLoading = false)}
            }

        }
    }

    private fun loadAcademicSetup() {
        val isProfileComplete = session.isProfileComplete()
        if (!isProfileComplete) {
            showCompleteProfileDialog.value = true
        } else {
            viewModelScope.launch {
                try {
                    val university = academicSetUpRepository.getActiveUniversityAcademics()
                    university?.let {
                        allProgrammes = it.programmes
                        _state.update { currentState ->
                            currentState.copy(
                                universityId = it.id,
                                availableUnits = getCommonUnits(emptyList()) // Start with empty selection
                            )
                        }
                    }
                } catch (e: Exception) {
                    _event.send(StartSessionEvent.ShowErrorMessage("Failed to retrieve academic data."))
                }
            }
        }
    }

    private fun updateProgrammeSelection(programme: Programme, selected: Boolean) {
        val updatedSelection = if (selected) {
            _state.value.selectedProgrammes + programme
        } else {
            _state.value.selectedProgrammes - programme
        }

        val commonUnits = getCommonUnits(updatedSelection)
        val updatedSelectedUnit = if (_state.value.selectedUnit in commonUnits) {
            _state.value.selectedUnit
        } else {
            null
        }

        _state.update {
            it.copy(
                selectedProgrammes = updatedSelection,
                availableUnits = commonUnits,
                selectedUnit = updatedSelectedUnit
            )
        }
    }

    private fun getCommonUnits(selectedProgrammes: List<Programme>): List<UnitModel> {
        return if (selectedProgrammes.isEmpty()) {
            emptyList()
        } else {
            selectedProgrammes
                .map { it.units.map { unit -> unit.id } }
                .reduce { acc, unitIds -> acc.intersect(unitIds).toList() }
                .mapNotNull { unitId ->
                    selectedProgrammes.flatMap { it.units }.find { it.id == unitId }
                }
                .distinctBy { it.id }
        }
    }

    private fun captureTeachingVenue(activity: Activity) {
        _state.update { it.copy(isCapturingLocation = true, locationError = null) }

        viewModelScope.launch {
            // 1. Check for location permissions first
            if (locationService.shouldRequestLocationPermission()) {
                val permissionState = locationService.getPermissionState(activity)
                //_event.send(StartSessionEvent.RequestPermission(permissionState))
                // Stop here; the user needs to grant permission first.
                // We also reset the loading state as the capture process is paused.
                _state.update { it.copy(isCapturingLocation = false) }
                return@launch
            }

            // 2. Check if the device's GPS is enabled
            if (!locationService.isLocationEnabled()) {
                _event.send(StartSessionEvent.RequestEnableGps)
                // Stop here; the user needs to enable GPS.
                // Reset loading state.
                _state.update { it.copy(isCapturingLocation = false) }
                return@launch
            }

            // 3. Both checks passed, proceed to get location
            try {
                val location = locationService.getCurrentLocation()

                // Getting the address is a separate network call, handle its potential failure
                val address = try {
                    locationService.getAddressFromLocation(location.latitude, location.longitude)
                } catch (e: Exception) {
                    // Log the geocoding error, but don't fail the whole operation
                    Log.w("StartSessionViewModel", "Failed to get address from geocoder", e)
                    null // Address is optional
                }

                val locationData = location.copy(address = address)
                onEvent(SessionUiEvent.TeachingVenueCaptured(locationData))

            } catch (e: Exception) {
                //Log.e("StartSessionViewModel", "Failed to get location", e)
                val errorMessage = when (e) {
                    is SecurityException -> "Location permission denied."
                    is LocationServiceException -> "Could not get location. Please try again."
                    else -> "An unknown error occurred while capturing location."
                }
                onEvent(SessionUiEvent.LocationCaptureFailed(errorMessage))
            }
        }
    }

    // Handle permission result from Activity
    fun onLocationPermissionResult(granted: Boolean, activity: Activity) {
        if (granted) {
            // Retry location capture if permission was just granted
            if (_state.value.requireLocation && _state.value.teachingVenue == null) {
                captureTeachingVenue(activity)
            }
        } else {
            _state.update {
                it.copy(
                    isCapturingLocation = false,
                    locationError = "Location permission is required to capture teaching venue"
                )
            }
        }
    }

    fun dismissProgrammeSelection() {
        _state.update { it.copy(showProgrammeSelection = false) }
    }

    fun dismissUnitSelection() {
        _state.update { it.copy(showUnitSelection = false) }
    }

    fun navigateToCompleteProfile() {
        _event.trySend(StartSessionEvent.CompleteProfile)
    }

    fun navigateToLiveAttendance() {
        _event.trySend(StartSessionEvent.NavigateToLiveAttendance)
    }

    fun clearSuccessState() {
        _successState.update { SessionSuccessState() }
    }

}