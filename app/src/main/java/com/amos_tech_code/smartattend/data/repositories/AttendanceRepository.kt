package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.LiveAttendanceSseClient
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.LiveAttendanceUpdate
import com.amos_tech_code.smartattend.domain.request.EndSessionRequest
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.StartSessionRequest
import com.amos_tech_code.smartattend.domain.request.UpdateSessionRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class AttendanceRepository(
    private val apiService: ApiService,
    private val sseClient: LiveAttendanceSseClient
) {

    /**
     * Lecture Attendance Implementation
     *
     */
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

    fun observeLiveAttendance(sessionId: String): Flow<LiveAttendanceUpdate> {
        return sseClient
            .connect(sessionId)
            .flowOn(Dispatchers.IO)
    }

    suspend fun resolveFlaggedStudent(studentId: String) : ApiResult<Unit> {
        return safeApiCall {
            apiService.resolveFlaggedStudent(studentId)
        }
    }

    /**
     * Student Attendance Implementation
     */

    suspend fun verifyAttendanceSession(request: VerifySessionRequest) : ApiResult<VerifyAttendanceResponse> {
        return safeApiCall {
            apiService.verifyAttendanceSession(request)
        }
    }

    suspend fun markAttendance(request: MarkAttendanceRequest) : ApiResult<MarkAttendanceResponse> {
        return safeApiCall {
            apiService.markAttendanceSession(request)
        }
    }

}