package com.amos_tech_code.smartattend.data.local.room_db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.amos_tech_code.smartattend.domain.models.ExportFormat

@Entity(
    tableName = "attendance_exports",
    indices = [Index("universityId")]
)
data class AttendanceExportEntity(

    @PrimaryKey
    val exportId: String,

    val universityId: String,

    val fileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val exportFormat: ExportFormat,

    val weekRange: String?,
    val createdAt: Long,
    val expiresAt: Long?,

    val unitName: String?,
    val unitCode: String?,
    val programmeName: String?,
    val academicTerm: String?,

    // Local storage
    val localFilePath: String? = null,

    // Download state
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0
)