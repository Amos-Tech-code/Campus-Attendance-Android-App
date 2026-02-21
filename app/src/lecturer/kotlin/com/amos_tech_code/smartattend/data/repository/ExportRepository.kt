package com.amos_tech_code.smartattend.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.amos_tech_code.smartattend.data.local.room_db.dao.AttendanceExportDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.domain.request.AttendanceExportRequest
import com.amos_tech_code.smartattend.domain.response.AttendanceExportRecordDto
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto
import com.amos_tech_code.smartattend.domain.response.ExportsListResponseDto
import com.amos_tech_code.smartattend.services.FileDownloadManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ExportRepository(
    private val apiService: ApiService,
    private val exportDao: AttendanceExportDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val fileDownloadManager: FileDownloadManager
) {
    /*suspend fun exportAttendance(
        universityId: String,
        programmeId: String,
        unitId: String,
        weekRange: String,
        sessionType: AttendanceSessionType?,
        yearOfStudy: Int,
        semester: Int,
        exportFormat: ExportFormat
    ): ApiResult<AttendanceExportResponseDto> {
        return safeApiCall {
            apiService.exportAttendanceRecords(
                AttendanceExportRequest(
                    universityId = universityId,
                    programmeId = programmeId,
                    unitId = unitId,
                    weekRange = weekRange,
                    sessionType = sessionType,
                    yearOfStudy = yearOfStudy,
                    semester = semester,
                    exportFormat = exportFormat
                )
            )
        }
    }

     */

    suspend fun getExportStatus(exportId: String): ApiResult<AttendanceExportRecordDto> {
        return safeApiCall {
            apiService.getExportStatus(exportId)
        }
    }

    /**
     * Get export records from server and sync to local database
     */
    suspend fun getExportRecords(
        universityId: String,
        page: Int = 0,
        size: Int = 10,
        sort: String = "desc"
    ): ApiResult<ExportsListResponseDto> {
        val result =  safeApiCall {
            apiService.getExportRecords(page, size)
        }
        if (result is ApiResult.Success) {
            // Sync to local database
            withContext(ioDispatcher) {
                syncExportsToDatabase(result.data, universityId)
            }
        }
        return result
    }

    private suspend fun syncExportsToDatabase(
        response: ExportsListResponseDto,
        universityId: String
    ) {
        val entities = response.exports.map { dto ->
            convertDtoToEntity(dto, universityId)
        }

        if (entities.isNotEmpty()) {
            exportDao.insertAllExports(entities)
        }
    }

    private fun convertDtoToEntity(
        dto: AttendanceExportRecordDto,
        universityId: String
    ): AttendanceExportEntity {
        return AttendanceExportEntity(
            exportId = dto.exportId,
            universityId = universityId,
            fileName = dto.fileName,
            fileUrl = dto.fileUrl,
            fileSize = dto.fileSize,
            exportFormat = ExportFormat.valueOf(dto.exportFormat),
            weekRange = dto.weekRange,
            createdAt = parseDateString(dto.createdAt),
            expiresAt = dto.expiresAt?.let { parseDateString(it) },
            unitName = dto.unitName,
            unitCode = dto.unitCode,
            programmeName = dto.programmeName,
            academicTerm = dto.academicTerm,
            localFilePath = null,
            isDownloading = false,
            downloadProgress = 0
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun parseDateString(dateString: String): Long {
        return try {
            // Try ISO format first
            Instant.parse(dateString).toEpochMilliseconds()
        } catch (e: Exception) {
            // Fallback to SimpleDateFormat
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                format.parse(dateString)?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    // Network call with local caching
    suspend fun exportAttendance(
        universityId: String,
        programmeId: String,
        unitId: String,
        weekRange: String,
        sessionType: AttendanceSessionType?,
        yearOfStudy: Int,
        semester: Int,
        exportFormat: ExportFormat,
        programmeName: String?,
        unitName: String?,
        unitCode: String?
    ): ApiResult<AttendanceExportResponseDto> {
        val result = safeApiCall {
                apiService.exportAttendanceRecords(
                    AttendanceExportRequest(
                        universityId = universityId,
                        programmeId = programmeId,
                        unitId = unitId,
                        weekRange = weekRange,
                        sessionType = sessionType,
                        yearOfStudy = yearOfStudy,
                        semester = semester,
                        exportFormat = exportFormat
                    )
                )
        }
        if (result is ApiResult.Success) {
            // Save to local database
            withContext(ioDispatcher) {
                saveExportToDatabase(
                    response = result.data,
                    universityId = universityId,
                    weekRange = weekRange,
                    yearOfStudy = yearOfStudy,
                    semester = semester,
                    programmeName = programmeName,
                    unitName = unitName,
                    unitCode = unitCode
                )
            }
        }

        return result
    }

    private suspend fun saveExportToDatabase(
        response: AttendanceExportResponseDto,
        universityId: String,
        weekRange: String?,
        yearOfStudy: Int,
        semester: Int,
        programmeName: String?,
        unitName: String?,
        unitCode: String?
    ) {
        val now = System.currentTimeMillis()

        val entity = AttendanceExportEntity(
            exportId = response.exportId,
            universityId = universityId,
            fileName = response.fileName,
            fileUrl = response.fileUrl,
            fileSize = response.fileSize,
            exportFormat = response.exportFormat,
            weekRange = weekRange,
            createdAt = now,
            expiresAt = response.expiresAt?.let { parseExpiryToMillis(it) },
            unitName = unitName,
            unitCode = unitCode,
            programmeName = programmeName,
            academicTerm = "Semester $semester, Year $yearOfStudy",
            localFilePath = null,
            isDownloading = false,
            downloadProgress = 0
        )

        exportDao.insertExport(entity)
    }

    // Get paginated exports
    fun getPagedExports(
        universityId: String,
        pageSize: Int = 20
    ): Flow<PagingData<AttendanceExportEntity>> {

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { exportDao.getPagingSource(universityId) }
        ).flow

    }

    // Get recent exports
    suspend fun getRecentExports(
        universityId: String,
        limit: Int = 10
    ): List<AttendanceExportEntity> {
        return exportDao.getRecentExports(universityId, limit)
    }

    // Observe all exports
    fun observeAllExports(universityId: String): Flow<List<AttendanceExportEntity>> {
        return exportDao.observeAllExports(universityId)
    }

    // Observe single export
    fun observeExport(exportId: String): Flow<AttendanceExportEntity?> {
        return exportDao.observeExport(exportId)
    }


    // Download file with progress tracking
    suspend fun downloadExportFile(
        context: Context,
        exportId: String,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        val export = exportDao.getExportByExportId(exportId)
            ?: return Result.failure(Exception("Export not found"))

        // Update to downloading state
        exportDao.updateDownloadProgress(exportId, true, 0)

        return try {
            // FIXED: Handle the Result<Uri> from FileDownloadManager
            val downloadResult = fileDownloadManager.downloadFile(
                context = context,
                url = export.fileUrl,
                fileName = export.fileName,
                onProgress = { progress ->
                    val percent = (progress * 100).toInt()
                    exportDao.updateDownloadProgress(exportId, true, percent)
                    onProgress(progress)
                }
            )

            // FIXED: Properly handle the Result<Uri>
            if (downloadResult.isSuccess) {
                val uri = downloadResult.getOrNull()
                val filePath = uri?.path ?: return Result.failure(Exception("Invalid URI"))

                // Mark as downloaded
                exportDao.markAsDownloaded(exportId, filePath)
                Result.success(filePath)
            } else {
                val error = downloadResult.exceptionOrNull()
                exportDao.markDownloadFailed(exportId)
                Result.failure(error ?: Exception("Download failed"))
            }
        } catch (e: Exception) {
            exportDao.markDownloadFailed(exportId)
            Result.failure(e)
        }
    }

    // Get statistics
    suspend fun getExportStatistics(universityId: String): ExportStatistics {
        val startOfMonth = getStartOfMonthTimestamp()

        return ExportStatistics(
            totalExports = exportDao.getTotalExportsCount(universityId),
            exportsThisMonth = exportDao.getExportsThisMonth(universityId, startOfMonth),
            downloadedExports = exportDao.getDownloadedExportsCount(universityId)
        )
    }

    // Search exports
    suspend fun searchExports(
        universityId: String,
        query: String
    ): List<AttendanceExportEntity> {
        return exportDao.searchExports(universityId, query)
    }

    // Clean up expired exports
    suspend fun cleanupExpiredExports() {
        exportDao.deleteExpiredExports(System.currentTimeMillis())
    }

    // Delete export
    suspend fun deleteExport(exportId: String) {
        exportDao.deleteExportByExportId(exportId)
    }

    // Helper function to parse expiry string to millis
    @OptIn(ExperimentalTime::class)
    private fun parseExpiryToMillis(expiryString: String): Long? {
        return try {
            // Parse ISO date string to milliseconds
            // Implement based on your date format
            Instant.parse(expiryString).toEpochMilliseconds()
        } catch (e: Exception) {
            null
        }
    }

    private fun getStartOfMonthTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    data class ExportStatistics(
        val totalExports: Int,
        val exportsThisMonth: Int,
        val downloadedExports: Int
    )

}