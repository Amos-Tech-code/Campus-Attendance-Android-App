package com.amos_tech_code.smartattend.data.local.room_db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration from version 1 to 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `attendance_exports` (
                        `exportId` TEXT PRIMARY KEY NOT NULL,
                        `universityId` TEXT NOT NULL,
                        `fileName` TEXT NOT NULL,
                        `fileUrl` TEXT NOT NULL,
                        `fileSize` INTEGER NOT NULL,
                        `exportFormat` TEXT NOT NULL,
                        `weekRange` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `expiresAt` INTEGER,
                        `unitName` TEXT,
                        `unitCode` TEXT,
                        `programmeName` TEXT,
                        `academicTerm` TEXT,
                        `localFilePath` TEXT,
                        `isDownloading` INTEGER NOT NULL DEFAULT 0,
                        `downloadProgress` INTEGER NOT NULL DEFAULT 0
                    )
                """)

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_exports_universityId` ON `attendance_exports` (`universityId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_exports_createdAt` ON `attendance_exports` (`createdAt` DESC)")
    }
}