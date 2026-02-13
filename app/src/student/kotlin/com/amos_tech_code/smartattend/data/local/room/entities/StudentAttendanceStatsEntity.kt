package com.amos_tech_code.smartattend.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_attendance_stats")
data class StudentAttendanceStatsEntity(
    @PrimaryKey
    val id: String = "singleton", // Only one row
    val totalSessions: Int = 0,
    val attendedSessions: Int = 0,
    val currentStreak: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)