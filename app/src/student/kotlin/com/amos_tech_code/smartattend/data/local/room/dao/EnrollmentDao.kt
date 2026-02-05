package com.amos_tech_code.smartattend.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.amos_tech_code.smartattend.data.local.room.entities.StudentEnrollmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnrollmentDao {

    @Query("SELECT * FROM student_enrollment WHERE isActive = 1 LIMIT 1")
    fun getActiveEnrollment(): Flow<StudentEnrollmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(enrollment: StudentEnrollmentEntity)

    @Query("UPDATE student_enrollment SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateCurrent()

    @Transaction
    suspend fun upsertEnsuringSingleActive(enrollment: StudentEnrollmentEntity) {
        if (enrollment.isActive) {
            deactivateCurrent()
        }
        upsert(enrollment)
    }

    @Query("UPDATE student_enrollment SET yearOfStudy = :newYearOfStudy WHERE enrollmentId = :enrollmentId")
    suspend fun updateYear(enrollmentId: String, newYearOfStudy: Int)

    @Query("DELETE FROM student_enrollment WHERE enrollmentId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM student_enrollment")
    suspend fun clear()
}
