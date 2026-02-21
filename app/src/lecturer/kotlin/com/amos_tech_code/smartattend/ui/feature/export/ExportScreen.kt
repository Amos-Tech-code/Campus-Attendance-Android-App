package com.amos_tech_code.smartattend.ui.feature.export

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto
import com.amos_tech_code.smartattend.services.FileDownloadManager
import com.amos_tech_code.smartattend.ui.feature.export.allexports.AllExportsScreen
import com.amos_tech_code.smartattend.ui.feature.export.components.CsvViewerScreen
import com.amos_tech_code.smartattend.ui.feature.export.components.ExportBottomSheet
import com.amos_tech_code.smartattend.ui.feature.export.components.PdfViewerScreen
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavController,
    viewModel: ExportViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentExports by viewModel.recentExports.collectAsStateWithLifecycle()
    val downloadingExports by viewModel.downloadingExports.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    // Navigation states
    var showExportViewerDialog by rememberSaveable { mutableStateOf(false) }
    var currentExport by remember { mutableStateOf<AttendanceExportResponseDto?>(null) } // Not saveable, complex object
    var showPdfViewer by rememberSaveable { mutableStateOf(false) }
    var showCsvViewer by rememberSaveable { mutableStateOf(false) }
    var pdfUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var csvUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentFileName by remember { mutableStateOf("") }
    var selectedExportForView by remember { mutableStateOf<AttendanceExportEntity?>(null) }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is ExportEvent.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            is ExportEvent.ExportSuccess -> {
                currentExport = event.response
                showExportViewerDialog = true
            }

            is ExportEvent.ViewPdf -> {
                selectedExportForView = event.export
                if (event.export.localFilePath != null) {
                    // Use local file
                    val uri = FileDownloadManager.getFileUri(context, event.export.localFilePath)
                    pdfUri = uri
                    currentFileName = event.export.fileName
                    showPdfViewer = true
                } else {
                    // Download first then view
                    viewModel.downloadExport(event.export, context)
                }
            }

            is ExportEvent.ViewCsv -> {
                selectedExportForView = event.export
                if (event.export.localFilePath != null) {
                    val uri = FileDownloadManager.getFileUri(context, event.export.localFilePath)
                    csvUri = uri
                    currentFileName = event.export.fileName
                    showCsvViewer = true
                } else {
                    viewModel.downloadExport(event.export, context)
                }
            }

            is ExportEvent.ShareExport -> {
                scope.launch {
                    val mimeType = when (event.export.exportFormat) {
                        ExportFormat.PDF -> "application/pdf"
                        ExportFormat.CSV -> "text/csv"
                    }
                    if (event.export.localFilePath != null) {
                        val uri =
                            FileDownloadManager.getFileUri(context, event.export.localFilePath)
                        FileDownloadManager.shareFile(context, uri, mimeType)
                    } else {
                        snackbarHostState.showSnackbar("Please download the file first")
                    }
                }
            }

            is ExportEvent.NavigateBack -> {
                navController.popBackStack()
            }

        }
    }

    // Auto-view after download completes
    LaunchedEffect(downloadingExports) {
        if (selectedExportForView != null) {
            val downloadState = downloadingExports[selectedExportForView!!.exportId]
            if (downloadState?.isDownloading == false && downloadState.progress >= 1f) {
                // Download completed, get updated export
                val updatedExport = recentExports.find { it.exportId == selectedExportForView!!.exportId }
                if (updatedExport?.localFilePath != null) {
                    val uri = FileDownloadManager.getFileUri(context, updatedExport.localFilePath)
                    when (updatedExport.exportFormat) {
                        ExportFormat.PDF -> {
                            pdfUri = uri
                            currentFileName = updatedExport.fileName
                            showPdfViewer = true
                        }
                        ExportFormat.CSV -> {
                            csvUri = uri
                            currentFileName = updatedExport.fileName
                            showCsvViewer = true
                        }
                    }
                }
                selectedExportForView = null
            }
        }
    }

    if (!uiState.showAllExports) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Export Center",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.universityName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = viewModel::navigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Stats Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExportStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Total Exports",
                                value = uiState.totalExports.toString(),
                                icon = Icons.Outlined.Description,
                                color = MaterialTheme.colorScheme.primary
                            )
                            ExportStatCard(
                                modifier = Modifier.weight(1f),
                                title = "This Month",
                                value = uiState.exportsThisMonth.toString(),
                                icon = Icons.Outlined.CalendarToday,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            ExportStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Downloaded",
                                value = uiState.downloadedExports.toString(),
                                icon = Icons.Outlined.DownloadDone,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // Selected Context Card
                    if (uiState.selectedProgramme != null || uiState.selectedUnit != null) {
                        item {
                            SelectedContextCard(
                                programme = uiState.selectedProgramme,
                                unit = uiState.selectedUnit,
                                onClearProgramme = { viewModel.clearProgrammeSelection() },
                                onClearUnit = { viewModel.clearUnitSelection() }
                            )
                        }
                    }

                    // Quick Actions
                    item {
                        QuickExportActions(
                            onQuickExport = { format ->
                                viewModel.quickExport(format)
                            }
                        )
                    }

                    // Export History Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Exports",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (uiState.totalExports > 0) {
                                TextButton(onClick = { viewModel.showAllExports() }) {
                                    Text("View All (${uiState.totalExports})")
                                }
                            }
                        }
                    }

                    // Export History Items
                    if (recentExports.isEmpty()) {
                        item {
                            EmptyStateCard(
                                onNewExport = { viewModel.showExportSheet() }
                            )
                        }
                    } else {
                        items(recentExports) { export ->
                            ExportHistoryItem(
                                export = export,
                                downloadProgress = downloadingExports[export.exportId],
                                onClick = {
                                },
                                onShare = { viewModel.shareExport(export) },
                                onDownload = { viewModel.downloadExport(export, context) },
                                onView = { viewModel.viewExport(export) }
                            )
                        }
                    }
                }

                // Loading Indicator
                if (uiState.isLoading || uiState.isExporting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.isExporting) "Generating Export..." else "Loading...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        AllExportsScreen(
            viewModel = viewModel,
            onNavigateBack = { viewModel.hideAllExports() },
            snackbarHostState = snackbarHostState,
            onExportClick = {
            },
            onShareClick = { viewModel.shareExport(it) },
            onDownloadClick = { viewModel.downloadExport(it, context)},
            onViewPdf = {
                viewModel.hideAllExports()
                viewModel.viewExport(it)
            },
            onViewCsv = {
                viewModel.hideAllExports()
                viewModel.viewExport(it)
            }
        )
    }

    // Export Bottom Sheet
    if (uiState.showExportSheet) {
        ExportBottomSheet(
            state = uiState,
            onDismiss = { viewModel.hideExportSheet() },
            onProgrammeSelected = { programme -> viewModel.selectProgramme(programme) },
            onUnitSelected = { unit -> viewModel.selectUnit(unit) },
            onWeekRangeSelected = { range -> viewModel.selectWeekRange(range) },
            onSessionTypeSelected = { type -> viewModel.selectSessionType(type) },
            onYearOfStudySelected = { year -> viewModel.selectYearOfStudy(year) },
            onSemesterSelected = { semester -> viewModel.selectSemester(semester) },
            onFormatSelected = { format -> viewModel.selectExportFormat(format) },
            onExport = { viewModel.executeExport() }
        )
    }

    // Dialog for viewer options
    if (showExportViewerDialog && currentExport != null) {
        ExportViewerDialog(
            export = currentExport!!,
            onDismiss = { showExportViewerDialog = false },
            onView = {
                showExportViewerDialog = false
                when (currentExport!!.exportFormat) {
                    ExportFormat.PDF -> {
                        // Convert to entity and view
                        val exportEntity =
                            recentExports.find { it.exportId == currentExport!!.exportId }
                        if (exportEntity != null) {
                            viewModel.viewExport(exportEntity)
                        }
                    }

                    ExportFormat.CSV -> {
                        val exportEntity =
                            recentExports.find { it.exportId == currentExport!!.exportId }
                        if (exportEntity != null) {
                            viewModel.viewExport(exportEntity)
                        }
                    }
                }
            },
            onDownload = {
                showExportViewerDialog = false
                val exportEntity = recentExports.find { it.exportId == currentExport!!.exportId }
                if (exportEntity != null) {
                    viewModel.downloadExport(exportEntity, context)
                }
            }
        )
    }

    // PDF Viewer
    if (showPdfViewer && pdfUri != null) {
        PdfViewerScreen(
            uri = pdfUri!!,
            fileName = currentFileName,
            onClose = {
                showPdfViewer = false
                pdfUri = null
            },
            onShare = {
                scope.launch {
                    FileDownloadManager.shareFile(context, pdfUri!!, "application/pdf")
                }
            },
            onDownload = {
                // Re-download if needed
                showPdfViewer = false
                pdfUri = null
            }
        )
    }

    // CSV Viewer
    if (showCsvViewer && csvUri != null) {
        CsvViewerScreen(
            uri = csvUri!!,
            fileName = currentFileName,
            onClose = {
                showCsvViewer = false
                csvUri = null
            },
            onShare = {
                scope.launch {
                    FileDownloadManager.shareFile(context, csvUri!!, "text/csv")
                }
            },
            onDownload = {
                showCsvViewer = false
                csvUri = null
            }
        )
    }
}



