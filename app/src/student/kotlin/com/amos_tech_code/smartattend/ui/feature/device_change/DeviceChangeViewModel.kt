package com.amos_tech_code.smartattend.ui.feature.device_change

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repositories.DeviceChangeRepository
import com.amos_tech_code.smartattend.domain.models.DeviceChangeStatus
import com.amos_tech_code.smartattend.domain.models.DeviceStatus
import com.amos_tech_code.smartattend.domain.request.DeviceInfoDto
import com.amos_tech_code.smartattend.domain.request.StudentDeviceChangeRequest
import com.amos_tech_code.smartattend.domain.response.DeviceChangeHistoryDto
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeviceChangeViewModel(
    private val repository: DeviceChangeRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val classTrackSession: ClassTrackSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceChangeUiState())
    val uiState: StateFlow<DeviceChangeUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<DeviceChangeEvent>()
    val event: SharedFlow<DeviceChangeEvent> = _event.asSharedFlow()

    init {
        loadDeviceChangeHistory()
        isCurrentDeviceActive()
    }

    fun loadDeviceChangeHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = repository.getDeviceChangeHistory()) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            history = result.data,
                            isLoading = false,
                            hasPendingRequest = result.data.any {
                                it.status == DeviceChangeStatus.PENDING
                            }
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(DeviceChangeEvent.ShowError(result.error.extractApiErrorMessage() ?: "Failed to load history"))
                }
            }
        }
    }

    fun requestDeviceChange(reason: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            val request = StudentDeviceChangeRequest(
                deviceInfo = DeviceInfoDto(
                    deviceId = deviceInfo.deviceId,
                    model = deviceInfo.model,
                    os = deviceInfo.os,
                    fcmToken = classTrackSession.getFCMToken()
                ),
                reason = reason
            )

            when (val result = repository.requestDeviceChange(request)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            showRequestDialog = false
                        )
                    }
                    loadDeviceChangeHistory() // Refresh history
                    _event.emit(DeviceChangeEvent.ShowSuccess("Device change request submitted successfully"))
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _event.emit(DeviceChangeEvent.ShowError(result.error.extractApiErrorMessage() ?: "Failed to submit request"))
                }
            }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }

            when (val result = repository.cancelDeviceChangeRequest(requestId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isCancelling = false) }
                    classTrackSession.updateDeviceStatus(DeviceStatus.REJECTED)
                    loadDeviceChangeHistory() // Refresh history
                    _event.emit(DeviceChangeEvent.ShowSuccess("Request cancelled successfully"))
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isCancelling = false) }
                    _event.emit(DeviceChangeEvent.ShowError(result.error.extractApiErrorMessage()))
                }
            }
        }
    }

    fun isCurrentDeviceActive() {
        viewModelScope.launch {
            classTrackSession.observeDeviceStatus().collect { newStatus ->
                val isCurrentDeviceActive = newStatus == DeviceStatus.ACTIVE
                _uiState.update { state ->
                    state.copy(isCurrentDeviceActive = isCurrentDeviceActive)
                }
            }
        }
    }

    fun setShowRequestDialog(show: Boolean) {
        _uiState.update { it.copy(showRequestDialog = show) }
    }

    fun setSelectedRequestForCancel(requestId: String?) {
        _uiState.update { it.copy(selectedRequestId = requestId) }
    }
}

data class DeviceChangeUiState(
    val history: List<DeviceChangeHistoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val isCurrentDeviceActive: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCancelling: Boolean = false,
    val hasPendingRequest: Boolean = false,
    val showRequestDialog: Boolean = false,
    val selectedRequestId: String? = null
)

sealed class DeviceChangeEvent {
    data class ShowError(val message: String) : DeviceChangeEvent()
    data class ShowSuccess(val message: String) : DeviceChangeEvent()
}