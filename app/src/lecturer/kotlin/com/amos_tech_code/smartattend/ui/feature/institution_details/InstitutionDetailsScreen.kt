package com.amos_tech_code.smartattend.ui.feature.institution_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionDetailsScreen(
    navController: NavController,
    institutionId: String,
    viewModel: InstitutionDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var showProgrammeSheet by remember { mutableStateOf<ProgrammeWithUnits?>(null) }
    var showAddProgrammeSheet by remember { mutableStateOf(false) }
    var showAddUnitSheet by remember { mutableStateOf(false) }
    var showTermSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(institutionId) {
        viewModel.onAction(InstitutionDetailsAction.LoadInstitution(institutionId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is InstitutionDetailsEvent.ShowSuccess -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is InstitutionDetailsEvent.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.error,
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Long
                    )
                }
            }
            InstitutionDetailsEvent.NavigateBack -> {
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onAction(InstitutionDetailsAction.DismissSuccess)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            InstitutionDetailsTopBar(
                title = state.institution?.university?.name ?: "Institution Details",
                isSaving = state.isSaving,
                onBackClick = {
                    focusManager.clearFocus()
                    navController.popBackStack()
                },
                onDeleteClick = { showDeleteConfirmation = true }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProgrammeSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Programme",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LoadingShimmer()
            } else if (state.institution == null) {
                EmptyState(
                    message = "Institution not found",
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                InstitutionContent(
                    state = state,
                    onAction = viewModel::onAction,
                    onEditProgramme = { programme -> showProgrammeSheet = programme },
                    onEditTerms = { showTermSheet = true },
                    onAddUnit = { showAddUnitSheet = true }
                )
            }

            if (showDeleteConfirmation) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        viewModel.onAction(InstitutionDetailsAction.DeleteInstitution)
                        showDeleteConfirmation = false
                    },
                    onDismiss = { showDeleteConfirmation = false }
                )
            }

            if (state.isSaving) {
                SavingOverlay()
            }
        }
    }

    // Edit Programme Sheet
    if (showProgrammeSheet != null) {
        val department = state.departments.find {
            it.id == showProgrammeSheet!!.programme.departmentId
        }

        ProgrammeEditBottomSheet(
            programme = showProgrammeSheet!!.programme,
            initialDepartmentName = department?.name ?: "",
            availableDepartments = state.departments,
            units = showProgrammeSheet!!.units,
            onDismiss = { showProgrammeSheet = null },
            onSave = { programmeEdit ->
                viewModel.onAction(InstitutionDetailsAction.UpdateProgramme(programmeEdit))
                showProgrammeSheet = null
            },
            onDelete = { programmeId ->
                viewModel.onAction(InstitutionDetailsAction.RemoveProgramme(programmeId))
                showProgrammeSheet = null
            }
        )
    }

    // Add Programme Sheet
    if (showAddProgrammeSheet) {
        ProgrammeAddBottomSheet(
            availableDepartments = state.departments,
            onDismiss = { showAddProgrammeSheet = false },
            onSave = { programmeEdit, units ->
                // Create the full request with units
                val programmeWithUnits = ProgrammeEdit(
                    id = programmeEdit.id,
                    name = programmeEdit.name,
                    departmentId = programmeEdit.departmentId,
                    departmentName = programmeEdit.departmentName,
                    yearOfStudy = programmeEdit.yearOfStudy,
                    expectedStudentCount = programmeEdit.expectedStudentCount,
                    isActive = programmeEdit.isActive
                )

                // Call addProgramme - the ViewModel will use units
                // We need to update the ViewModel to accept units
                viewModel.addProgrammeWithUnits(programmeWithUnits, units)
                showAddProgrammeSheet = false
            }
        )
    }

    // Add Unit Sheet
    if (showAddUnitSheet) {
        UnitEditBottomSheet(
            unit = null,
            programmes = state.institution?.programmes?.map { it.programme } ?: emptyList(),
            onDismiss = { showAddUnitSheet = false },
            onSave = { unitEdit, programmeId ->
                val selectedProgramme = state.institution?.programmes
                    ?.find { it.programme.id == programmeId }

                val departmentRef = DepartmentRef(
                    departmentId = selectedProgramme?.programme?.departmentId,
                    draftName = selectedProgramme?.programme?.let {
                        state.departments.find { dept -> dept.id == it.departmentId }?.name
                    }
                )

                val newUnit = NewUnitDraft(
                    code = unitEdit.code,
                    name = unitEdit.name,
                    semester = unitEdit.semester,
                    department = departmentRef
                )
                viewModel.onAction(InstitutionDetailsAction.AddUnit(newUnit, programmeId))
                showAddUnitSheet = false
            },
            onDelete = {}
        )
    }

    // Terms Sheet
    if (showTermSheet) {
        val allTerms = state.institution?.let {
            listOfNotNull(state.activeTerm)
        } ?: emptyList()

        AcademicTermBottomSheet(
            terms = allTerms,
            activeTerm = state.activeTerm,
            onDismiss = { showTermSheet = false },
            onAddTerm = { newTermDraft ->
                viewModel.onAction(InstitutionDetailsAction.AddAcademicTerm(newTermDraft))
                showTermSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstitutionDetailsTopBar(
    title: String,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete Institution") },
                    onClick = {
                        showMenu = false
                        onDeleteClick()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun InstitutionContent(
    state: InstitutionDetailsState,
    onAction: (InstitutionDetailsAction) -> Unit,
    onEditProgramme: (ProgrammeWithUnits) -> Unit,
    onEditTerms: () -> Unit,
    onAddUnit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            InstitutionHeaderCard(
                institution = state.institution!!,
                activeTerm = state.activeTerm,
                onEditTerms = onEditTerms,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Quick Stats
        item {
            QuickStatsGrid(
                institution = state.institution!!,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Academic Terms Section
        item {
            AcademicTermsSection(
                activeTerm = state.activeTerm,
                isExpanded = state.expandedSections.contains("terms"),
                onToggleExpand = { onAction(InstitutionDetailsAction.ToggleSection("terms")) },
                onEditTerms = onEditTerms,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Programmes Section
        item {
            ProgrammesSection(
                programmes = state.institution!!.programmes,
                isExpanded = state.expandedSections.contains("programmes"),
                onToggleExpand = { onAction(InstitutionDetailsAction.ToggleSection("programmes")) },
                onEditProgramme = onEditProgramme,
                onRemoveProgramme = { programmeId ->
                    onAction(InstitutionDetailsAction.RemoveProgramme(programmeId))
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Units Section
        item {
            UnitsOverviewSection(
                programmes = state.institution!!.programmes,
                isExpanded = state.expandedSections.contains("units"),
                onToggleExpand = { onAction(InstitutionDetailsAction.ToggleSection("units")) },
                onAddUnit = onAddUnit,
                onRemoveUnit = { programmeId, unitId ->
                    onAction(InstitutionDetailsAction.RemoveUnit(programmeId, unitId))
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun InstitutionHeaderCard(
    institution: UniversityWithProgrammesAndUnits,
    activeTerm: AcademicTermEntity?,
    onEditTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = institution.university.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                IconButton(
                    onClick = onEditTerms
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Edit Terms",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            activeTerm?.let { term ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Active Term: ${term.academicYear} - Semester ${term.semester}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ID: ${institution.university.id.takeLast(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun QuickStatsGrid(
    institution: UniversityWithProgrammesAndUnits,
    modifier: Modifier = Modifier
) {
    val totalUnits = institution.programmes.sumOf { it.units.size }
    val totalStudents = institution.programmes.sumOf { it.programme.expectedStudentCount }
    val totalDepartments = institution.programmes.map { it.programme.departmentId }.distinct().size

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            value = institution.programmes.size.toString(),
            label = "Programmes",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            value = totalUnits.toString(),
            label = "Units",
            icon = Icons.Default.Book,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            value = totalDepartments.toString(),
            label = "Departments",
            icon = Icons.Default.Business,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            value = totalStudents.toString(),
            label = "Students",
            icon = Icons.Default.People,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AcademicTermsSection(
    activeTerm: AcademicTermEntity?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExpandableSection(
        title = "Academic Terms",
        icon = Icons.Default.CalendarMonth,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        action = {
            IconButton(
                onClick = onEditTerms,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Terms",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = modifier
    ) {
        activeTerm?.let { term ->
            TermCard(term = term)
        } ?: run {
            Text(
                text = "No active term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun TermCard(
    term: AcademicTermEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                Text(
                    text = term.academicYear,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Semester ${term.semester}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgrammesSection(
    programmes: List<ProgrammeWithUnits>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditProgramme: (ProgrammeWithUnits) -> Unit,
    onRemoveProgramme: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExpandableSection(
        title = "Programmes",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            programmes.forEach { programmeWithUnits ->
                ProgrammeListItem(
                    programme = programmeWithUnits,
                    onEdit = { onEditProgramme(programmeWithUnits) },
                    onRemove = { onRemoveProgramme(programmeWithUnits.programme.id) }
                )
            }

            if (programmes.isEmpty()) {
                EmptyStateMessage(
                    message = "No programmes added yet",
                    icon = Icons.AutoMirrored.Filled.MenuBook
                )
            }
        }
    }
}

@Composable
private fun ProgrammeListItem(
    programme: ProgrammeWithUnits,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = programme.programme.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Year ${programme.programme.yearOfStudy}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${programme.units.size} units",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${programme.programme.expectedStudentCount} expected students",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove Programme") },
            text = { Text("Are you sure you want to remove this programme? This will deactivate your teaching assignments.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onRemove()
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UnitsOverviewSection(
    programmes: List<ProgrammeWithUnits>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddUnit: () -> Unit,
    onRemoveUnit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExpandableSection(
        title = "All Units",
        icon = Icons.Default.Book,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        action = {
            IconButton(
                onClick = onAddUnit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Unit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = modifier
    ) {
        val allUnits = programmes.flatMap { programme ->
            programme.units.map { unit ->
                Triple(programme.programme.id, programme.programme, unit)
            }
        }

        if (allUnits.isEmpty()) {
            EmptyStateMessage(
                message = "No units added yet",
                icon = Icons.Outlined.Book
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allUnits.forEach { (programmeId, programme, unit) ->
                    UnitListItem(
                        programme = programme,
                        unit = unit,
                        onRemove = { onRemoveUnit(programmeId, unit.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitListItem(
    programme: ProgrammeEntity,
    unit: UnitEntity,
    onRemove: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${unit.code} - ${unit.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = programme.name.take(15),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "Sem ${unit.semester}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (unit.lectureDay != null || unit.lectureTime != null || unit.lectureVenue != null) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        unit.lectureDay?.let { day ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        unit.lectureTime?.let { time ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        unit.lectureVenue?.let { venue ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = venue,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove Unit") },
            text = { Text("Are you sure you want to remove this unit from the programme?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onRemove()
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                action?.invoke()

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isExpanded) 180f else 0f)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .animateContentSize(),
                content = content
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun EmptyStateMessage(
    message: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyState(
    message: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Go Back")
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete Institution") },
        text = {
            Text(
                "Are you sure you want to delete this institution? " +
                        "This will permanently remove all associated programmes, units, and academic terms. " +
                        "This action cannot be undone."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SavingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Saving changes...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun LoadingShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(6) {
            ShimmerCard()
        }
    }
}

@Composable
private fun ShimmerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
        )
    }
}