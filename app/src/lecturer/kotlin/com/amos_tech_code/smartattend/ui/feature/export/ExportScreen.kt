package com.amos_tech_code.smartattend.ui.feature.export

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.domain.response.AttendanceExportRecordDto
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto
import com.amos_tech_code.smartattend.services.FileDownloadManager
import com.amos_tech_code.smartattend.ui.feature.export.components.CsvViewerScreen
import com.amos_tech_code.smartattend.ui.feature.export.components.ExportBottomSheet
import com.amos_tech_code.smartattend.ui.feature.export.components.PdfViewerScreen
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavController,
    viewModel: ExportViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    // Navigation states
    var showExportViewerDialog by rememberSaveable { mutableStateOf(false) }
    var currentExport by remember { mutableStateOf<AttendanceExportResponseDto?>(null) } // Not saveable, complex object
    var showPdfViewer by rememberSaveable { mutableStateOf(false) }
    var pdfUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentFileName by rememberSaveable { mutableStateOf("") }
    var currentFormat by rememberSaveable { mutableStateOf("") }

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
            is ExportEvent.NavigateToExportDetails -> {
                // Navigate to details screen if needed
            }
            is ExportEvent.NavigateBack -> {
                navController.popBackStack()
            }
            is ExportEvent.DownloadExport -> {
                scope.launch {
                    downloadAndHandleFile(
                        context = context,
                        export = event.export,
                        snackbarHostState = snackbarHostState,
                        onFileReady = { uri, fileName, format ->
                            pdfUri = uri
                            currentFileName = fileName
                            currentFormat = format
                            showPdfViewer = true
                        }
                    )
                }
            }
            is ExportEvent.ShareExport -> {
                scope.launch {
                    shareExportFile(context, event.export, snackbarHostState)
                }
            }
            // Handle new events if you added them
            is ExportEvent.DownloadNewExport -> {
                // This is handled by viewModel.downloadNewExport call
            }
            is ExportEvent.ShareNewExport -> {
                // This is handled by viewModel.shareNewExport call
            }
        }
    }

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
                        if (uiState.recentExports.isNotEmpty()) {
                            TextButton(onClick = { viewModel.loadMoreExports() }) {
                                Text("View All")
                            }
                        }
                    }
                }

                // Export History Items
                if (uiState.recentExports.isEmpty()) {
                    item {
                        EmptyStateCard(
                            onNewExport = { viewModel.showExportSheet() }
                        )
                    }
                } else {
                    items(uiState.recentExports) { export ->
                        ExportHistoryItem(
                            export = export,
                            onClick = {  },
                            onShare = { viewModel.shareExport(it) },
                            onDownload = { viewModel.downloadExport(it) }
                        )
                    }
                }
            }

            // Loading Indicator
            if (uiState.isLoading) {
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
                                text = "Generating Export...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
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
                // Use the new function that handles conversion
                viewModel.downloadNewExport(currentExport!!)
            },
            onDownload = {
                showExportViewerDialog = false
                // Just download without viewing
                viewModel.shareNewExport(currentExport!!) // or handle download directly
            }
        )
    }

    // PDF/CSV Viewer
    if (showPdfViewer && pdfUri != null) {
        if (currentFormat.equals("PDF", ignoreCase = true)) {
            PdfViewerScreen(
                uri = pdfUri!!,
                fileName = currentFileName,
                onClose = { showPdfViewer = false },
                onShare = {
                    scope.launch {
                        val mimeType = "application/pdf"
                        FileDownloadManager.shareFile(context, pdfUri!!, mimeType)
                    }
                },
                onDownload = {
                    // This is just re-downloading the same file
                    showPdfViewer = false
                    // You might want to trigger download again
                }
            )
        } else {
            CsvViewerScreen(
                uri = pdfUri!!,
                fileName = currentFileName,
                onClose = { showPdfViewer = false },
                onShare = {
                    scope.launch {
                        val mimeType = "text/csv"
                        FileDownloadManager.shareFile(context, pdfUri!!, mimeType)
                    }
                },
                onDownload = {
                    showPdfViewer = false
                    // Re-download if needed
                }
            )
        }
    }
}

@Composable
fun ExportStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
    export: AttendanceExportRecordDto,
    onClick: () -> Unit,
    onDownload: (AttendanceExportRecordDto) -> Unit,
    onShare: (AttendanceExportRecordDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Format Icon with colored background
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
                color = when (export.exportFormat.uppercase()) {
                    "PDF" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    "CSV" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (export.exportFormat.uppercase()) {
                            "PDF" -> Icons.Default.PictureAsPdf
                            "CSV" -> Icons.Default.TableChart
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = when (export.exportFormat.uppercase()) {
                            "PDF" -> MaterialTheme.colorScheme.error
                            "CSV" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
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
                // Title with unit code and name
                Text(
                    text = buildString {
                        append(export.unitCode ?: "Unit")
                        if (export.unitName != null) {
                            append(" - ")
                            append(export.unitName)
                        }
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Programme and week range
                Text(
                    text = buildString {
                        append(export.programmeName ?: "Programme")
                        append(" • Week ")
                        append(export.weekRange ?: "All")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Metadata row
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

                    // File size
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

                    // Status indicator (if expired)
                    if (export.expiresAt != null) {
                        val isExpired = isExpired(export.expiresAt)
                        if (isExpired) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Expired",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onShare(export) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onDownload(export) },
                    modifier = Modifier.size(36.dp)
                ) {
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
            Text("Export Complete")
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
@OptIn(ExperimentalTime::class)
private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val month = localDateTime.month.name.lowercase()
            .replaceFirstChar { it.uppercase() }
            .take(3)
        val day = localDateTime.day
        val year = localDateTime.year
        val hour = String.format("%02d", localDateTime.hour)
        val minute = String.format("%02d", localDateTime.minute)

        "$month $day, $year $hour:$minute"
    } catch (_: Exception) {
        dateString
    }
}

fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}

@OptIn(ExperimentalTime::class)
private fun isExpired(expiresAt: String): Boolean {
    return try {
        val expiryInstant = Instant.parse(expiresAt)
        val now = Clock.System.now()
        expiryInstant < now
    } catch (_: Exception) {
        false
    }
}

// Helper functions
private suspend fun downloadAndHandleFile(
    context: Context,
    export: AttendanceExportRecordDto,
    snackbarHostState: SnackbarHostState,
    onFileReady: (Uri, String, String) -> Unit
) {
    val result = FileDownloadManager.downloadFile(
        context = context,
        url = export.fileUrl,
        fileName = export.fileName
    )

    result.onSuccess { uri ->
        snackbarHostState.showSnackbar("File downloaded successfully")
        onFileReady(uri, export.fileName, export.exportFormat)
    }.onFailure { error ->
        snackbarHostState.showSnackbar("Download failed: ${error.message}")
    }
}

private suspend fun shareExportFile(
    context: Context,
    export: AttendanceExportRecordDto,
    snackbarHostState: SnackbarHostState
) {
    val result = FileDownloadManager.downloadFile(
        context = context,
        url = export.fileUrl,
        fileName = export.fileName
    )

    result.onSuccess { uri ->
        val mimeType = if (export.exportFormat.equals("PDF", ignoreCase = true))
            "application/pdf" else "text/csv"
        FileDownloadManager.shareFile(context, uri, mimeType)
    }.onFailure { error ->
        snackbarHostState.showSnackbar("Share failed: ${error.message}")
    }
}
