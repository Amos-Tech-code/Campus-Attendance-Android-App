package com.amos_tech_code.smartattend.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM student_attendance_records ORDER BY attendedAt DESC")
    fun pagingSource(): PagingSource<Int, StudentAttendanceRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StudentAttendanceRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<StudentAttendanceRecordEntity>)

    @Query("SELECT DISTINCT attendedAt FROM student_attendance_records ORDER BY attendedAt DESC")
    suspend fun getDistinctAttendanceDays(): List<Long>

    @Query("SELECT COUNT(*) FROM student_attendance_records")
    suspend fun getTotalSessionsCount(): Int

    @Query("SELECT COUNT(*) FROM student_attendance_records")
    suspend fun getAttendedSessionsCount(): Int // All records are attended sessions

    @Query("SELECT * FROM student_attendance_records WHERE attendedAt >= :startOfDay AND attendedAt <= :endOfDay")
    suspend fun getTodaySessions(startOfDay: Long, endOfDay: Long): List<StudentAttendanceRecordEntity>

    @Query("SELECT * FROM student_attendance_records ORDER BY attendedAt DESC LIMIT :limit")
    suspend fun getRecentAttendance(limit: Int): List<StudentAttendanceRecordEntity>

    @Query("DELETE FROM student_attendance_records")
    suspend fun clearAll()

}