@Composable
fun ExportStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SelectedContextCard(
    programme: ProgrammeEntity?,
    unit: UnitEntity?,
    onClearProgramme: () -> Unit,
    onClearUnit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Selected Context",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (programme != null) {
                SelectedChip(
                    label = "Programme: ${programme.name}",
                    onClear = onClearProgramme
                )
            }
            if (unit != null) {
                SelectedChip(
                    label = "Unit: ${unit.code} - ${unit.name}",
                    onClear = onClearUnit
                )
            }
        }
    }
}

@Composable
fun SelectedChip(
    label: String,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun QuickExportActions(
    onQuickExport: (ExportFormat) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Export",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickExportButton(
                    modifier = Modifier.weight(1f),
                    format = ExportFormat.PDF,
                    icon = Icons.Outlined.PictureAsPdf,
                    onClick = { onQuickExport(ExportFormat.PDF) }
                )
                QuickExportButton(
                    modifier = Modifier.weight(1f),
                    format = ExportFormat.CSV,
                    icon = Icons.Outlined.TableChart,
                    onClick = { onQuickExport(ExportFormat.CSV) }
                )
            }
        }
    }
}

@Composable
fun QuickExportButton(
    modifier: Modifier = Modifier,
    format: ExportFormat,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = SolidColor(
                if (format == ExportFormat.PDF)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (format == ExportFormat.PDF)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = format.name,
            color = if (format == ExportFormat.PDF)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ExportHistoryItem(
    export: AttendanceExportEntity,
    downloadProgress: DownloadProgress?,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloading = downloadProgress?.isDownloading == true
    val progress = downloadProgress?.progress ?: 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Format Icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.small,
                    color = when (export.exportFormat) {
                        ExportFormat.PDF -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ExportFormat.CSV -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (export.exportFormat) {
                                ExportFormat.PDF -> Icons.Default.PictureAsPdf
                                ExportFormat.CSV -> Icons.Default.TableChart
                            },
                            contentDescription = null,
                            tint = when (export.exportFormat) {
                                ExportFormat.PDF -> MaterialTheme.colorScheme.error
                                ExportFormat.CSV -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = export.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )

                    Text(
                        text = buildString {
                            append(export.programmeName ?: "Programme")
                            append(" • ")
                            append(export.unitCode ?: "Unit")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Date
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = formatDate(export.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        // Size
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = formatFileSize(export.fileSize),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        // Downloaded indicator
                        if (export.localFilePath != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (export.localFilePath != null) {
                        IconButton(
                            onClick = onView,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (export.localFilePath == null) {
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier.size(36.dp),
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    progress = progress
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Download progress bar
            if (isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    onNewExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Exports Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Generate your first attendance export to get started",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNewExport,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Export")
            }
        }
    }
}

@Composable
fun ExportViewerDialog(
    export: AttendanceExportResponseDto,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onDownload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Attendance Record Generated")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = export.message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = export.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatFileSize(export.fileSize),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Text(
                    text = "What would you like to do?",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
                Button(
                    onClick = onView,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download")
                }
            }
        }
    )
}


// Helper functions
fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}

fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault())
    return format.format(date)
}
