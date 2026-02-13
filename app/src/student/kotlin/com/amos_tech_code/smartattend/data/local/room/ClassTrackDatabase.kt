package com.amos_tech_code.smartattend.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amos_tech_code.smartattend.data.local.room.dao.AttendanceDao
import com.amos_tech_code.smartattend.data.local.room.dao.EnrollmentDao
import com.amos_tech_code.smartattend.data.local.room.dao.StudentAttendanceStatsDao
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceStatsEntity
import com.amos_tech_code.smartattend.data.local.room.entities.StudentEnrollmentEntity

@Database(
    entities = [
        StudentEnrollmentEntity::class,
        StudentAttendanceRecordEntity::class,
        StudentAttendanceStatsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ClassTrackDatabase : RoomDatabase() {

    abstract fun enrollmentDao(): EnrollmentDao

    abstract fun attendanceDao(): AttendanceDao

    abstract fun studentAttendanceStatsDao(): StudentAttendanceStatsDao
}