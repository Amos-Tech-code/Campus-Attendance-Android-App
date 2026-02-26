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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusManager
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
import com.amos_tech_code.smartattend.domain.request.DepartmentRef
import com.amos_tech_code.smartattend.domain.request.NewProgrammeDraft
import com.amos_tech_code.smartattend.domain.request.NewUnitDraft
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
    var showUnitSheet by remember { mutableStateOf<Pair<UnitEntity?, ProgrammeEntity?>?>(null) }
    var showTermSheet by remember { mutableStateOf(false) }
    var showAddProgrammeSheet by remember { mutableStateOf(false) }
    var showAddUnitSheet by remember { mutableStateOf(false) }

    LaunchedEffect(institutionId) {
        viewModel.onAction(InstitutionDetailsAction.LoadInstitution(institutionId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is InstitutionDetailsEvent.ShowMessage -> {
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
                isEditing = state.editMode != EditMode.VIEW,
                isSaving = state.isSaving,
                onBackClick = {
                    focusManager.clearFocus()
                    if (state.editMode != EditMode.VIEW) {
                        viewModel.onAction(InstitutionDetailsAction.CancelChanges)
                    } else {
                        navController.popBackStack()
                    }
                },
                onEditClick = {
                    viewModel.onAction(InstitutionDetailsAction.ToggleEditMode(EditMode.EDIT))
                },
                onSaveClick = {
                    focusManager.clearFocus()
                    viewModel.onAction(InstitutionDetailsAction.SaveChanges)
                },
                onCancelClick = {
                    focusManager.clearFocus()
                    viewModel.onAction(InstitutionDetailsAction.CancelChanges)
                },
                onDeleteClick = {
                    viewModel.onAction(InstitutionDetailsAction.ConfirmDelete)
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
        floatingActionButton = {
            if (state.editMode != EditMode.VIEW) {
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
                    focusManager = focusManager,
                    onEditProgramme = { programme -> showProgrammeSheet = programme },
                    onEditUnit = { unit, programme -> showUnitSheet = Pair(unit, programme) },
                    onEditTerms = { showTermSheet = true },
                    onAddUnit = { showAddUnitSheet = true },
                    onAddProgramme = { showAddProgrammeSheet = true },
                )
            }

            if (state.showDeleteConfirmation) {
                DeleteConfirmationDialog(
                    onConfirm = { viewModel.onAction(InstitutionDetailsAction.DeleteInstitution) },
                    onDismiss = { viewModel.onAction(InstitutionDetailsAction.DismissDelete) }
                )
            }

            if (state.isSaving) {
                SavingOverlay()
            }
        }
    }

    // Bottom Sheets
    if (showProgrammeSheet != null) {
        // Get the department name from your data
        val departmentName = state.departments.find {
            it.id == showProgrammeSheet!!.programme.departmentId
        }?.name ?: ""

        ProgrammeEditBottomSheet(
            programme = showProgrammeSheet!!.programme,
            initialDepartmentName = departmentName,
            units = showProgrammeSheet!!.units,
            onDismiss = { showProgrammeSheet = null },
            onSave = { programmeEdit, unitEdits ->
                viewModel.onAction(InstitutionDetailsAction.UpdateProgramme(programmeEdit))
                unitEdits.forEach { unitEdit ->
                    viewModel.onAction(InstitutionDetailsAction.UpdateUnit(unitEdit))
                }
                showProgrammeSheet = null
            },
            onDelete = { programmeId ->
                viewModel.onAction(InstitutionDetailsAction.RemoveProgramme(programmeId))
                showProgrammeSheet = null
            }
        )
    }

    if (showUnitSheet != null) {
        val (unit, programme) = showUnitSheet!!
        UnitEditBottomSheet(
            unit = unit,
            programmes = state.institution?.programmes?.map { it.programme } ?: emptyList(),
            //selectedProgrammeId = programme?.id,
            onDismiss = { showUnitSheet = null },
            onSave = { unitEdit, programmeId ->
                viewModel.onAction(InstitutionDetailsAction.UpdateUnit(unitEdit))
                showUnitSheet = null
            },
            onDelete = { unitId ->
                viewModel.onAction(InstitutionDetailsAction.RemoveUnit(unitId))
                showUnitSheet = null
            }
        )
    }

    if (showTermSheet) {
        val allTerms = state.institution?.let {
            // You'd need to fetch all terms, not just active term
            listOfNotNull(state.activeTerm)
        } ?: emptyList()

        AcademicTermBottomSheet(
            terms = allTerms,
            activeTerm = state.activeTerm,
            onDismiss = { showTermSheet = false },
            onSetActive = { termId ->
                viewModel.onAction(InstitutionDetailsAction.SetActiveTerm(termId))
                showTermSheet = false
            },
            onAddTerm = { newTermDraft ->
                viewModel.onAction(InstitutionDetailsAction.AddAcademicTerm(newTermDraft))
                showTermSheet = false
            }
        )
    }

    if (showAddProgrammeSheet) {
        ProgrammeEditBottomSheet(
            programme = null,
            units = emptyList(),
            onDismiss = { showAddProgrammeSheet = false },
            onSave = { programmeEdit, _ ->
                val programmeWithTempId = ProgrammeEdit(
                    id = UUID.randomUUID().toString(), // ✅ TEMP ID GENERATED HERE
                    name = programmeEdit.name,
                    departmentId = programmeEdit.departmentId,
                    departmentName = programmeEdit.departmentName,
                    yearOfStudy = programmeEdit.yearOfStudy,
                    expectedStudentCount = programmeEdit.expectedStudentCount,
                    isActive = true
                )

                viewModel.onAction(
                    InstitutionDetailsAction.AddProgramme(programmeWithTempId)
                )
                showAddProgrammeSheet = false
            },
            initialDepartmentName = "",
            onDelete = {

            }
        )
    }

    if (showAddUnitSheet) {
        UnitEditBottomSheet(
            unit = null,
            programmes = state.institution?.programmes?.map { it.programme } ?: emptyList(),
            onDismiss = { showAddUnitSheet = false },
            onSave = { unitEdit, programmeId ->
                // Find the selected programme
                val selectedProgramme = state.institution?.programmes
                    ?.find { it.programme.id == programmeId }

                // Use the programme's department for the new unit
                val departmentRef = DepartmentRef(
                    departmentId = selectedProgramme?.programme?.departmentId,
                    draftName = selectedProgramme?.programme?.let {
                        state.departments.find { dept -> dept.id == it.departmentId }?.name
                    }
                )

                val newUnit = NewUnitDraft(
                    code = unitEdit.code,
                    name = unitEdit.name,
                    department = departmentRef
                )
                viewModel.onAction(InstitutionDetailsAction.AddUnit(newUnit, programmeId))
                showAddUnitSheet = false
            },
            onDelete = {}
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstitutionDetailsTopBar(
    title: String,
    isEditing: Boolean,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditing) "Editing Mode" else title,
                    style = if (isEditing)
                        MaterialTheme.typography.titleMedium
                    else
                        MaterialTheme.typography.titleLarge,
                    fontWeight = if (isEditing) FontWeight.Normal else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (isEditing) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
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
            if (isEditing) {
                TextButton(
                    onClick = onCancelClick,
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSaveClick,
                    enabled = !isSaving,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Save",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            } else {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Institution") },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
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
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstitutionContent(
    state: InstitutionDetailsState,
    onAction: (InstitutionDetailsAction) -> Unit,
    focusManager: FocusManager,
    onAddProgramme: () -> Unit,
    onEditProgramme: (ProgrammeWithUnits) -> Unit,
    onEditUnit: (UnitEntity, ProgrammeEntity) -> Unit,
    onEditTerms: () -> Unit,
    onAddUnit: () -> Unit
) {
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InstitutionHeaderCard(
                institution = state.institution!!,
                activeTerm = state.activeTerm,
                pendingUniversityName = state.pendingChanges.universityName,
                onUniversityNameChange = {
                    onAction(InstitutionDetailsAction.UpdateUniversityName(it))
                },
                onEditTerms = onEditTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
            )
        }

        item {
            QuickStatsGrid(
                institution = state.institution!!,
                isEditing = state.editMode != EditMode.VIEW,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateItem()
            )
        }

        item {
            AcademicTermsSection(
                activeTerm = state.activeTerm,
                isEditing = state.editMode != EditMode.VIEW,
                isExpanded = state.expandedSections.contains("terms"),
                onToggleExpand = { onAction(InstitutionDetailsAction.ToggleSection("terms")) },
                onEditTerms = onEditTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateItem()
            )
        }

        item {
            ProgrammesSection(
                programmes = state.institution!!.programmes,
                isEditing = state.editMode != EditMode.VIEW,
                isExpanded = state.expandedSections.contains("programmes"),
                onToggleExpand = { onAction(InstitutionDetailsAction.ToggleSection("programmes")) },
                onEditProgramme = onEditProgramme,
                onRemoveProgramme = { programmeId ->
                    onAction(InstitutionDetailsAction.RemoveProgramme(programmeId))
                },
                onAddProgramme = onAddProgramme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateItem()
            )
        }

        if (isLandscape || state.editMode != EditMode.VIEW) {
            item {
                UnitsOverviewSection(
                    programmes = state.institution!!.programmes,
                    isEditing = state.editMode != EditMode.VIEW,
                    isExpanded = state.expandedSections.contains("units"),
                    onToggleExpand = { onAction(InstitutionDetailsAction.ToggleSection("units")) },
                    onEditUnit = onEditUnit,
                    onAddUnit = onAddUnit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateItem()
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InstitutionHeaderCard(
    institution: UniversityWithProgrammesAndUnits,
    activeTerm: AcademicTermEntity?,
    pendingUniversityName: String?,
    onUniversityNameChange: (String) -> Unit,
    onEditTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = pendingUniversityName ?: institution.university.name

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
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Row {

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
    isEditing: Boolean,
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
    isEditing: Boolean,
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
            if (isEditing) {
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
    isEditing: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditProgramme: (ProgrammeWithUnits) -> Unit,
    onRemoveProgramme: (String) -> Unit,
    onAddProgramme: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExpandableSection(
        title = "Programmes",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        action = {
            if (isEditing) {
                IconButton(
                    onClick = onAddProgramme,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Programme",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            programmes.forEach { programmeWithUnits ->
                ProgrammeListItem(
                    programme = programmeWithUnits,
                    isEditing = isEditing,
                    onEdit = { onEditProgramme(programmeWithUnits) },
                    onRemove = { onRemoveProgramme(programmeWithUnits.programme.id) }
                )

                if (!isEditing && programmeWithUnits.units.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Units:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        programmeWithUnits.units.forEach { unit ->
                            UnitCompactView(unit = unit)
                        }
                    }
                }
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
    isEditing: Boolean,
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

                if (isEditing) {
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
            text = { Text("Are you sure you want to remove this programme? This action cannot be undone.") },
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
private fun UnitCompactView(
    unit: UnitEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = "${unit.code} - ${unit.name}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (unit.lectureDay != null || unit.lectureTime != null) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Has schedule",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
            }

            if (unit.lectureVenue != null) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Has venue",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun UnitsOverviewSection(
    programmes: List<ProgrammeWithUnits>,
    isEditing: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditUnit: (UnitEntity, ProgrammeEntity) -> Unit,
    onAddUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExpandableSection(
        title = "All Units",
        icon = Icons.Default.Book,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        action = {
            if (isEditing) {
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
            }
        },
        modifier = modifier
    ) {
        val allUnits = programmes.flatMap { programme ->
            programme.units.map { unit ->
                Pair(programme.programme, unit)
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
                allUnits.forEach { (programme, unit) ->
                    UnitListItem(
                        programme = programme,
                        unit = unit,
                        isEditing = isEditing,
                        onEdit = { onEditUnit(unit, programme) },
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
    isEditing: Boolean,
    onEdit: () -> Unit,
) {
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
                    FlowRow(
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

            if (isEditing) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
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