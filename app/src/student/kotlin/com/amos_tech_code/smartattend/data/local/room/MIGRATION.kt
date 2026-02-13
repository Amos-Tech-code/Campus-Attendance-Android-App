package com.amos_tech_code.smartattend.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration from 1 to 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `student_attendance_stats` (
                        `id` TEXT NOT NULL,
                        `totalSessions` INTEGER NOT NULL,
                        `attendedSessions` INTEGER NOT NULL,
                        `currentStreak` INTEGER NOT NULL,
                        `lastUpdated` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
        )

        // Insert default stats
        db.execSQL(
            """
                    INSERT INTO student_attendance_stats 
                    (id, totalSessions, attendedSessions, currentStreak, lastUpdated) 
                    VALUES ('singleton', 0, 0, 0, ${System.currentTimeMillis()})
                    """.trimIndent()
        )
    }
}