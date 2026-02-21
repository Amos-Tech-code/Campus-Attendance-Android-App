package com.amos_tech_code.smartattend.ui.feature.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.ExportRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.domain.response.AttendanceExportRecordDto
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ExportViewModel(
    private val exportRepository: ExportRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val _event = Channel<ExportEvent>()
    val event = _event.receiveAsFlow()

    private val _exports = MutableStateFlow<List<AttendanceExportRecordDto>>(emptyList())

    private var programmesWithUnits: List<ProgrammeWithUnits> = emptyList()

    init {
        loadInitialData()
        loadExportHistory()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Get active university
                val activeUniversity = universityRepository.getActiveUniversity()
                if (activeUniversity != null) {
                    _uiState.update { state ->
                        state.copy(
                            universityName = activeUniversity.name,
                            universityId = activeUniversity.id,
                            isLoading = false
                        )
                    }

                    // Load programmes for this university
                    loadProgrammes(activeUniversity.id)

                    // Get active academic term for default semester
                    val activeTerm = universityRepository.getActiveAcademicTerm(activeUniversity.id)
                    activeTerm?.let { term ->
                        _uiState.update { it.copy(semester = term.semester) }
                    }
                } else {
                    _event.send(ExportEvent.ShowSnackbar("No active university found"))
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _event.send(ExportEvent.ShowSnackbar("Error loading data: ${e.message}"))
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadProgrammes(universityId: String) {
        viewModelScope.launch {
            try {
                programmesWithUnits = universityRepository.getProgrammesForUniversity(universityId)
                val programmes = programmesWithUnits.map { it.programme }
                _uiState.update { state ->
                    state.copy(
                        programmes = programmes,
                        canExport = state.selectedProgramme != null && state.selectedUnit != null
                    )
                }
            } catch (e: Exception) {
                _event.send(ExportEvent.ShowSnackbar("Error loading programmes: ${e.message}"))
            }
        }
    }

    fun loadUnitsForProgramme(programmeId: String) {
        viewModelScope.launch {
            try {
                val units = universityRepository.getUnitsForProgramme(programmeId, programmesWithUnits)
                _uiState.update { state ->
                    state.copy(
                        units = units,
                        selectedUnit = null // Reset selected unit when programme changes
                    )
                }
                validateCanExport()
            } catch (e: Exception) {
                _event.send(ExportEvent.ShowSnackbar("Error loading units: ${e.message}"))
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun loadExportHistory(page: Int = 0) {
        viewModelScope.launch {
            val result = exportRepository.getExportRecords(page = page, size = 10)
                when (result) {
                    is ApiResult.Success -> {
                        result.data.let { response ->
                            val newExports = if (page == 0) {
                                response.exports
                            } else {
                                _exports.value + response.exports
                            }
                            _exports.value = newExports

                            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            val thisMonthExports = newExports.count { export ->
                                try {
                                    // Parse the ISO date string to Instant then to LocalDateTime
                                    val instant = Instant.parse(export.createdAt)
                                    val createdAt = instant.toLocalDateTime(TimeZone.currentSystemDefault())

                                    createdAt.month == now.month && createdAt.year == now.year
                                } catch (e: Exception) {
                                    false
                                }
                            }

                            _uiState.update { state ->
                                state.copy(
                                    recentExports = newExports.take(10),
                                    totalExports = response.total,
                                    exportsThisMonth = thisMonthExports,
                                    currentPage = page,
                                    hasMorePages = newExports.size < response.total,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = result.error.extractApiErrorMessage()
                        _event.send(ExportEvent.ShowSnackbar("Failed to load exports: $errorMessage"))
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    fun loadMoreExports() {
        if (_uiState.value.hasMorePages && !_uiState.value.isLoading) {
            loadExportHistory(_uiState.value.currentPage + 1)
        }
    }

    fun refreshExports() {
        loadExportHistory(0)
    }

    // ========== UI ACTIONS ==========

    fun showExportSheet() {
        _uiState.update { it.copy(showExportSheet = true) }
    }

    fun hideExportSheet() {
        _uiState.update { it.copy(showExportSheet = false) }
    }

    fun selectProgramme(programme: ProgrammeEntity) {
        _uiState.update { it.copy(selectedProgramme = programme) }
        loadUnitsForProgramme(programme.id)
        _uiState.update { it.copy(yearOfStudy = programme.yearOfStudy) }
        validateCanExport()
    }

    fun clearProgrammeSelection() {
        _uiState.update { state ->
            state.copy(
                selectedProgramme = null,
                selectedUnit = null,
                units = emptyList()
            )
        }
        validateCanExport()
    }

    fun selectUnit(unit: UnitEntity) {
        _uiState.update { it.copy(selectedUnit = unit) }
        validateCanExport()
    }

    fun clearUnitSelection() {
        _uiState.update { it.copy(selectedUnit = null) }
        validateCanExport()
    }

    fun selectWeekRange(range: String) {
        _uiState.update { it.copy(weekRange = range) }
        validateCanExport()
    }

    fun selectSessionType(type: AttendanceSessionType?) {
        _uiState.update { it.copy(sessionType = type) }
    }

    fun selectYearOfStudy(year: Int) {
        _uiState.update { it.copy(yearOfStudy = year) }
        validateCanExport()
    }

    fun selectSemester(semester: Int) {
        _uiState.update { it.copy(semester = semester) }
    }

    fun selectExportFormat(format: ExportFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    private fun validateCanExport() {
        val state = _uiState.value
        val canExport = state.selectedProgramme != null &&
                state.selectedUnit != null &&
                state.weekRange.isNotBlank() &&
                state.yearOfStudy > 0 &&
                state.semester in 1..3
        _uiState.update { it.copy(canExport = canExport) }
    }

    fun quickExport(format: ExportFormat) {
        val state = _uiState.value
        if (state.selectedProgramme != null && state.selectedUnit != null) {
            selectExportFormat(format)
            executeExport()
        } else {
            viewModelScope.launch {
                _event.send(ExportEvent.ShowSnackbar("Please select a programme and unit first"))
                showExportSheet()
            }
        }
    }

    fun executeExport() {
        val state = _uiState.value

        if (!state.canExport) {
            viewModelScope.launch {
                _event.send(ExportEvent.ShowSnackbar("Please complete all required fields"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            val result = exportRepository.exportAttendance(
                universityId = state.universityId,
                programmeId = state.selectedProgramme!!.id,
                unitId = state.selectedUnit!!.id,
                weekRange = state.weekRange,
                sessionType = state.sessionType,
                yearOfStudy = state.yearOfStudy,
                semester = state.semester,
                exportFormat = state.selectedFormat
            )
                when (result) {
                    is ApiResult.Success -> {
                        result.data.let { response ->
                            _uiState.update { it.copy(isExporting = false, showExportSheet = false) }
                            _event.send(ExportEvent.ExportSuccess(response))
                            // Refresh export history
                            loadExportHistory(0)
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = result.error.extractApiErrorMessage()
                        _uiState.update { it.copy(isExporting = false) }
                        _event.send(ExportEvent.ShowSnackbar("Export failed: $errorMessage"))
                    }
                }

        }
    }

    fun getExportDetails(exportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = exportRepository.getExportStatus(exportId)
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        result.data.let { export ->
                            _event.send(ExportEvent.NavigateToExportDetails(export.exportId))
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = result.error.extractApiErrorMessage()
                        _uiState.update { it.copy(isLoading = false) }
                        _event.send(ExportEvent.ShowSnackbar("Failed to get export details: $errorMessage"))
                    }
                }
            }

    }

    fun navigateBack() {
        viewModelScope.launch {
            _event.send(ExportEvent.NavigateBack)
        }
    }

    // For handling new exports (from ExportSuccess)
    @OptIn(ExperimentalTime::class)
    fun downloadNewExport(export: AttendanceExportResponseDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Convert to RecordDto with available data
            val recordDto = AttendanceExportRecordDto(
                exportId = export.exportId,
                fileName = export.fileName,
                fileUrl = export.fileUrl,
                fileSize = export.fileSize,
                exportFormat = export.exportFormat.name,
                weekRange = uiState.value.weekRange,
                createdAt = Clock.System.now().toString(),
                expiresAt = export.expiresAt,
                unitName = uiState.value.selectedUnit?.name,
                unitCode = uiState.value.selectedUnit?.code,
                programmeName = uiState.value.selectedProgramme?.name,
                academicTerm = "Semester ${uiState.value.semester}"
            )

            _event.send(ExportEvent.DownloadExport(recordDto))
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun shareNewExport(export: AttendanceExportResponseDto) {
        viewModelScope.launch {
            val recordDto = AttendanceExportRecordDto(
                exportId = export.exportId,
                fileName = export.fileName,
                fileUrl = export.fileUrl,
                fileSize = export.fileSize,
                exportFormat = export.exportFormat.name,
                weekRange = uiState.value.weekRange,
                createdAt = Clock.System.now().toString(),
                expiresAt = export.expiresAt,
                unitName = uiState.value.selectedUnit?.name,
                unitCode = uiState.value.selectedUnit?.code,
                programmeName = uiState.value.selectedProgramme?.name,
                academicTerm = "Semester ${uiState.value.semester}"
            )

            _event.send(ExportEvent.ShareExport(recordDto))
        }
    }

    fun downloadExport(export: AttendanceExportRecordDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _event.send(ExportEvent.DownloadExport(export))
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun shareExport(export: AttendanceExportRecordDto) {
        viewModelScope.launch {
            _event.send(ExportEvent.ShareExport(export))
        }
    }

}