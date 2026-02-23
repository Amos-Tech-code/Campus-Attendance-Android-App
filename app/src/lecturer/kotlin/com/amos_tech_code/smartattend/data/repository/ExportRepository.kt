package com.amos_tech_code.smartattend.data.repository

import android.content.Context
import android.net.Uri
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
import com.amos_tech_code.smartattend.utils.toEpochMillisOrNull
import com.amos_tech_code.smartattend.utils.toEpochMillisStrict
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class ExportRepository(
    private val apiService: ApiService,
    private val exportDao: AttendanceExportDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val fileDownloadManager: FileDownloadManager
) {

    suspend fun getExportStatus(exportId: String): ApiResult<AttendanceExportRecordDto> {
        return safeApiCall {
            apiService.getExportStatus(exportId)
        }
    }

    // Download file with progress tracking
    suspend fun downloadExportFile(
        context: Context,
        exportId: String,
        onProgress: (Float) -> Unit = {}
    ): Result<Pair<String, Uri>> {
        val export = exportDao.getExportByExportId(exportId)
            ?: return Result.failure(Exception("Export not found"))

        exportDao.updateDownloadProgress(exportId, true, 0)

        return try {
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

            if (downloadResult.isSuccess) {
                val uri = downloadResult.getOrNull()!!

                // IMPORTANT: Store the URI as a string, not the file path
                val uriString = uri.toString()

                exportDao.markAsDownloaded(exportId, uriString)
                Result.success(Pair(uriString, uri))
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

    /**
     * Get export records from server and sync to local database
     */
    suspend fun syncExportRecords(
        pageSize: Int = 20
    ): ApiResult<Unit> = withContext(ioDispatcher) {

        var page = 0
        var hasNext = true

        while (hasNext && isActive) {

            when (val result = safeApiCall {
                apiService.getExportRecords(page = page, size = pageSize)
            }) {

                is ApiResult.Success -> {

                    val response = result.data

                    val entities = response.exports.mapNotNull { dto ->
                        runCatching { convertDtoToEntity(dto) }.getOrNull()
                    }

                    if (entities.isNotEmpty()) {
                        exportDao.insertAllExports(entities)
                    }

                    // Determine if more pages exist
                    val nextOffset = (page + 1) * pageSize
                    hasNext = nextOffset < response.total

                    page++
                }

                is ApiResult.Failure -> {
                    return@withContext ApiResult.Failure(result.error)
                }
            }
        }

        ApiResult.Success(Unit)
    }

    private suspend fun syncExportsToDatabase(
        response: ExportsListResponseDto,
    ) {
        val entities = response.exports.map { dto ->
            convertDtoToEntity(dto)
        }

        if (entities.isNotEmpty()) {
            exportDao.insertAllExports(entities)
        }
    }

    // Export attendance records and save to local database
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

    // Save export records to local database
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
            expiresAt = response.expiresAt?.toEpochMillisOrNull(),
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
    // In ExportRepository.kt
    fun getPagedExports(
        universityId: String,
        query: String = "",
        pageSize: Int = 20
    ): Flow<PagingData<AttendanceExportEntity>> {

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    exportDao.pagingSource(universityId)
                } else {
                    exportDao.pagingSourceWithQuery(universityId, query)
                }
            }
        ).flow
    }

    // Get recent exports
     fun observeRecentExports(
        universityId: String,
        limit: Int = 10
    ): Flow<List<AttendanceExportEntity>> {
        return exportDao.observeRecentExports(universityId, limit)
    }

    // Get statistics
    fun getExportStatistics(universityId: String): Flow<ExportStatistics> {
        val startOfMonth = getStartOfMonthTimestamp()

        val totalExportsFlow = exportDao.observeTotalExportsCount(universityId)
        val exportsThisMonthFlow = exportDao.observeExportsThisMonth(universityId, startOfMonth)
        val downloadedExportsFlow = exportDao.observeDownloadedExportsCount(universityId)
        // Combine all flows into a single flow
        return combine(
            totalExportsFlow,
            exportsThisMonthFlow,
            downloadedExportsFlow
        ) { total, thisMonth, downloaded ->
            ExportStatistics(total, thisMonth, downloaded)
        }
    }

    suspend fun clearLocalFilePath(exportId: String) {
        exportDao.clearLocalFilePath(exportId)
    }

    // Delete export
    suspend fun deleteExport(exportId: String) {
        exportDao.deleteExportByExportId(exportId)
    }

    // Helper functions
    private fun convertDtoToEntity(
        dto: AttendanceExportRecordDto,
    ): AttendanceExportEntity {
        return AttendanceExportEntity(
            exportId = dto.exportId,
            universityId = dto.universityId,
            fileName = dto.fileName,
            fileUrl = dto.fileUrl,
            fileSize = dto.fileSize,
            exportFormat = ExportFormat.valueOf(dto.exportFormat),
            weekRange = dto.weekRange,
            createdAt = dto.createdAt.toEpochMillisStrict(),
            expiresAt = dto.expiresAt.toEpochMillisStrict(),
            unitName = dto.unitName,
            unitCode = dto.unitCode,
            programmeName = dto.programmeName,
            academicTerm = dto.academicTerm,
            localFilePath = null,
            isDownloading = false,
            downloadProgress = 0
        )
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