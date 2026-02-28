package com.amos_tech_code.smartattend.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM student_attendance_records ORDER BY attendedAt DESC")
    fun pagingSource(): PagingSource<Int, StudentAttendanceRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StudentAttendanceRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<StudentAttendanceRecordEntity>)

    @Query("SELECT * FROM student_attendance_records WHERE attendedAt >= :startOfDay AND attendedAt <= :endOfDay")
    fun observeTodaySessions(startOfDay: Long, endOfDay: Long): Flow<List<StudentAttendanceRecordEntity>>

    @Query("SELECT * FROM student_attendance_records ORDER BY attendedAt DESC LIMIT :limit")
    fun observeRecentAttendance(limit: Int): Flow<List<StudentAttendanceRecordEntity>>

    @Query("DELETE FROM student_attendance_records")
    suspend fun clearAll()

}
