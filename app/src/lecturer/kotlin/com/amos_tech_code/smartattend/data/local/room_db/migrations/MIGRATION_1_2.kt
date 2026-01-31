package com.amos_tech_code.smartattend.data.local.room_db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS attendance_session_history (
                sessionId TEXT NOT NULL,
                title TEXT,
                unitCode TEXT NOT NULL,
                unitName TEXT NOT NULL,
                sessionType TEXT NOT NULL,
                attendanceMethod TEXT NOT NULL,
                status TEXT NOT NULL,
                startedAt INTEGER NOT NULL,
                endedAt INTEGER,
                PRIMARY KEY(sessionId)
            )
            """.trimIndent()
        )
    }
}
