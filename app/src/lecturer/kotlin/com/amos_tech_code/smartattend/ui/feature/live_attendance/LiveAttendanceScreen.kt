package com.amos_tech_code.smartattend.ui.feature.live_attendance

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.WrongLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendWidthSpacer
import com.amos_tech_code.smartattend.ui.feature.start_session.SessionSuccessScreen
import com.amos_tech_code.smartattend.ui.navigation.BottomNavigation
import com.amos_tech_code.smartattend.ui.theme.LiveAttendanceAmber
import com.amos_tech_code.smartattend.ui.theme.LiveAttendanceGreen
import com.amos_tech_code.smartattend.ui.theme.LiveAttendanceRed
import com.amos_tech_code.smartattend.ui.theme.attendanceColors
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import com.amos_tech_code.smartattend.utils.toAmPmTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LiveAttendanceScreen(
    navController: NavController,
    viewModel: LiveAttendanceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showConfirmEndSessionDialog by remember { mutableStateOf(false) }
    var showFiltersMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedTab by remember(state.filterOptions.showOnlyFlagged) {
        // When showOnlyFlagged is true, force tab 1, otherwise use saved tab
        val initialTab = if (state.filterOptions.showOnlyFlagged) 1 else 0
        mutableIntStateOf(initialTab)
    }
    // Collect events
    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is LiveAttendanceEvent.ShowErrorMessage -> {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is LiveAttendanceEvent.ShowSuccessMessage -> {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            //is LiveAttendanceEvent.NoActiveSession
            LiveAttendanceEvent.SessionEndedSuccessfully -> {
                navController.popBackStack()
            }
            else -> {}
        }
    }

    // Show filter indicator if active filters
    val showFilterIndicator = state.hasActiveFilters()
    val filterDescription = state.getActiveFilterDescription()
    // Define the list of actions for the speed dial menu
    val speedDialItems = listOf(
        SpeedDialItem(
            id = "refresh",
            label = "Refresh",
            icon = Icons.Outlined.Refresh,
            tint = MaterialTheme.colorScheme.primary
        ),
        SpeedDialItem(
            id = "filter",
            label = "Filter",
            icon = Icons.Outlined.FilterList,
            tint = MaterialTheme.colorScheme.primary
        ),
        SpeedDialItem(
            id = "sort",
            label = "Sort",
            icon = Icons.AutoMirrored.Outlined.Sort,
            tint = MaterialTheme.colorScheme.primary
        ),
        SpeedDialItem(
            id = "end_session",
            label = "End Session",
            icon = Icons.Outlined.Stop,
            tint = MaterialTheme.colorScheme.error
        ),
        SpeedDialItem(
            id = "session_details",
            label = "Session Details",
            icon = Icons.Outlined.QrCodeScanner,
            tint = MaterialTheme.colorScheme.primary
        )
    )
    // Animated background for live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "liveIndicator")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "livePulse"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LiveAttendanceTopBar(
                scrollBehavior = scrollBehavior,
                state = state,
                connectionState = state.connectionState,
                onBackClick = { navController.popBackStack() },
                liveAlpha = alpha,
                showFilterIndicator = showFilterIndicator,
                filterDescription = filterDescription
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                isVisible = scrollBehavior.state.collapsedFraction < 0.5f  //!lazyListState.isScrollInProgress || !lazyListState.isScrollingUp()
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.large
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !state.showSessionDetails || scrollBehavior.state.collapsedFraction < 0.5f,
                enter = slideInHorizontally { it / 2 } + fadeIn(),
                exit = slideOutHorizontally { it / 2 } + fadeOut()
            ) {
                SpeedDialFab(
                    items = speedDialItems,
                    onItemClick = { item ->
                        when (item.id) {
                            "refresh" -> viewModel.onAction(LiveAttendanceUIEvent.RefreshData)
                            "filter" -> showFiltersMenu = true
                            "sort" -> showSortMenu = true
                            "end_session" -> showConfirmEndSessionDialog = true
                            "session_details" -> viewModel.onAction(LiveAttendanceUIEvent.ShowSessionDetails)
                        }
                    }
                )
            }
        }
    )
    { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 8.dp) // Only bottom padding needed now
            ) {
                // Attendance Summary with Chart
                item {
                    AttendanceChartSection(
                        stats = state.attendanceStats,
                        hasActiveFilters = showFilterIndicator,
                        filterDescription = filterDescription,
                        onClearFilters = {
                            viewModel.onAction(LiveAttendanceUIEvent.ClearFilters)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // --- STICKY HEADER FOR TABS ---
                stickyHeader {
                    StudentsTabSection(
                        state = state,
                        selectedTabIndex = selectedTab,
                        onTabSelected = { selectedTab = it },
                    )
                }

                // --- DYNAMIC STUDENT LISTS ---
                when (selectedTab) {
                    0 -> { // Present students (non-flagged)
                        if (state.approvedStudents.isEmpty()) {
                            item {
                                EmptyAttendanceState(
                                    title = if (showFilterIndicator) "No Matching Students" else "No Attendance Yet",
                                    message = if (showFilterIndicator)
                                        "No non-flagged students match your filters"
                                    else "Attendance will appear here when students start marking",
                                    showClearFilters = showFilterIndicator,
                                    onClearFilters = { viewModel.onAction(LiveAttendanceUIEvent.ClearFilters) },
                                    onRefresh = { viewModel.onAction(LiveAttendanceUIEvent.RefreshData) },
                                    modifier = Modifier.fillParentMaxSize()
                                )
                            }
                        } else {
                            items(state.approvedStudents, key = { "present-${it.student.id}" }) { student ->
                                StudentAttendanceCard(
                                    student = student,
                                    showArrivalAnimation = true,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    1 -> { // Flagged students
                        if (state.flaggedStudents.isEmpty()) {
                            item {
                                EmptyAttendanceState(
                                    title = if (showFilterIndicator) "No Flagged Students" else "No Flagged Students",
                                    message = if (showFilterIndicator)
                                        "No flagged students match your filters"
                                    else "No suspicious attendance records yet",
                                    showClearFilters = showFilterIndicator,
                                    onClearFilters = { viewModel.onAction(LiveAttendanceUIEvent.ClearFilters) },
                                    onRefresh = { viewModel.onAction(LiveAttendanceUIEvent.RefreshData) },
                                    modifier = Modifier.fillParentMaxSize()
                                )
                            }
                        } else {
                            items(state.flaggedStudents, key = { "flagged-${it.student.id}" }) { student ->
                                FlaggedStudentCard(
                                    student = student,
                                    onRemove = { viewModel.onAction(LiveAttendanceUIEvent.RemoveFlaggedStudent(student.student.id, student.student.name)) },
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

            }

            // Connection Status Banner
            AnimatedVisibility(
                visible = state.connectionState != ConnectionState.CONNECTED,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                ConnectionStatusBanner(
                    connectionState = state.connectionState,
                    onRetry = { viewModel.onAction(LiveAttendanceUIEvent.Reconnect) }
                )
            }
        }

        // Loading Overlay
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingOverlay()
        }
    }

    // QR Code Overlay
    AnimatedVisibility(
        visible = state.showSessionDetails,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        state.session?.let { session ->
            SessionSuccessScreen(
                scope = scope,
                sessionResponse = session,
                shouldShowSessionSuccess = false,
                onLiveAttendanceClick = { viewModel.onAction(LiveAttendanceUIEvent.HideSessionDetails) },
                onBackToHome = {
                    viewModel.onAction(LiveAttendanceUIEvent.HideSessionDetails)
                }
            )
        }
    }

    // End Session Confirmation Dialog
    if (showConfirmEndSessionDialog) {
        EndSessionDialog(
            onDismiss = { showConfirmEndSessionDialog = false },
            onConfirm = {
                showConfirmEndSessionDialog = false
                state.session?.sessionId?.let { viewModel.onAction(LiveAttendanceUIEvent.EndSession(it)) }
            }
        )
    }

    // Filters Menu
    if (showFiltersMenu) {
        FiltersMenu(
            state = state,
            onDismiss = { showFiltersMenu = false },
            onApplyFilter = { programmeId ->
                viewModel.onAction(LiveAttendanceUIEvent.ApplyFilter(programmeId))
            },
            onToggleFlaggedOnly = { enabled ->
                viewModel.onAction(LiveAttendanceUIEvent.ToggleFlaggedFilter(enabled))
            },
            onClearFilters = {
                viewModel.onAction(LiveAttendanceUIEvent.ClearFilters)
            }
        )
    }

    // Sort Menu
    if (showSortMenu) {
        SortMenu(
            state = state,
            onDismiss = { showSortMenu = false },
            onApplySort = { sortBy ->
                viewModel.onAction(LiveAttendanceUIEvent.ApplySort(sortBy))
            },
            onToggleSortOrder = { sortOrder ->
                viewModel.onAction(LiveAttendanceUIEvent.ToggleSortOrder(sortOrder))
            },
            onClearSort = {
                viewModel.onAction(LiveAttendanceUIEvent.ClearSort)
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun LiveAttendanceTopBar(
    state: LiveAttendanceState,
    connectionState: ConnectionState,
    scrollBehavior: TopAppBarScrollBehavior,
    liveAlpha: Float,
    showFilterIndicator: Boolean,
    filterDescription: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Column(
                modifier = Modifier.animateContentSize()
            ) {
                Text(
                    text = "Live Attendance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (state.session != null) {
                    AnimatedContent(
                        targetState = state.session.unit.name,
                        transitionSpec = {
                            slideInVertically { -it }.togetherWith(slideOutVertically { it })
                        }
                    ) { unitName ->
                        Text(
                            text = unitName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Live Indicator with Connection Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                color = when (connectionState) {
                                    ConnectionState.CONNECTED ->
                                        LiveAttendanceGreen.copy(alpha = liveAlpha)

                                    ConnectionState.CONNECTING ->
                                        LiveAttendanceAmber

                                    ConnectionState.DISCONNECTED ->
                                        LiveAttendanceRed
                                },
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = when (connectionState) {
                            ConnectionState.CONNECTED -> "LIVE"
                            ConnectionState.CONNECTING -> "CONNECTING..."
                            ConnectionState.DISCONNECTED -> "DISCONNECTED"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (connectionState) {
                            ConnectionState.CONNECTED -> LiveAttendanceGreen
                            ConnectionState.CONNECTING -> LiveAttendanceAmber
                            ConnectionState.DISCONNECTED -> LiveAttendanceRed
                        }
                    )
                    if (showFilterIndicator) {
                        Text(
                            text = "• $filterDescription",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (connectionState == ConnectionState.CONNECTED && state.lastUpdate.isNotEmpty()) {
                        Text(
                            text = "• Updated ${state.lastUpdate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        actions = {
            // Show filter badge if active
            if (showFilterIndicator) {
                Badge(
                    containerColor = MaterialTheme.attendanceColors.flagged,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Filter", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    )
}

@Composable
private fun AttendanceChartSection(
    stats: AttendanceStats,
    hasActiveFilters: Boolean,
    filterDescription: String,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attendanceColors = MaterialTheme.attendanceColors
    val percentages = remember(stats) {
        listOf(
            stats.approvedPercentage,
            stats.flaggedPercentage,
            stats.absentPercentage
        )
    }
    val colors = listOf(
        attendanceColors.present,
        attendanceColors.flagged,
        attendanceColors.absent,
    )
    val labels = listOf("Approved",  "Flagged", "Absent")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "%.1f%%".format(stats.attendancePercentage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        stats.attendancePercentage >= 80 -> attendanceColors.present
                        stats.attendancePercentage >= 60 -> attendanceColors.pending
                        else -> attendanceColors.absent
                    }
                )
            }
            if (hasActiveFilters) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = filterDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    TextButton(
                        onClick = onClearFilters,
                        modifier = Modifier.height(24.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Progress Bar with Labels
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        percentages.forEachIndexed { index, percentage ->
                            if (percentage > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(percentage)
                                        .background(colors[index])
                                )
                            }
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    percentages.forEachIndexed { index, percentage ->
                        if (percentage > 0) {
                            LegendItem(
                                color = colors[index],
                                label = labels[index],
                                percentage = percentage
                            )
                        }
                    }
                }
            }

            // Stats Breakdown
            StatsBreakdown(stats = stats)
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    percentage: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label (${percentage.toInt()}%)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun StatsBreakdown(stats: AttendanceStats) {
    val attendanceColors = MaterialTheme.attendanceColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BreakdownItem(
            value = "${stats.totalAttended} / ${stats.totalExpectedStudents}",
            label = "Attended",
            color = attendanceColors.present,
            icon = Icons.Outlined.CheckCircle
        )
        BreakdownItem(
            value = stats.totalFlagged.toString(),
            label = "Flagged",
            color = attendanceColors.flagged,
            icon = Icons.Outlined.Flag
        )
        BreakdownItem(
            value = stats.totalAbsent.toString(),
            label = "Absent",
            color = attendanceColors.absent,
            icon = Icons.Outlined.Cancel
        )
    }
}

@Composable
private fun BreakdownItem(
    value: String,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StudentsTabSection(
    state: LiveAttendanceState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit, // Callback to notify parent
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            listOf(
                TabItem("Approved", state.approvedStudents.size, Icons.Outlined.CheckCircle),
                TabItem("Flagged", state.flaggedStudents.size, Icons.Outlined.Flag),
            ).forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) }, // Use the callback
                ) {
                    TabContent(tab = tab, isSelected = selectedTabIndex == index)
                }
            }
        }
    }
}

@Composable
private fun TabContent(tab: TabItem, isSelected: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.title,
            modifier = Modifier.size(18.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = tab.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Badge(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Text(
                text = tab.count.toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

data class TabItem(
    val title: String,
    val count: Int,
    val icon: ImageVector
)

@Composable
private fun StudentAttendanceCard(
    student: StudentAttendance,
    showArrivalAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(!showArrivalAnimation) }

    LaunchedEffect(Unit) {
        if (showArrivalAnimation) {
            delay(100)
            isVisible = true
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it * 2 },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = fadeOut() + shrinkOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.attendanceColors.presentLight
            ),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.attendanceColors.present.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.attendanceColors.present.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.attendanceColors.present,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Student Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = student.student.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = student.student.registrationNo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Attendance Details
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = student.timestamp.toAmPmTime(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FlaggedStudentCard(
    student: StudentAttendance,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.attendanceColors.flaggedLight
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.attendanceColors.flagged.copy(alpha = 0.3f)
        )
    ) {
        var isExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.attendanceColors.flagged.copy(alpha = 0.1f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = "Flagged",
                            tint = MaterialTheme.attendanceColors.flagged,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = student.student.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = student.student.registrationNo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Flag Reasons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Use a smaller spacing for items in a list
                    ) {
                        Text(
                            text = "Flagged For:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Parse the reason string and display each flag
                        student.suspiciousReason?.split(",")?.forEach { reason ->
                            if (reason.isNotBlank()) {
                                FlagReasonItem(reason = reason)
                            }
                        }
                    }

                    // Divider
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Marked at ${student.timestamp.toAmPmTime()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onRemove,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.attendanceColors.flagged,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Remove")
                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun FlagReasonItem(reason: String) {
    val (icon, text) = when (reason.trim()) {
        "LOCATION_MISMATCH" -> Icons.Outlined.WrongLocation to "Location Mismatch"
        "OUTSIDE_SCHEDULE_WINDOW" -> Icons.Outlined.Schedule to "Outside Schedule"
        else -> Icons.AutoMirrored.Outlined.HelpOutline to reason // Fallback for unknown reasons
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Animated loading indicator
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Loading",
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Loading Live Attendance...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EndSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.attendanceColors.absent.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Stop,
                        contentDescription = "End Session",
                        tint = MaterialTheme.attendanceColors.absent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "End Live Session",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Are you sure you want to end this live attendance session?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• No new attendance can be marked\n• Session data will be finalized\n• Reports will be generated",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        },
        dismissButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.attendanceColors.absent,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("End Session")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun ConnectionStatusBanner(
    connectionState: ConnectionState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusInfo = when (connectionState) {
        ConnectionState.CONNECTING ->
            ConnectionStatusInfo(
                message = "Connecting...",
                icon = Icons.Outlined.Refresh,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                showRetry = false
            )
        ConnectionState.DISCONNECTED ->
            ConnectionStatusInfo(
                message = "Connection Lost. Live updates are paused.",
                icon = Icons.Outlined.WifiOff,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                showRetry = true
            )
        else -> null
    }

    statusInfo?.let { info ->
        // Use a Surface for a full-width banner look
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = info.containerColor,
            contentColor = info.contentColor,
            shadowElevation = 4.dp // Add a subtle shadow to lift it off the content
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false) // Prevent this from stretching too much
                ) {
                    // Animate the icon for the "Connecting" state
                    if (connectionState == ConnectionState.CONNECTING) {
                        val infiniteTransition = rememberInfiniteTransition(label = "connecting-rotation")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing)
                            ),
                            label = "angle"
                        )
                        Icon(
                            imageVector = info.icon,
                            contentDescription = null,
                            modifier = Modifier.rotate(angle)
                        )
                    } else {
                        Icon(
                            imageVector = info.icon,
                            contentDescription = null
                        )
                    }

                    Text(
                        text = info.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (info.showRetry) {
                    TextButton(onClick = onRetry) {
                        Text(
                            "RETRY",
                            fontWeight = FontWeight.Bold,
                            color = info.contentColor
                        )
                    }
                }
            }
        }
    }
}

// Simplified data class
data class ConnectionStatusInfo(
    val message: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val showRetry: Boolean
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SpeedDialFab(
    items: List<SpeedDialItem>,
    onItemClick: (SpeedDialItem) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    // If there are no items to show, don't display the FAB at all.
    // This handles the case where the TopAppBar is expanded.
    if (items.isEmpty()) {
        // You can have a Spacer or an empty Box, but returning Unit is cleanest.
        return
    }

    FloatingActionButtonMenu(
        expanded = isExpanded,
        // The main button that toggles the menu
        button = {
            ToggleFloatingActionButton(
                checked = isExpanded,
                onCheckedChange = { isExpanded = it }, // Always just toggle the expanded state
            ) {
                // The icon now correctly animates between Menu and Close
                val icon = if (isExpanded) Icons.Default.Close else Icons.Default.Menu
                Icon(
                    imageVector = icon,
                    contentDescription = "Actions",
                )
            }
        }
    ) {
        // The menu items that appear when expanded
        items.forEach { item ->
            FloatingActionButtonMenuItem(
                onClick = {
                    isExpanded = false // Collapse the menu
                    onItemClick(item) // Perform the item's action
                },
                text = {
                    Text(text = item.label, style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ))
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = item.tint
                    )
                },
            )
        }
    }
}

// Data class to define an item in the speed dial menu
private data class SpeedDialItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val tint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersMenu(
    state: LiveAttendanceState,
    onDismiss: () -> Unit,
    onApplyFilter: (String?) -> Unit,
    onToggleFlaggedOnly: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSelectedProgramme by remember(state.filterOptions.selectedProgrammeId) {
        mutableStateOf(state.filterOptions.selectedProgrammeId)
    }
    var tempShowOnlyFlagged by remember(state.filterOptions.showOnlyFlagged) {
        mutableStateOf(state.filterOptions.showOnlyFlagged)
    }

    val hasChanges = remember(tempSelectedProgramme, tempShowOnlyFlagged) {
        tempSelectedProgramme != state.filterOptions.selectedProgrammeId ||
                tempShowOnlyFlagged != state.filterOptions.showOnlyFlagged
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.widthIn(max = 400.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Filter Attendance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Programme Filter (only show if there are multiple programmes)
                if (state.programmes.size > 1) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Filter by Programme",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = tempSelectedProgramme == null,
                                    onClick = { tempSelectedProgramme = null },
                                    label = {
                                        Text("All Programmes", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    //modifier = Modifier.animateItemPlacement()
                                )
                            }

                            items(state.programmeOptions, key = { it.id }) { programme ->
                                FilterChip(
                                    selected = tempSelectedProgramme == programme.id,
                                    onClick = {
                                        tempSelectedProgramme = if (tempSelectedProgramme == programme.id) {
                                            null
                                        } else {
                                            programme.id
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = programme.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    //modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }
                    }
                }

                // Flagged Only Filter
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.attendanceColors.flaggedLight.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.attendanceColors.flagged.copy(alpha = 0.2f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Flag,
                                    contentDescription = "Flagged Only",
                                    tint = MaterialTheme.attendanceColors.flagged,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Show only flagged students",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Display only suspicious attendance records",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = tempShowOnlyFlagged,
                            onCheckedChange = { tempShowOnlyFlagged = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.attendanceColors.flagged,
                                checkedTrackColor = MaterialTheme.attendanceColors.flagged.copy(alpha = 0.5f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Current Filter Status
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Filter Preview:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        val filterDescription = buildString {
                            append("Showing ")
                            if (tempSelectedProgramme != null) {
                                val programmeName = state.programmes.find { it.programmeId == tempSelectedProgramme }?.programmeName
                                append("${programmeName ?: "Selected Programme"} ")
                            } else {
                                append("All Programmes ")
                            }

                            if (tempShowOnlyFlagged) {
                                append("• Flagged Only")
                            } else {
                                append("• All Students")
                            }

                            // Add expected count
                            val expectedCount = if (tempSelectedProgramme != null) {
                                state.programmes.find { it.programmeId == tempSelectedProgramme }
                                    ?.yearGroups?.sumOf { it.noOfExpectedStudents } ?: 0
                            } else {
                                state.programmes.flatMap { it.yearGroups }.sumOf { it.noOfExpectedStudents }
                            }

                            val filteredCount = if (tempSelectedProgramme != null) {
                                state.allStudents.count { it.programmeId == tempSelectedProgramme }
                            } else {
                                state.allStudents.size
                            }

                            if (tempShowOnlyFlagged) {
                                val flaggedCount = if (tempSelectedProgramme != null) {
                                    state.allStudents.count {
                                        it.programmeId == tempSelectedProgramme && it.isSuspicious
                                    }
                                } else {
                                    state.allStudents.count { it.isSuspicious }
                                }
                                append(" ($flaggedCount of $filteredCount attended)")
                            } else {
                                append(" ($filteredCount of $expectedCount attended)")
                            }
                        }

                        Text(
                            text = filterDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.hasActiveFilters() || hasChanges) {
                    OutlinedButton(
                        onClick = {
                            onClearFilters()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("Clear All")
                    }
                }

                Button(
                    onClick = {
                        if (tempSelectedProgramme != state.filterOptions.selectedProgrammeId) {
                            onApplyFilter(tempSelectedProgramme)
                        }
                        if (tempShowOnlyFlagged != state.filterOptions.showOnlyFlagged) {
                            onToggleFlaggedOnly(tempShowOnlyFlagged)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hasChanges,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Apply Filters")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortMenu(
    state: LiveAttendanceState,
    onDismiss: () -> Unit,
    onApplySort: (SortBy) -> Unit,
    onToggleSortOrder: (SortOrder) -> Unit,
    onClearSort: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSortBy by remember(state.sortOptions.sortBy) {
        mutableStateOf(state.sortOptions.sortBy)
    }
    var tempSortOrder by remember(state.sortOptions.sortOrder) {
        mutableStateOf(state.sortOptions.sortOrder)
    }

    val hasChanges = remember(tempSortBy, tempSortOrder) {
        tempSortBy != state.sortOptions.sortBy ||
                tempSortOrder != state.sortOptions.sortOrder
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.widthIn(max = 400.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sort Students",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Sort Options
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sort By",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val sortOptions = listOf(
                        SortOption(
                            sortBy = SortBy.NAME,
                            name = "Name",
                            description = "Sort by student name",
                            icon = Icons.Outlined.Person
                        ),
                        SortOption(
                            sortBy = SortBy.REG_NO,
                            name = "Registration No",
                            description = "Sort by registration number",
                            icon = Icons.Outlined.Numbers
                        ),
                        SortOption(
                            sortBy = SortBy.TIME,
                            name = "Attendance Time",
                            description = "Sort by when attendance was marked",
                            icon = Icons.Outlined.Schedule
                        ),
                        SortOption(
                            sortBy = SortBy.STATUS,
                            name = "Status",
                            description = "Sort by attendance status",
                            icon = Icons.Outlined.Flag
                        )
                    )

                    sortOptions.forEach { option ->
                        SortOptionItem(
                            option = option,
                            isSelected = tempSortBy == option.sortBy,
                            onClick = { tempSortBy = option.sortBy }
                        )
                    }
                }

                // Sort Order
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sort Order",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortOrder.values().forEach { order ->
                            FilterChip(
                                selected = tempSortOrder == order,
                                onClick = { tempSortOrder = order },
                                label = {
                                    Text(
                                        text = order.displayName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sort Preview
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Sort Preview:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        val sortOption = when (tempSortBy) {
                            SortBy.NAME -> "Name"
                            SortBy.REG_NO -> "Registration Number"
                            SortBy.TIME -> "Attendance Time"
                            SortBy.STATUS -> "Status (Flagged/Not Flagged)"
                        }

                        Text(
                            text = "• $sortOption (${tempSortOrder.displayName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Example of how it will look
                        val exampleText = when (tempSortBy) {
                            SortBy.NAME -> "Students will be sorted alphabetically by name"
                            SortBy.REG_NO -> "Students will be sorted by registration number"
                            SortBy.TIME -> "${if (tempSortOrder == SortOrder.DESCENDING) "Newest" else "Oldest"} attendance first"
                            SortBy.STATUS -> "${if (tempSortOrder == SortOrder.ASCENDING) "Flagged" else "Not flagged"} students first"
                        }

                        Text(
                            text = exampleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.sortOptions.sortBy != SortBy.TIME || state.sortOptions.sortOrder != SortOrder.DESCENDING) {
                    OutlinedButton(
                        onClick = {
                            onClearSort()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("Reset to Default")
                    }
                }

                Button(
                    onClick = {
                        if (tempSortBy != state.sortOptions.sortBy) {
                            onApplySort(tempSortBy)
                        }
                        if (tempSortOrder != state.sortOptions.sortOrder) {
                            onToggleSortOrder(tempSortOrder)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hasChanges,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Apply Sort")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun SortOptionItem(
    option: SortOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = if (isSelected) CardDefaults.cardElevation(defaultElevation = 2.dp)
        else CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.name,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isSelected) 0.8f else 0.6f
                    )
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

data class SortOption(
    val sortBy: SortBy,
    val name: String,
    val description: String,
    val icon: ImageVector
)


@Composable
private fun EmptyAttendanceState(
    title: String,
    message: String,
    showClearFilters: Boolean,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.People,
                contentDescription = "No Attendance",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

       SmartAttendHeightSpacer(16.dp)

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        SmartAttendHeightSpacer(24.dp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showClearFilters) {
                OutlinedButton(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("Clear Filters")
                }
            }

            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                SmartAttendWidthSpacer(8.dp)
                Text("Refresh")
            }
        }
    }
}