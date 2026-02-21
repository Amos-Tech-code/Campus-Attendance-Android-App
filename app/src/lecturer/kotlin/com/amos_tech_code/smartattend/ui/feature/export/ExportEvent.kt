package com.amos_tech_code.smartattend.ui.feature.export

import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import com.amos_tech_code.smartattend.domain.response.AttendanceExportRecordDto
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto

/*sealed class ExportEvent {
    data class ShowSnackbar(val message: String) : ExportEvent()
    data class ExportSuccess(val response: AttendanceExportResponseDto) : ExportEvent()
    data class NavigateToExportDetails(val exportId: String) : ExportEvent()
    object NavigateBack : ExportEvent()
    // These should use the same type as your repository returns
    data class DownloadExport(val export: AttendanceExportRecordDto) : ExportEvent()
    data class ShareExport(val export: AttendanceExportRecordDto) : ExportEvent()

    // Add this for new exports from the bottom sheet
    data class DownloadNewExport(val export: AttendanceExportResponseDto) : ExportEvent()
    data class ShareNewExport(val export: AttendanceExportResponseDto) : ExportEvent()

}

 */

sealed class ExportEvent {
    data class ShowSnackbar(val message: String) : ExportEvent()
    data class ExportSuccess(val response: AttendanceExportResponseDto) : ExportEvent()
    data class ShareExport(val export: AttendanceExportEntity) : ExportEvent()
    data class ViewPdf(val export: AttendanceExportEntity) : ExportEvent()
    data class ViewCsv(val export: AttendanceExportEntity) : ExportEvent()
    object NavigateBack : ExportEvent()
}