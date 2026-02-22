package com.amos_tech_code.smartattend.ui.feature.export.allexports

import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.services.FileDownloadManager
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.feature.export.DownloadProgress
import com.amos_tech_code.smartattend.ui.feature.export.ExportEvent
import com.amos_tech_code.smartattend.ui.feature.export.ExportViewModel
import com.amos_tech_code.smartattend.ui.feature.export.components.CsvViewerScreen
import com.amos_tech_code.smartattend.ui.feature.export.components.DeleteConfirmationDialog
import com.amos_tech_code.smartattend.ui.feature.export.components.ExportDetailsDialog
import com.amos_tech_code.smartattend.ui.feature.export.components.PdfViewerScreen
import com.amos_tech_code.smartattend.ui.feature.export.formatDate
import com.amos_tech_code.smartattend.ui.feature.export.formatFileSize
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllExportsScreen(
    navController: NavController,
    viewModel: ExportViewModel = koinViewModel(),
) {
    val pagedExportsFlow = viewModel.pagedExports
    val downloadingExports by viewModel.downloadingExports.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearching by remember { mutableStateOf(false) }
    // Navigation states
    var showPdfViewer by rememberSaveable { mutableStateOf(false) }
    var showCsvViewer by rememberSaveable { mutableStateOf(false) }
    var pdfUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var csvUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentFileName by rememberSaveable { mutableStateOf("") }
    var showExportDetails by rememberSaveable { mutableStateOf(false) }
    var selectedExport by remember { mutableStateOf<AttendanceExportEntity?>(null) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    var exportToDelete by remember { mutableStateOf<AttendanceExportEntity?>(null) }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is ExportEvent.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            is ExportEvent.OpenPdf -> {
                pdfUri = event.uri
                currentFileName = event.fileName
                showPdfViewer = true
            }

            is ExportEvent.OpenCsv -> {
                csvUri = event.uri
                currentFileName = event.fileName
                showCsvViewer = true
            }

            is ExportEvent.ShareExport -> {
                scope.launch {
                    val mimeType = when (event.export.exportFormat) {
                        ExportFormat.PDF -> "application/pdf"
                        ExportFormat.CSV -> "text/csv"
                    }
                    if (event.export.localFilePath != null) {
                        viewModel.validateAndShareExport(event.export, context, mimeType)
                    } else {
                        snackbarHostState.showSnackbar("Please download the file first")
                    }
                }
            }

            is ExportEvent.ShowExportDetails -> {
                selectedExport = event.export
                showExportDetails = true
            }
            is ExportEvent.ConfirmDelete -> {
                exportToDelete = event.export
                showDeleteConfirmation = true
            }

            is ExportEvent.NavigateBack -> {
                navController.popBackStack()
            }

            else -> { }

        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isSearching) {
                        SmartAttendTextField(
                            value = searchQuery,
                            onValueChange = {
                                viewModel.searchExports(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "Search exports...",
                            label = "Search exports"
                        )
                    } else {
                        Text(
                            text = "All Exports",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Close search" else "Search"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (pagedExportsFlow != null) {
                val lazyPagingItems = pagedExportsFlow.collectAsLazyPagingItems()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = lazyPagingItems.itemCount,
                        key = lazyPagingItems.itemKey { it.exportId },
                        contentType = lazyPagingItems.itemContentType { "export" }
                    ) { index ->
                        val export = lazyPagingItems[index]
                        export?.let {
                            AllExportsHistoryItem(
                                export = it,
                                downloadProgress = downloadingExports[it.exportId],
                                onClick = {
                                    selectedExport = it
                                    showExportDetails = true
                                },
                                onShare = { viewModel.shareExport(export) },
                                onDownload = { viewModel.downloadExport(export, context) },
                                onView = { viewModel.onViewClicked(export, context) },
                                onDelete = {
                                    exportToDelete = it
                                    showDeleteConfirmation = true
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    // Loading state
                    when (val refreshState = lazyPagingItems.loadState.refresh) {
                        is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                ErrorItem(
                                    message = refreshState.error.message ?: "Unknown error",
                                    onRetry = { lazyPagingItems.retry() }
                                )
                            }
                        }
                        else -> {}
                    }

                    // Empty state
                    if (lazyPagingItems.loadState.refresh is LoadState.NotLoading &&
                        lazyPagingItems.itemCount == 0) {
                        item {
                            EmptyExportsState(
                                onNewExport = { viewModel.showExportSheet() }
                            )
                        }
                    }

                    // Footer loading
                    item {
                        if (lazyPagingItems.loadState.append is LoadState.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
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

    // Export Details Dialog
    if (showExportDetails && selectedExport != null) {
        ExportDetailsDialog(
            export = selectedExport!!,
            onDismiss = {
                showExportDetails = false
                selectedExport = null
            },
            onView = {
                viewModel.onViewClicked(selectedExport!!, context)
            },
            onDownload = {
                viewModel.downloadExport(selectedExport!!, context)
            },
            onDelete = {
                viewModel.deleteExport(selectedExport!!)
            },
            onShare = {
                viewModel.shareExport(selectedExport!!)
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation && exportToDelete != null) {
        DeleteConfirmationDialog(
            fileName = exportToDelete!!.fileName,
            onConfirm = {
                viewModel.deleteExport(exportToDelete!!)
                exportToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                exportToDelete = null
            }
        )
    }

}

@Composable
fun AllExportsHistoryItem(
    export: AttendanceExportEntity,
    downloadProgress: DownloadProgress?,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit,
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
                // Format Icon (same as above)
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

                // Content (same as above)
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

                // Actions (with delete button)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
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
                                    progress = { progress }
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

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Download progress bar
            if (isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Error loading exports",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyExportsState(
    onNewExport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                text = "No Exports Found",
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