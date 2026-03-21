package com.amos_tech_code.smartattend.ui.feature.device_change

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repositories.DeviceChangeRepository
import com.amos_tech_code.smartattend.domain.request.DeviceChangeApprovalRequest
import com.amos_tech_code.smartattend.domain.response.PendingDeviceChangeDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LecturerDeviceChangeViewModel(
    private val repository: DeviceChangeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LecturerDeviceChangeUiState())
    val uiState: StateFlow<LecturerDeviceChangeUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<LecturerDeviceChangeEvent>()
    val event: SharedFlow<LecturerDeviceChangeEvent> = _event.asSharedFlow()

    init {
        loadPendingRequests()
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = repository.getPendingDeviceChangeRequests()) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            pendingRequests = result.data,
                            isLoading = false,
                            isEmpty = result.data.isEmpty()
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(LecturerDeviceChangeEvent.ShowError(
                        result.error.extractApiErrorMessage()
                    ))
                }
            }
        }
    }

    fun showRequestDetails(request: PendingDeviceChangeDto) {
        _uiState.update { it.copy(selectedRequest = request, showDetailsDialog = true) }
    }

    fun dismissDetailsDialog() {
        _uiState.update { it.copy(showDetailsDialog = false, selectedRequest = null) }
    }

    fun approveRequest(request: PendingDeviceChangeDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val approvalRequest = DeviceChangeApprovalRequest(
                requestId = request.requestId,
                approve = true,
                rejectionReason = null
            )

            when (val result = repository.reviewDeviceChange(approvalRequest)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isProcessing = false, showDetailsDialog = false) }
                    loadPendingRequests() // Refresh list
                    _event.emit(LecturerDeviceChangeEvent.ShowSuccess(
                        "Device change request approved successfully"
                    ))
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isProcessing = false) }
                    _event.emit(LecturerDeviceChangeEvent.ShowError(
                        result.error.extractApiErrorMessage()
                    ))
                }
            }
        }
    }

    fun rejectRequest(request: PendingDeviceChangeDto, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val approvalRequest = DeviceChangeApprovalRequest(
                requestId = request.requestId,
                approve = false,
                rejectionReason = reason
            )

            when (val result = repository.reviewDeviceChange(approvalRequest)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isProcessing = false, showDetailsDialog = false) }
                    loadPendingRequests() // Refresh list
                    _event.emit(LecturerDeviceChangeEvent.ShowSuccess(
                        "Device change request rejected"
                    ))
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isProcessing = false) }
                    _event.emit(LecturerDeviceChangeEvent.ShowError(
                        result.error.extractApiErrorMessage()
                    ))
                }
            }
        }
    }

    fun setShowRejectDialog(show: Boolean) {
        _uiState.update { it.copy(showRejectDialog = show) }
    }

    fun setRejectionReason(reason: String) {
        _uiState.update { it.copy(rejectionReason = reason) }
    }
}

data class LecturerDeviceChangeUiState(
    val pendingRequests: List<PendingDeviceChangeDto> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val isEmpty: Boolean = false,
    val selectedRequest: PendingDeviceChangeDto? = null,
    val showDetailsDialog: Boolean = false,
    val showRejectDialog: Boolean = false,
    val rejectionReason: String = ""
)

sealed class LecturerDeviceChangeEvent {
    data class ShowError(val message: String) : LecturerDeviceChangeEvent()
    data class ShowSuccess(val message: String) : LecturerDeviceChangeEvent()
}