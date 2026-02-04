package com.amos_tech_code.smartattend.ui.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.amos_tech_code.smartattend.data.local.room.entities.StudentAttendanceRecordEntity
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionStatus
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.ui.components.ErrorState
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import com.amos_tech_code.smartattend.utils.formatDate
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is AttendanceHistoryEvent.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
            is AttendanceHistoryEvent.RefreshComplete -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Refresh completed successfully")
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            HistoryTopAppBar(
                isRefreshing = isRefreshing,
                scrollBehavior = scrollBehavior,
                onRefreshClick = {
                    viewModel.refresh()
                }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        viewModel.updateFilter(HistoryFilterState())
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    ),
                ) {
                    Text("Clear All Filters")
                }
                FilterFAB(filterState, viewModel)
            }
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                isVisible = scrollBehavior.state.collapsedFraction < 0.5f
            )
        }
    ) { paddingValues ->

        val pagingItems = viewModel.getAttendancePagingData(filterState)
            .collectAsLazyPagingItems()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Session Stats Summary (updated for session status)
            item {
                SessionStatsSummary(viewModel)
            }

            // Filter Chips (when filters are active)
            stickyHeader {
                ActiveFiltersChipRow(filterState)
            }

            // Attendance List
            items(
                count = pagingItems.itemCount,
                key = { index ->
                    pagingItems[index]?.sessionId ?: index
                }
            ) { index ->
                val item = pagingItems[index]
                if (item != null) {
                    AttendanceCard(record = item)
                }
            }

            if (pagingItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (pagingItems.loadState.refresh is LoadState.Error) {
                item {
                    ErrorState(
                        message = "Failed to load attendance records",
                        onRetry = { pagingItems.retry() }
                    )
                }
            }

            if (pagingItems.itemCount == 0) {
                item {
                    EmptyState()
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopAppBar(
    isRefreshing: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onRefreshClick: () -> Unit
) {
    // Animate the rotation only when isRefreshing is true
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Attendance History",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        actions = {
            IconButton(
                onClick = onRefreshClick,
                enabled = !isRefreshing // Disable button while refreshing
            ) {
                if (!isRefreshing) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier,
                    tint = MaterialTheme.colorScheme.primary
                ) } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun SessionStatsSummary(viewModel: HistoryViewModel) {
    val stats by viewModel.statsState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                count = stats.totalSessions.toString(),
                label = "Total Sessions",
                icon = Icons.Default.History,
                color = MaterialTheme.colorScheme.primary
            )

            StatItem(
                count = stats.attendedSessions.toString(),
                label = "Attended",
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary
            )

            StatItem(
                count = stats.scheduledSessions.toString(),
                label = "Scheduled",
                icon = Icons.Default.Schedule,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun ActiveFiltersChipRow(
    filterState: HistoryFilterState,
) {
    val activeFilters = remember(filterState) {
        buildList {
            filterState.selectedType?.let {
                add("Type: ${formatSessionType(it)}")
            }
            filterState.selectedStatus?.let {
                add("Status: ${formatSessionStatus(it)}")
            }
            filterState.selectedMethod?.let {
                add("Method: ${formatAttendanceMethod(it)}")
            }
            if (filterState.showSuspiciousOnly) {
                add("Suspicious Only")
            }
            add("Sort: ${if (filterState.sortOrder == SortOrder.NEWEST_FIRST) "Newest" else "Oldest"}")
        }
    }

    if (activeFilters.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeFilters.forEach { filter ->
                    ElevatedAssistChip(
                        onClick = { /* Show filter dialog */ },
                        label = {
                            Text(filter)
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterFAB(
    filterState: HistoryFilterState,
    viewModel: HistoryViewModel
) {
    var showFilterDialog by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        onClick = { showFilterDialog = true },
        icon = {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter"
            )
        },
        text = {
            Text("Filter")
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )

    if (showFilterDialog) {
        FilterDialog(
            filterState = filterState,
            onDismiss = { showFilterDialog = false },
            onApply = { newState ->
                viewModel.updateFilter(newState)
                showFilterDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    filterState: HistoryFilterState,
    onDismiss: () -> Unit,
    onApply: (HistoryFilterState) -> Unit
) {
    var localState by remember { mutableStateOf(filterState) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Sessions",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Session Status Filter
                Text(
                    text = "Session Status",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    AttendanceSessionStatus.entries.forEach { status ->
                        val isSelected = localState.selectedStatus == status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    localState = localState.copy(
                                        selectedStatus = if (isSelected) null else status
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    localState = localState.copy(
                                        selectedStatus = if (isSelected) null else status
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatSessionStatus(status),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Session Type Filter
                Text(
                    text = "Session Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttendanceSessionType.entries.forEach { type ->
                        val isSelected = localState.selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                localState = localState.copy(
                                    selectedType = if (isSelected) null else type
                                )
                            },
                            label = {
                                Text(
                                    when (type) {
                                        AttendanceSessionType.REGULAR -> "Regular"
                                        AttendanceSessionType.MAKEUP -> "Make-up"
                                        AttendanceSessionType.SPECIAL -> "Special"
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Attendance Method Filter
                Text(
                    text = "Attendance Method",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    AttendanceMethod.entries.forEach { method ->
                        val isSelected = localState.selectedMethod == method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    localState = localState.copy(
                                        selectedMethod = if (isSelected) null else method
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    localState = localState.copy(
                                        selectedMethod = if (isSelected) null else method
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatAttendanceMethod(method),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Suspicious Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = localState.showSuspiciousOnly,
                        onCheckedChange = {
                            localState = localState.copy(showSuspiciousOnly = it)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show suspicious only",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Sort Order
                Text(
                    text = "Sort Order",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortOrder.entries.forEach { order ->
                        val isSelected = localState.sortOrder == order
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                localState = localState.copy(sortOrder = order)
                            },
                            label = {
                                Text(
                                    when (order) {
                                        SortOrder.NEWEST_FIRST -> "Newest First"
                                        SortOrder.OLDEST_FIRST -> "Oldest First"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(localState) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun AttendanceCard(record: StudentAttendanceRecordEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (record.isSuspicious) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with session status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = record.unitName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = record.unitCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    record.sessionTitle?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                SessionStatusBadge(status = record.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Session Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    SessionDetailRow(
                        icon = Icons.Default.Schedule,
                        text = record.attendedAt.formatDate()
                    )

                    SessionDetailRow(
                        icon = Icons.Default.Category,
                        text = formatSessionType(record.sessionType)
                    )
                }

                // Right column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    SessionDetailRow(
                        icon = getMethodIcon(record.attendanceMethodUsed),
                        text = formatAttendanceMethod(record.attendanceMethodUsed)
                    )

                    if (record.status == AttendanceSessionStatus.ENDED) {
                        SessionDetailRow(
                            icon = Icons.Default.CheckCircle,
                            text = "Attendance Recorded",
                            iconColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Suspicious Warning
            if (record.isSuspicious) {
                Spacer(modifier = Modifier.height(8.dp))
                SuspiciousWarning(
                    reason = record.suspiciousReason
                )
            }
        }
    }
}

@Composable
fun SuspiciousWarning(reason: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = "Suspicious",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = reason ?: "Marked as suspicious",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SessionStatusBadge(status: AttendanceSessionStatus) {
    val style = when (status) {
        AttendanceSessionStatus.SCHEDULED -> BadgeStyle(
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Default.Schedule,
            text = "Scheduled"
        )

        AttendanceSessionStatus.ACTIVE -> BadgeStyle(
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Default.PlayCircle,
            text = "Active"
        )

        AttendanceSessionStatus.ENDED -> BadgeStyle(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Default.CheckCircle,
            text = "Ended"
        )

        AttendanceSessionStatus.CANCELLED -> BadgeStyle(
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            textColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Default.Cancel,
            text = "Cancelled"
        )
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(style.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = style.text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = style.textColor
        )
    }
}

@Composable
fun SessionDetailRow(icon: ImageVector, text: String, iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun formatSessionStatus(status: AttendanceSessionStatus): String {
    return when (status) {
        AttendanceSessionStatus.SCHEDULED -> "Scheduled"
        AttendanceSessionStatus.ACTIVE -> "Active"
        AttendanceSessionStatus.ENDED -> "Ended"
        AttendanceSessionStatus.CANCELLED -> "Cancelled"
    }
}

fun formatSessionType(type: AttendanceSessionType): String {
    return when (type) {
        AttendanceSessionType.REGULAR -> "Regular Session"
        AttendanceSessionType.MAKEUP -> "Make-up Session"
        AttendanceSessionType.SPECIAL -> "Special Session"
    }
}

fun formatAttendanceMethod(method: AttendanceMethod): String {
    return when (method) {
        AttendanceMethod.QR_CODE -> "QR Code Scan"
        AttendanceMethod.MANUAL_CODE -> "Manual Code"
        AttendanceMethod.ANY -> "Any Method"
    }
}

fun getMethodIcon(method: AttendanceMethod): ImageVector {
    return when (method) {
        AttendanceMethod.QR_CODE -> Icons.Default.QrCodeScanner
        AttendanceMethod.MANUAL_CODE -> Icons.Default.Keyboard
        AttendanceMethod.ANY -> Icons.Default.MoreHoriz
    }
}


// Helper data class for styling the badge
private data class BadgeStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val text: String
)

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = "No records",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No attendance records found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Try adjusting your filters or attend a session to see records here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
