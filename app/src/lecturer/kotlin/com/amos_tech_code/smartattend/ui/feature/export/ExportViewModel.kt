package com.amos_tech_code.smartattend.ui.feature.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.network.utils.extractApiErrorMessage
import com.amos_tech_code.smartattend.data.repository.ExportRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.services.FileDownloadManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExportViewModel(
    private val exportRepository: ExportRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val _event = Channel<ExportEvent>()
    val event = _event.receiveAsFlow()

    // Create a universityId StateFlow to drive other flows
    private val _universityIdFlow = MutableStateFlow<String?>(null)
    // StateFlow for search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Recent exports flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val recentExports: StateFlow<List<AttendanceExportEntity>> = _universityIdFlow.flatMapLatest { universityId ->
        if (universityId == null) {
            flowOf(emptyList())
        } else {
            // It only depends on the universityId now.
            exportRepository.observeRecentExports(universityId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedExports: StateFlow<PagingData<AttendanceExportEntity>> = combine(
        _universityIdFlow,
        _searchQuery
    ) { universityId, query ->
        universityId to query
    }.flatMapLatest { (universityId, query) ->
        if (universityId == null) {
            flowOf(PagingData.empty())
        } else {
            exportRepository.getPagedExports(universityId, query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    )

    // Combine statistics into the main uiState flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val exportStatistics: StateFlow<ExportRepository.ExportStatistics> = _universityIdFlow.flatMapLatest { universityId ->
        if (universityId != null) {
            exportRepository.getExportStatistics(universityId)
        } else {
            MutableStateFlow(ExportRepository.ExportStatistics(0, 0, 0))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExportRepository.ExportStatistics(0, 0, 0)
    )

    // Track ongoing downloads
    private val _downloadingExports = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadingExports: StateFlow<Map<String, DownloadProgress>> = _downloadingExports.asStateFlow()

    private var programmesWithUnits: List<ProgrammeWithUnits> = emptyList()

    init {
        loadInitialData()
        // Collect statistics and update UI state
        viewModelScope.launch {
            exportStatistics.collect { stats ->
                _uiState.update { currentState ->
                    currentState.copy(
                        totalExports = stats.totalExports,
                        exportsThisMonth = stats.exportsThisMonth,
                        downloadedExports = stats.downloadedExports
                    )
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val activeUniversity = universityRepository.getActiveUniversity()
                if (activeUniversity != null) {
                    // Set the universityId flow first
                    _universityIdFlow.value = activeUniversity.id
                    _uiState.update { state ->
                        state.copy(
                            universityName = activeUniversity.name,
                            universityId = activeUniversity.id,
                            isLoading = false
                        )
                    }

                    loadProgrammes(activeUniversity.id)

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
                        selectedUnit = null
                    )
                }
                validateCanExport()
            } catch (e: Exception) {
                _event.send(ExportEvent.ShowSnackbar("Error loading units: ${e.message}"))
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
                exportFormat = state.selectedFormat,
                programmeName = state.selectedProgramme.name,
                unitName = state.selectedUnit.name,
                unitCode = state.selectedUnit.code
            )

            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isExporting = false, showExportSheet = false)
                    }
                    _event.send(ExportEvent.ExportSuccess(result.data))
                }
                is ApiResult.Failure -> {
                    val errorMessage = result.error.extractApiErrorMessage()
                    _uiState.update { it.copy(isExporting = false) }
                    _event.send(ExportEvent.ShowSnackbar("Export failed: $errorMessage"))
                }
            }
        }
    }

    fun onViewClicked(export: AttendanceExportEntity, context: Context) {
        viewModelScope.launch {
            val uri = FileDownloadManager.validateAndGetUri(context, export.localFilePath ?: "")

            if (uri != null) {
                emitOpenEvent(export, uri)
            } else {
                downloadAndOpen(export, context)
            }
        }
    }

    fun validateAndShareExport(export: AttendanceExportEntity, context: Context, mimeType: String) {
        viewModelScope.launch {
            val uri = FileDownloadManager.validateAndGetUri(context, export.localFilePath!!)
            if (uri != null) {
                FileDownloadManager.shareFile(context, uri, mimeType)
            } else {
                exportRepository.clearLocalFilePath(export.exportId)
                _event.send(ExportEvent.ShowSnackbar("File not found. Please download again."))
            }
        }
    }

    fun downloadExport(export: AttendanceExportEntity, context: Context) {
        viewModelScope.launch {
            _downloadingExports.update {
                it + (export.exportId to DownloadProgress(0f, true))
            }

            val result = exportRepository.downloadExportFile(
                context = context,
                exportId = export.exportId,
                onProgress = { progress ->
                    _downloadingExports.update {
                        it + (export.exportId to DownloadProgress(progress, true))
                    }
                }
            )

            _downloadingExports.update {
                it - export.exportId
            }

            result.fold(
                onSuccess = {
                    _event.send(ExportEvent.ShowSnackbar("Download complete"))
                },
                onFailure = {
                    _event.send(
                        ExportEvent.ShowSnackbar(
                            "Download failed: ${it.message}"
                        )
                    )
                }
            )
        }
    }
    private suspend fun downloadAndOpen(
        export: AttendanceExportEntity,
        context: Context
    ) {
        _downloadingExports.update {
            it + (export.exportId to DownloadProgress(0f, true))
        }

        val result = exportRepository.downloadExportFile(
            context = context,
            exportId = export.exportId,
            onProgress = { progress ->
                _downloadingExports.update {
                    it + (export.exportId to DownloadProgress(progress, true))
                }
            }
        )

        result.onSuccess { (_, uri) ->
            _downloadingExports.update {
                it - export.exportId
            }

            emitOpenEvent(export, uri)
        }.onFailure {
            _downloadingExports.update {
                it - export.exportId
            }
            _event.send(ExportEvent.ShowSnackbar("Download failed"))
        }
    }

    private suspend fun emitOpenEvent(
        export: AttendanceExportEntity,
        uri: Uri
    ) {
        when (export.exportFormat) {
            ExportFormat.PDF ->
                _event.send(ExportEvent.OpenPdf(uri, export.fileName))

            ExportFormat.CSV ->
                _event.send(ExportEvent.OpenCsv(uri, export.fileName))
        }
    }

    fun searchExports(query: String) {
        _searchQuery.value = query
    }

    fun deleteExport(export: AttendanceExportEntity) {
        viewModelScope.launch {
            exportRepository.deleteExport(export.exportId)
            _event.send(ExportEvent.ShowSnackbar("Export deleted"))
        }
    }

    // UI State update functions
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

    fun navigateBack() {
        _event.trySend(ExportEvent.NavigateBack)
    }

    fun onViewAllExportsClick() {
        _event.trySend(ExportEvent.ViewAllExports)
    }

    fun shareExport(export: AttendanceExportEntity) {
        _event.trySend(ExportEvent.ShareExport(export))
    }

    fun onShowExportDetails(export: AttendanceExportEntity) {
        viewModelScope.launch {
            _event.send(ExportEvent.ShowExportDetails(export))
        }
    }

    fun onDeleteClicked(export: AttendanceExportEntity) {
        viewModelScope.launch {
            _event.send(ExportEvent.ConfirmDelete(export))
        }
    }
}

data class DownloadProgress(
    val progress: Float,
    val isDownloading: Boolean
)