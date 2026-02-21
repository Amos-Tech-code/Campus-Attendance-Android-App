package com.amos_tech_code.smartattend.ui.feature.export

import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.domain.response.AttendanceExportRecordDto
/*
data class ExportUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false,
    val universityName: String = "",
    val universityId: String = "",
    val programmes: List<ProgrammeEntity> = emptyList(),
    val units: List<UnitEntity> = emptyList(),
    val selectedProgramme: ProgrammeEntity? = null,
    val selectedUnit: UnitEntity? = null,
    val weekRange: String = "ALL",
    val sessionType: AttendanceSessionType? = null,
    val yearOfStudy: Int = 1,
    val semester: Int = 1,
    val selectedFormat: ExportFormat = ExportFormat.PDF,
    val recentExports: List<AttendanceExportRecordDto> = emptyList(),
    val totalExports: Int = 0,
    val exportsThisMonth: Int = 0,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val canExport: Boolean = false
)

 */
data class ExportUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false,
    val universityId: String = "",
    val universityName: String = "",
    val programmes: List<ProgrammeEntity> = emptyList(),
    val units: List<UnitEntity> = emptyList(),
    val selectedProgramme: ProgrammeEntity? = null,
    val selectedUnit: UnitEntity? = null,
    val weekRange: String = "ALL",
    val sessionType: AttendanceSessionType? = null,
    val yearOfStudy: Int = 1,
    val semester: Int = 1,
    val selectedFormat: ExportFormat = ExportFormat.PDF,
    val canExport: Boolean = false,
    val totalExports: Int = 0,
    val exportsThisMonth: Int = 0,
    val downloadedExports: Int = 0,
    val showAllExports : Boolean = false
)