package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.domain.request.AttendanceExportRequest
import com.amos_tech_code.smartattend.domain.response.AttendanceExportRecordDto
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto
import com.amos_tech_code.smartattend.domain.response.ExportsListResponseDto

class ExportRepository(
    private val apiService: ApiService
) {
    suspend fun exportAttendance(
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

    suspend fun getExportStatus(exportId: String): ApiResult<AttendanceExportRecordDto> {
        return safeApiCall {
            apiService.getExportStatus(exportId)
        }
    }

    suspend fun getExportRecords(
        page: Int = 0,
        size: Int = 10,
        sort: String = "desc"
    ): ApiResult<ExportsListResponseDto> {
        return safeApiCall {
            apiService.getExportRecords(page, size)
        }
    }

}