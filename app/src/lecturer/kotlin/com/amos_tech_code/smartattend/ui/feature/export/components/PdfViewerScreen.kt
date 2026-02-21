package com.amos_tech_code.smartattend.ui.feature.export.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    uri: Uri,
    fileName: String,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var pdfPages by remember { mutableStateOf<List<PdfPage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    // Track which page is currently zoomed
    var zoomedPageIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(uri) {
        isLoading = true
        try {
            pdfPages = loadPdfPages(context, uri)
            if (pdfPages.isNotEmpty()) {
                currentPage = 1
            }
        } catch (e: Exception) {
            error = "Failed to load PDF: ${e.message}"
            scope.launch {
                snackbarHostState.showSnackbar("Error loading PDF: ${e.message}")
            }
        } finally {
            isLoading = false
        }
    }

    // Scroll to page when currentPage changes
    LaunchedEffect(currentPage) {
        if (currentPage > 0 && currentPage <= pdfPages.size) {
            listState.animateScrollToItem(index = currentPage - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (pdfPages.isNotEmpty()) {
                            Text(
                                text = "Page $currentPage of ${pdfPages.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Again"
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
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading PDF...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = onClose) {
                                Text("Close")
                            }
                        }
                    }
                }

                pdfPages.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pages found in PDF",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(
                            count = pdfPages.size,
                            key = { index -> pdfPages[index].pageNumber }
                        ) { index ->
                            val page = pdfPages[index]
                            PdfPageItem(
                                page = page,
                                pageNumber = index + 1,
                                totalPages = pdfPages.size,
                                isZoomed = zoomedPageIndex == index,
                                onZoomStateChange = { isZoomed ->
                                    zoomedPageIndex = if (isZoomed) index else -1
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        1.dp,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    // Only show navigation when not zoomed
                    if (zoomedPageIndex == -1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    if (currentPage > 1) {
                                        currentPage--
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Page"
                                )
                            }

                            Surface(
                                modifier = Modifier.size(width = 80.dp, height = 48.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$currentPage/${pdfPages.size}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }

                            FloatingActionButton(
                                onClick = {
                                    if (currentPage < pdfPages.size) {
                                        currentPage++
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Page"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfPageItem(
    page: PdfPage,
    pageNumber: Int,
    totalPages: Int,
    isZoomed: Boolean,
    onZoomStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) } // <-- Store the size
    val maxScale = 5f
    val minScale = 1f

    val zoomState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        scale = max(minScale, min(scale * zoomChange, maxScale))

        if (scale > 1f) {
            val maxX = (size.width * (scale - 1)) / 2
            val maxY = (size.height * (scale - 1)) / 2

            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
            )
        } else {
            offset = Offset.Zero
        }
    }

    // Double-tap to zoom
    val doubleTapHandler = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { tapOffset ->
                if (scale > 1f) {
                    // Reset zoom
                    scale = 1f
                    offset = Offset.Zero
                    onZoomStateChange(false)
                } else {
                    // Zoom to 2x at tap position
                    scale = 2f

                    // Calculate offset to center around tap position
                    val targetOffset = Offset(
                        x = (tapOffset.x * (scale - 1)).coerceIn(0f, size.width * (scale - 1)),
                        y = (tapOffset.y * (scale - 1)).coerceIn(0f, size.height * (scale - 1))
                    )
                    offset = targetOffset
                    onZoomStateChange(true)
                }
            }
        )
    }


        Column(
            modifier = modifier
        ) {
            // Page header - only show when not zoomed
            if (!isZoomed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Page $pageNumber of $totalPages",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = String.format("%.1f KB", page.sizeInKb),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Page content with zoom and pan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(page.bitmap.width.toFloat() / page.bitmap.height)
                    .clipToBounds()
                    .onSizeChanged { size = it } // <-- Get the size here
                    .then(doubleTapHandler)
            ) {
                Image(
                    bitmap = page.bitmap.asImageBitmap(),
                    contentDescription = "PDF Page $pageNumber",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(zoomState)
                )
            }

            // Show zoom indicator when zoomed
            if (isZoomed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "Zoom: ${(scale * 100).toInt()}% | Double-tap to reset",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

}

data class PdfPage(
    val pageNumber: Int,
    val bitmap: Bitmap,
    val sizeInKb: Float
)

private suspend fun loadPdfPages(
    context: Context,
    uri: Uri
): List<PdfPage> = withContext(Dispatchers.IO) {
    val pages = mutableListOf<PdfPage>()

    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
        PdfRenderer(pfd).use { pdfRenderer ->
            for (i in 0 until pdfRenderer.pageCount) {
                pdfRenderer.openPage(i).use { page ->
                    // Calculate appropriate scale for screen
                    val scale = minOf(
                        1f,
                        1000f / page.width.toFloat(),
                        1500f / page.height.toFloat()
                    )

                    val width = (page.width * scale).toInt()
                    val height = (page.height * scale).toInt()

                    val bitmap = Bitmap.createBitmap(
                        width,
                        height,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // Calculate approximate size
                    val sizeInKb = bitmap.byteCount / 1024f

                    pages.add(
                        PdfPage(
                            pageNumber = i + 1,
                            bitmap = bitmap,
                            sizeInKb = sizeInKb
                        )
                    )
                }
            }
        }
    } ?: throw Exception("Could not open PDF file")

    pages
}