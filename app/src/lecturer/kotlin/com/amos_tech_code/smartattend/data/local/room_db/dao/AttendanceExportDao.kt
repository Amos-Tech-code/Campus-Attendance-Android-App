package com.amos_tech_code.smartattend.data.local.room_db.dao

import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceExportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExport(export: AttendanceExportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExports(exports: List<AttendanceExportEntity>)

    @Update
    suspend fun updateExport(export: AttendanceExportEntity)

    @Delete
    suspend fun deleteExport(export: AttendanceExportEntity)

    @Query("DELETE FROM attendance_exports WHERE exportId = :exportId")
    suspend fun deleteExportByExportId(exportId: String)

    @Query("DELETE FROM attendance_exports WHERE universityId = :universityId")
    suspend fun deleteAllExportsForUniversity(universityId: String)

    @Query("SELECT * FROM attendance_exports WHERE exportId = :exportId")
    suspend fun getExportByExportId(exportId: String): AttendanceExportEntity?

    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentExports(universityId: String, limit: Int): List<AttendanceExportEntity>

    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedExports(universityId: String, limit: Int, offset: Int): List<AttendanceExportEntity>

    @Query("SELECT COUNT(*) FROM attendance_exports WHERE universityId = :universityId")
    suspend fun getTotalExportsCount(universityId: String): Int

    @Query("SELECT COUNT(*) FROM attendance_exports WHERE universityId = :universityId AND createdAt >= :startOfMonth")
    suspend fun getExportsThisMonth(universityId: String, startOfMonth: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_exports WHERE localFilePath IS NOT NULL AND universityId = :universityId")
    suspend fun getDownloadedExportsCount(universityId: String): Int

    @Query("UPDATE attendance_exports SET localFilePath = :filePath, isDownloading = false, downloadProgress = 100 WHERE exportId = :exportId")
    suspend fun markAsDownloaded(exportId: String, filePath: String)

    @Query("UPDATE attendance_exports SET isDownloading = :isDownloading, downloadProgress = :progress WHERE exportId = :exportId")
    suspend fun updateDownloadProgress(exportId: String, isDownloading: Boolean, progress: Int)

    @Query("UPDATE attendance_exports SET isDownloading = 0, downloadProgress = 0 WHERE exportId = :exportId")
    suspend fun markDownloadFailed(exportId: String)

    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId AND (fileName LIKE '%' || :query || '%' OR unitName LIKE '%' || :query || '%' OR unitCode LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    suspend fun searchExports(universityId: String, query: String): List<AttendanceExportEntity>

    @Query("DELETE FROM attendance_exports WHERE expiresAt < :now AND expiresAt IS NOT NULL")
    suspend fun deleteExpiredExports(now: Long)

    // Flow observers
    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC")
    fun observeAllExports(universityId: String): Flow<List<AttendanceExportEntity>>

    @Query("SELECT * FROM attendance_exports WHERE exportId = :exportId")
    fun observeExport(exportId: String): Flow<AttendanceExportEntity?>

    // Paging source
    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC")
    fun getPagingSource(universityId: String): PagingSource<Int, AttendanceExportEntity>
}