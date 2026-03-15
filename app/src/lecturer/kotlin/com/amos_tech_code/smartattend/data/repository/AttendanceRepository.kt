package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.LiveAttendanceSseClient
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.LiveAttendanceUpdate
import com.amos_tech_code.smartattend.domain.request.RemoveAttendanceRecordRequest
import com.amos_tech_code.smartattend.domain.response.GenericResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class AttendanceRepository(
    private val apiService: ApiService,
    private val sseClient: LiveAttendanceSseClient,
) {

    /**
     * Lecture Attendance Implementation
     *
     */
    fun observeLiveAttendance(sessionId: String): Flow<LiveAttendanceUpdate> {
        return sseClient
            .connect(sessionId)
            .flowOn(Dispatchers.IO)
    }

    suspend fun removeFlaggedStudent(request: RemoveAttendanceRecordRequest) : ApiResult<GenericResponse> {
        return safeApiCall {
            apiService.removeFlaggedStudent(request)
        }
    }


}