package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.request.EndSessionRequest
import com.amos_tech_code.smartattend.domain.models.request.StartSessionRequest
import com.amos_tech_code.smartattend.domain.models.request.UpdateSessionRequest
import com.amos_tech_code.smartattend.domain.models.response.StartAttendanceSessionResponse

class AttendanceRepository(
    private val apiService: ApiService
) {

    suspend fun startAttendanceSession(request: StartSessionRequest) : ApiResult<StartAttendanceSessionResponse> {
        return safeApiCall {
            apiService.startAttendanceSession(request)
        }
    }

    suspend fun updateAttendanceSession(sessionId: String, request: UpdateSessionRequest) : ApiResult<StartAttendanceSessionResponse> {
        return safeApiCall {
            apiService.updateAttendanceSession(sessionId, request)
        }
    }

    suspend fun getActiveSession() : ApiResult<StartAttendanceSessionResponse> {
        return safeApiCall {
            apiService.getActiveSession()
        }
    }

    suspend fun endActiveSession(sessionId: String) : ApiResult<Unit> {
        return safeApiCall {
            apiService.endAttendanceSession(
                EndSessionRequest(sessionId)
            )
        }

    }

}