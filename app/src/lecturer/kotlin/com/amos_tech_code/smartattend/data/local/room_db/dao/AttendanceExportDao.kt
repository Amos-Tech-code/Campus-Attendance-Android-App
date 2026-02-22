package com.amos_tech_code.smartattend.data.local.room_db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
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

    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedExports(universityId: String, limit: Int, offset: Int): List<AttendanceExportEntity>

    @Query("UPDATE attendance_exports SET localFilePath = :uriString, isDownloading = 0, downloadProgress = 100 WHERE exportId = :exportId")
    suspend fun markAsDownloaded(exportId: String, uriString: String)

    @Query("UPDATE attendance_exports SET isDownloading = :isDownloading, downloadProgress = :progress WHERE exportId = :exportId")
    suspend fun updateDownloadProgress(exportId: String, isDownloading: Boolean, progress: Int)

    @Query("UPDATE attendance_exports SET isDownloading = 0, downloadProgress = 0 WHERE exportId = :exportId")
    suspend fun markDownloadFailed(exportId: String)

    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId AND (fileName LIKE '%' || :query || '%' OR unitName LIKE '%' || :query || '%' OR unitCode LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    suspend fun searchExports(universityId: String, query: String): List<AttendanceExportEntity>

    @Query("DELETE FROM attendance_exports WHERE expiresAt < :now AND expiresAt IS NOT NULL")
    suspend fun deleteExpiredExports(now: Long)

     @Query("UPDATE attendance_exports SET localFilePath = NULL WHERE exportId = :exportId")
     suspend fun clearLocalFilePath(exportId: String)

    // Flow observers
    @Query("SELECT COUNT(*) FROM attendance_exports WHERE universityId = :universityId")
    fun observeTotalExportsCount(universityId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_exports WHERE universityId = :universityId AND createdAt >= :startOfMonth")
    fun observeExportsThisMonth(universityId: String, startOfMonth: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_exports WHERE localFilePath IS NOT NULL AND universityId = :universityId")
    fun observeDownloadedExportsCount(universityId: String): Flow<Int>

    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentExports(universityId: String, limit: Int): Flow<List<AttendanceExportEntity>>

    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC")
    fun observeAllExports(universityId: String): Flow<List<AttendanceExportEntity>>

    @Query("SELECT * FROM attendance_exports WHERE exportId = :exportId")
    fun observeExport(exportId: String): Flow<AttendanceExportEntity?>

    // Paging source
    @Query("SELECT * FROM attendance_exports WHERE universityId = :universityId ORDER BY createdAt DESC")
    fun pagingSource(universityId: String): PagingSource<Int, AttendanceExportEntity>
}