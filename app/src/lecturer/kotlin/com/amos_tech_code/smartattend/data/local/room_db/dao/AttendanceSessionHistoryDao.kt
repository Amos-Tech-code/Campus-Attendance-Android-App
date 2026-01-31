package com.amos_tech_code.smartattend.data.local.room_db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity

@Dao
interface AttendanceSessionHistoryDao {

    @Query("""
        SELECT * FROM attendance_session_history
        ORDER BY startedAt DESC
    """)
    fun observeSessions(): Flow<List<AttendanceSessionHistoryEntity>>

    @Query("""
        SELECT * FROM attendance_session_history 
        ORDER BY startedAt DESC
    """)
    fun pagingSource(): PagingSource<Int, AttendanceSessionHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        sessions: List<AttendanceSessionHistoryEntity>
    )

    @Query("DELETE FROM attendance_session_history")
    suspend fun clearAll()
}
