package com.amos_tech_code.smartattend.data.local.room.dao

import androidx.room.*
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StudentAttendanceRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<StudentAttendanceRecordEntity>)

    @Query("DELETE FROM student_attendance_records")
    suspend fun clearAll()
}
