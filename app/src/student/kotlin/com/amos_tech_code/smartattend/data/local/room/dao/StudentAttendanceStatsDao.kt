package com.amos_tech_code.smartattend.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentAttendanceStatsDao {

    @Query("SELECT * FROM student_attendance_stats WHERE id = 'singleton'")
    fun getStats(): Flow<StudentAttendanceStatsEntity?>

    @Query("SELECT * FROM student_attendance_stats WHERE id = 'singleton'")
    suspend fun getStatsOnce(): StudentAttendanceStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: StudentAttendanceStatsEntity)

    @Query("DELETE FROM student_attendance_stats")
    suspend fun clearAll()
}