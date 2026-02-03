package com.amos_tech_code.smartattend.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amos_tech_code.smartattend.data.local.room.entities.StudentEnrollmentEntity

@Dao
interface EnrollmentDao {

    @Query("SELECT * FROM student_enrollment LIMIT 1")
    suspend fun getActiveEnrollment(): StudentEnrollmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(enrollment: StudentEnrollmentEntity)

    @Query("DELETE FROM student_enrollment WHERE enrollmentId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM student_enrollment")
    suspend fun clear()
}
