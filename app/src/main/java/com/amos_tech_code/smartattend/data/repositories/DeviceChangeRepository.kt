package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.DeviceChangeApprovalRequest
import com.amos_tech_code.smartattend.domain.request.StudentDeviceChangeRequest
import com.amos_tech_code.smartattend.domain.response.DeviceChangeHistoryDto
import com.amos_tech_code.smartattend.domain.response.DeviceChangeRequestResponse
import com.amos_tech_code.smartattend.domain.response.GenericResponse
import com.amos_tech_code.smartattend.domain.response.PendingDeviceChangeDto

class DeviceChangeRepository(
    private val apiService: ApiService
) {

    /**
     * Student: Request a device change
     */
    suspend fun requestDeviceChange(request: StudentDeviceChangeRequest): ApiResult<DeviceChangeRequestResponse> {
        return safeApiCall {
            apiService.requestDeviceChange(request)
        }
    }

    /**
     * Student: Get device change request history
     */
    suspend fun getDeviceChangeHistory(): ApiResult<List<DeviceChangeHistoryDto>> {
        return safeApiCall {
            apiService.getDeviceChangeHistory()
        }
    }

    /**
     * Student: Cancel device change request
     */
    suspend fun cancelDeviceChangeRequest(requestId: String): ApiResult<GenericResponse> {
        return safeApiCall {
            apiService.cancelDeviceChangeRequest(requestId)
        }
    }

    /**
     * Lecturer: Get pending device change requests from students
     */
    suspend fun getPendingDeviceChangeRequests(): ApiResult<List<PendingDeviceChangeDto>> {
        return safeApiCall {
            apiService.getPendingRequests()
        }
    }

    /**
     * Lecturer: Approve or decline a student's device change request
     */
    suspend fun reviewDeviceChange(request: DeviceChangeApprovalRequest): ApiResult<DeviceChangeRequestResponse> {
        return safeApiCall {
            apiService.reviewDeviceChange(request)
        }
    }

}