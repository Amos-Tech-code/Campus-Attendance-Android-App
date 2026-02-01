package com.amos_tech_code.smartattend.data.local.room_db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceSessionHistoryDao {

    @Query("""
        SELECT * FROM attendance_session_history 
        ORDER BY startedAt DESC
    """)
    fun pagingSource(): PagingSource<Int, AttendanceSessionHistoryEntity>

    @Query("""
        SELECT * FROM attendance_session_history 
        WHERE startedAt >= :startTime AND startedAt <= :endTime
        ORDER BY startedAt DESC
    """)
    fun observeSessionsInDateRange(startTime: Long, endTime: Long): Flow<List<AttendanceSessionHistoryEntity>>

    @Query("""
        SELECT * FROM attendance_session_history 
        ORDER BY startedAt DESC 
        LIMIT :limit
    """)
    fun observeRecentSessions(limit: Int): Flow<List<AttendanceSessionHistoryEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        sessions: List<AttendanceSessionHistoryEntity>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AttendanceSessionHistoryEntity)

    @Query("UPDATE attendance_session_history SET status = :status, endedAt = :endedAt  WHERE sessionId = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: AttendanceSessionStatus, endedAt: Long)

    @Query("DELETE FROM attendance_session_history")
    suspend fun clearAll()
}
