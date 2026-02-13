package com.amos_tech_code.smartattend.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.amos_tech_code.smartattend.data.local.room.dao.AttendanceDao
import com.amos_tech_code.smartattend.data.local.room.dao.StudentAttendanceStatsDao
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceStatsEntity
import com.amos_tech_code.smartattend.data.mappers.toEntity
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Calendar

class AttendanceSessionRepository (
    private val apiService: ApiService,
    private val attendanceDao: AttendanceDao,
    private val attendanceStatsDao: StudentAttendanceStatsDao,
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

    fun getAttendancePagingSource() :  Flow<PagingData<StudentAttendanceRecordEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                attendanceDao.pagingSource()
            }
        ).flow
    }

    /**
     * Sync Attendance Records from server
     */
    suspend fun syncStudentAttendanceRecords() : ApiResult<Unit> {

        return withContext(ioDispatcher) {
            try {
                var page = 0
                var hasNext = true

                while (hasNext && isActive) {
                    val result = safeApiCall { apiService.getAttendanceRecords(page) }

                    when (result) {
                        is ApiResult.Success -> {
                            // Save to local database
                            val response = result.data

                            attendanceDao.insertAll(response.records.map { it.toEntity() })

                            hasNext = response.hasNext
                            page++
                        }

                        is ApiResult.Failure -> {
                            return@withContext ApiResult.Failure(result.error)
                        }
                    }
                }

                ApiResult.Success(Unit)

            } catch (e: Exception) {
                return@withContext ApiResult.Failure(ApiError.UnknownError(e))
            }
        }
    }

    /**
     * Observe stats from local database and auto-refresh if stale
     */
    fun observeStats(): Flow<StudentAttendanceStatsEntity?> {
        return attendanceStatsDao.getStats()
    }

    /**
     * Refresh stats from API
     */
    suspend fun refreshStats(): ApiResult<StudentAttendanceStatsEntity> {
        return withContext(ioDispatcher) {
            try {
                val response = safeApiCall { apiService.getStudentAttendanceStats() }
                when (response) {
                    is ApiResult.Success -> {
                        val entity = StudentAttendanceStatsEntity(
                            totalSessions = response.data.totalSessions,
                            attendedSessions = response.data.attendedSessions,
                            currentStreak = response.data.currentStreak,
                            lastUpdated = System.currentTimeMillis()
                        )
                        attendanceStatsDao.insertOrUpdate(entity)
                        ApiResult.Success(entity)
                    }
                    is ApiResult.Failure -> ApiResult.Failure(response.error)
                }
            } catch (e: Exception) {
                ApiResult.Failure(ApiError.UnknownError(e))
            }
        }
    }

    /**
     * Calculate streak from local attendance records
     */
    suspend fun calculateCurrentStreak(): Int {
        val attendanceDays = attendanceDao.getDistinctAttendanceDays()
        if (attendanceDays.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val today = calendar.timeInMillis

        // Check if attended today
        val attendedToday = attendanceDays.any { isSameDay(it, today) }
        if (!attendedToday) return 0

        var streak = 1
        var expectedDate = today - 86400000 // Yesterday

        while (attendanceDays.any { isSameDay(it, expectedDate) }) {
            streak++
            expectedDate -= 86400000
        }

        return streak
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    suspend fun getTodaySessions(): List<StudentAttendanceRecordEntity> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return attendanceDao.getTodaySessions(startOfDay, endOfDay)
    }

    suspend fun getTotalSessionsCount(): Int = attendanceDao.getTotalSessionsCount()

    suspend fun getAttendedSessionsCount(): Int = attendanceDao.getAttendedSessionsCount()

    suspend fun getRecentAttendance(limit: Int): List<StudentAttendanceRecordEntity> =
        attendanceDao.getRecentAttendance(limit)

    suspend fun getCurrentStreak(): Int = calculateCurrentStreak()


}