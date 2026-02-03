package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.local.room.dao.AttendanceDao
import com.amos_tech_code.smartattend.data.mappers.toEntity
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.StudentAttendanceHistoryResponse
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttendanceSessionRepository (
    private val apiService: ApiService,
    private val attendanceDao: AttendanceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Student Attendance Implementation
     */

    suspend fun markAttendance(request: MarkAttendanceRequest) : ApiResult<MarkAttendanceResponse> {
        return safeApiCall {
            apiService.markAttendanceSession(request)
        }
    }

    /**
     * Student AttendanceSession Network Implementation
     */

    suspend fun verifyAttendanceSession(request: VerifySessionRequest) : ApiResult<VerifyAttendanceResponse> {
        return safeApiCall {
            apiService.verifyAttendanceSession(request)
        }
    }


    /**
     * Local Datasource Operations + Network Sync Operations
     */
    suspend fun syncStudentAttendanceRecords() : ApiResult<StudentAttendanceHistoryResponse> {

        var page = 0
        var hasNext = true
        lateinit var result: ApiResult<StudentAttendanceHistoryResponse>

        while (hasNext) {
            result = safeApiCall { apiService.getAttendanceRecords(page) }

            if (result is ApiResult.Success) {
                try {
                    withContext(ioDispatcher) {
                        // Save to local database
                        val response = result.data

                        attendanceDao.insertAll(response.records.map { it.toEntity() })

                        hasNext = response.hasNext
                        page++
                    }
                } catch (e: Exception) {
                    //Log.e("AttendanceSessionRepository", "Error inserting attendance records into database", e)
                }
            } else {
                return result
            }
        }

        return result
    }
}