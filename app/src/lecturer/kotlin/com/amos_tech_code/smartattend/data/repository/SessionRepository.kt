package com.amos_tech_code.smartattend.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.amos_tech_code.smartattend.data.local.room_db.dao.AttendanceSessionHistoryDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity
import com.amos_tech_code.smartattend.data.mappers.toEntity
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.request.EndSessionRequest
import com.amos_tech_code.smartattend.domain.request.StartSessionRequest
import com.amos_tech_code.smartattend.domain.request.UpdateSessionRequest
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.ui.feature.session_history.SessionUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.util.Calendar
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SessionRepository(
    private val apiService: ApiService,
    private val sessionHistoryDao: AttendanceSessionHistoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Lecture AttendanceSession Implementation
     *
     */
    @OptIn(ExperimentalTime::class)
    suspend fun startAttendanceSession(request: StartSessionRequest) : ApiResult<StartAttendanceSessionResponse> {

        val result = safeApiCall { apiService.startAttendanceSession(request) }

        if(result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    val sessionEntity = result.data.toEntity()
                    sessionHistoryDao.insert(sessionEntity)
                }
            } catch (e: Exception) {
                //Log.e("SessionRepository", "Error inserting session into database", e)
            }
        }

        return result

    }

    suspend fun updateAttendanceSession(sessionId: String, request: UpdateSessionRequest) : ApiResult<StartAttendanceSessionResponse> {
        val result = safeApiCall {
            apiService.updateAttendanceSession(sessionId, request)
        }
        if(result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    val sessionEntity = result.data.toEntity()
                    sessionHistoryDao.insert(sessionEntity)
                }
            } catch (e: Exception) {
                //Log.e("SessionRepository", "Error inserting session into database", e)
            }
        }
        return result
    }

    suspend fun getActiveSession() : ApiResult<StartAttendanceSessionResponse> {
        return safeApiCall {
            apiService.getActiveSession()
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun endActiveSession(sessionId: String) : ApiResult<Unit> {

        val result = safeApiCall { apiService.endAttendanceSession(EndSessionRequest(sessionId)) }

        if (result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    sessionHistoryDao.updateSessionStatus(
                        sessionId = sessionId,
                        status = AttendanceSessionStatus.ENDED,
                        endedAt = Clock.System.now().toEpochMilliseconds()
                    )
                }
            } catch (e : Exception) {
                //Log.e("SessionRepository", "Error updating session status in database", e)
            }
        }
        return result
    }


    /**
     * Local Datasource Operations
     */

    fun getGroupedSessionsPaged(): Flow<PagingData<SessionUiModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                sessionHistoryDao.pagingSource()
            }
        ).flow
            .map { pagingData ->
                pagingData.map { entity ->
                    SessionUiModel.SessionItem(entity)
                }
                    .insertSeparators { before, after ->
                        val beforeItem = before?.session
                        val afterItem = after?.session

                        // At the top of the list, always add a header for the first item.
                        if (beforeItem == null && afterItem != null) {
                            return@insertSeparators createDateHeader(afterItem.startedAt)
                        }

                        if (beforeItem != null && afterItem != null) {
                            val beforeDate = getLocalDate(beforeItem.startedAt)
                            val afterDate = getLocalDate(afterItem.startedAt)

                            // If the date is different, insert a header for the 'after' item.
                            if (beforeDate != afterDate) {
                                return@insertSeparators createDateHeader(afterItem.startedAt)
                            }
                        }

                        // No separator needed
                        null
                    }
            }
    }

    fun observeTodaysSessions(): Flow<List<AttendanceSessionHistoryEntity>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfToday = calendar.timeInMillis

        return sessionHistoryDao.observeSessionsInDateRange(startOfToday, endOfToday)
    }

    fun observeRecentSessions(limit: Int = 5): Flow<List<AttendanceSessionHistoryEntity>> {
        return sessionHistoryDao.observeRecentSessions(limit)
    }

    // Helper to get the LocalDate from a timestamp
    @OptIn(ExperimentalTime::class)
    private fun getLocalDate(timestamp: Long): LocalDate {
        return Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    // New helper function to create the DateHeader model
    @OptIn(ExperimentalTime::class)
    private fun createDateHeader(timestamp: Long): SessionUiModel.DateHeader {
        val sessionDate = getLocalDate(timestamp)
        val today = Clock.System.todayIn(TimeZone.Companion.currentSystemDefault())
        val yesterday = today.minus(1, DateTimeUnit.Companion.DAY)
        val currentYear = today.year

        val displayDate = when (sessionDate) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                // Custom formatter for Kotlin's LocalDate
                formatLocalDate(sessionDate, currentYear)
            }
        }

        val keyDate = sessionDate.toString() // ISO format: "yyyy-MM-dd"

        return SessionUiModel.DateHeader(
            dateString = keyDate,
            displayDate = displayDate
        )
    }

    // Custom formatter to create a display string like "Saturday, Jan 31"
    private fun formatLocalDate(date: LocalDate, currentYear: Int): String {
        val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() }
        val month = date.month.name.lowercase().replaceFirstChar { it.titlecase() }.take(3)
        val datePart = "$dayOfWeek, $month ${date.day}"

        // Add year only if it's not current year
        return if (date.year == currentYear) {
            datePart
        } else {
            "$datePart ${date.year}"
        }
    }

    suspend fun syncSessionHistory(pageSize: Int = 10) : ApiResult<Unit> {
        return withContext(ioDispatcher) {
            try {
                var page = 0
                var hasNext = true

                while (hasNext && isActive) {
                    when (val result = safeApiCall {
                        apiService.getSessionsHistory(page = page, size = pageSize)
                    }) {
                        is ApiResult.Success -> {
                            val response = result.data
                            sessionHistoryDao.insertAll(
                                response.sessions.map { it.toEntity() }
                            )
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

}