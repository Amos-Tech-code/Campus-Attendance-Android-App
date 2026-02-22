package com.amos_tech_code.smartattend.ui.feature.export

import android.net.Uri
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto

sealed class ExportEvent {
    data class ShowSnackbar(val message: String) : ExportEvent()
    data class ExportSuccess(val response: AttendanceExportResponseDto) : ExportEvent()
    data class ShareExport(val export: AttendanceExportEntity) : ExportEvent()
    data class OpenPdf(val uri: Uri, val fileName: String) : ExportEvent()
    data class OpenCsv(val uri: Uri, val fileName: String) : ExportEvent()
    data class ShowExportDetails(val export: AttendanceExportEntity) : ExportEvent()
    data class ConfirmDelete(val export: AttendanceExportEntity) : ExportEvent()
    object NavigateBack : ExportEvent()
    object ViewAllExports : ExportEvent()

}