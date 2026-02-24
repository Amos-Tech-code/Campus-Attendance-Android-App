package com.amos_tech_code.smartattend.ui.feature.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.response.UnitSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion
import com.amos_tech_code.smartattend.ui.components.LoadingDialog
import com.amos_tech_code.smartattend.ui.feature.setup.SetupStep.Companion.setUpStepTitle
import com.amos_tech_code.smartattend.ui.navigation.HomeRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversitySetupScreen(
    navController: NavController,
    viewModel: SetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Handle events
    ObserveAsEvents(viewModel.events) { event ->
            when (event) {
                SetupEvent.SetupComplete -> {
                    navController.navigate(HomeRoute) {
                        popUpTo(0)
                    }
                }
                is SetupEvent.ShowErrorMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is SetupEvent.ShowSuccessMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SetupTopBar(navController)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        SetupContent(
            uiState = uiState,
            onEvent = { viewModel.onIntent(it) },
            focusManager = focusManager,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .imePadding()
        )
    }

    if (uiState.isLoading) {
        LoadingDialog(message = "Setting up academic profile...")
    }

}

@Composable
private fun SetupContent(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Text(
            text = "Configure your institution, programmes, and teaching units to start managing attendance efficiently",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        // University Details Card
        UniversityCard(
            uiState = uiState,
            onEvent = onEvent,
        )

        // Programmes Card
        ProgrammesCard(
            programmes = uiState.programmes,
            onEvent = onEvent,
            showAddUnitForm = uiState.showAddUnitForm,
            addUnitState = uiState.addUnitState
        )

        // Action Button (only shown when not adding units)
        if (!uiState.showAddUnitForm) {
            SmartAttendPrimaryButton(
                text = "Complete Academic Setup",
                onClick = {
                    focusManager.clearFocus()
                    onEvent(SetupUiEvent.CompleteSetup)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                enabled = uiState.isSetupValid && !uiState.isLoading,
                size = SmartAttendButtonSize.Medium,
            )
        }
    }
}

@Composable
private fun UniversityCard(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
) {
    val academicYearSuggestions = remember { getCurrentAcademicYearOptions() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Institution Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // University Input with Suggestions
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SmartAttendTextField(
                    value = uiState.universityName,
                    onValueChange = { onEvent(SetupUiEvent.UniversityNameChanged(it)) },
                    label = "University Name *",
                    placeholder = "Search or enter your university",
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    isError = uiState.universityNameError != null,
                    errorMessage = uiState.universityNameError,
                    trailingIcon = {
                        if (uiState.isLoadingSuggestions) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                if (uiState.showUniversitySuggestions) {
                    UniversitySuggestionDropdown(
                        suggestions = uiState.universitySuggestions,
                        query = uiState.universityName,
                        onSuggestionSelected = { onEvent(SetupUiEvent.UniversitySelected(it)) },
                        onDismiss = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Academic Year with Suggestions
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Select Academic Year *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Show suggestion chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    academicYearSuggestions.forEach { year ->
                        FilterChip(
                            selected = uiState.academicYear == year,
                            onClick = {onEvent(SetupUiEvent.AcademicYearChanged(year)) },
                            label = { Text(year) },
                        )
                    }
                }

                // Manual input field
                SmartAttendTextField(
                    value = uiState.academicYear,
                    onValueChange = {onEvent(SetupUiEvent.AcademicYearChanged(it)) },
                    label = "Academic year",
                    placeholder = "e.g., 2026-2027",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.CalendarMonth, null)
                    }
                )
            }

            // Semester Selection
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select Semester *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..3).forEach { semester ->
                        FilterChip(
                            selected = uiState.selectedSemester == semester,
                            onClick = { onEvent(SetupUiEvent.SemesterChanged(semester)) },
                            label = { Text("Sem $semester") }
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun ProgrammesCard(
    programmes: List<ProgrammeUiState>,
    showAddUnitForm: Boolean,
    addUnitState: AddUnitState,
    onEvent: (SetupUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Add Button
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Teaching Programmes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Add programmes and their teaching units",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Add Programme Button
                if (programmes.isNotEmpty()) {
                    ElevatedButton(
                        onClick = { onEvent(SetupUiEvent.AddProgramme) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Programme",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Add Programme",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Programmes List
            if (programmes.isEmpty()) {
                EmptyProgrammesState(onAddProgramme = { onEvent(SetupUiEvent.AddProgramme) })
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    programmes.forEach { programme ->
                        ProgrammeCard(
                            programme = programme,
                            onProgrammeNameChanged = { name ->
                                onEvent(SetupUiEvent.ProgrammeNameChanged(programme.id, name))
                            },
                            onProgrammeSelected = { suggestion ->
                                onEvent(SetupUiEvent.ProgrammeSelected(programme.id, suggestion))
                            },
                            onProgrammeToggleExpanded = { onEvent(SetupUiEvent.ToggleProgrammeExpanded(programme.id)) },
                            onRemoveProgramme = { onEvent(SetupUiEvent.RemoveProgramme(programme.id)) },
                            onShowAddUnitForm = { onEvent(SetupUiEvent.ShowAddUnitForm(programme.id)) },
                            onRemoveUnit = { unitId ->
                                onEvent(SetupUiEvent.RemoveUnit(programme.id, unitId))
                            },
                            onDepartmentNameChanged = { name ->
                                onEvent(SetupUiEvent.DepartmentNameChanged(programme.id, name)) },
                            onDepartmentSelected = { suggestion ->
                                onEvent(SetupUiEvent.DepartmentSelected(programme.id, suggestion)) },
                            onYearOfStudyChanged = { year ->
                                onEvent(SetupUiEvent.OnYearOfStudyChanged(programme.id, year))
                            },
                            onStudentNoChanged = { count ->
                                onEvent(SetupUiEvent.NoOfExpectedStudentsChanged(programme.id, count))
                            }
                        )
                    }
                }
            }

            // Add Unit Form
            if (showAddUnitForm && programmes.isNotEmpty()) {
                AddUnitCard(
                    state = addUnitState,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun ProgrammeCard(
    programme: ProgrammeUiState,
    onProgrammeNameChanged: (String) -> Unit,
    onProgrammeSelected: (ProgrammeSuggestion) -> Unit,
    onDepartmentNameChanged: (String) -> Unit,
    onDepartmentSelected: (DepartmentSuggestion) -> Unit,
    onYearOfStudyChanged: (Int) -> Unit,
    onStudentNoChanged: (String) -> Unit,
    onProgrammeToggleExpanded: () -> Unit,
    onRemoveProgramme: () -> Unit,
    onShowAddUnitForm: () -> Unit,
    onRemoveUnit: (String) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Expand/Collapse
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Programme Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onProgrammeToggleExpanded,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (programme.isExpanded)
                            Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (programme.isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Programme Name with Suggestions
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmartAttendTextField(
                    value = programme.name,
                    onValueChange = {
                        onProgrammeNameChanged(it)
                    },
                    label = "Programme Name *",
                    placeholder = "Search or enter programme name",
                    modifier = Modifier.fillMaxWidth(),
                    isError = programme.nameError != null,
                    errorMessage = programme.nameError,
                    leadingIcon = { Icon(Icons.Default.School, null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                if (programme.showProgrammeSuggestions && programme.programmeSuggestions.isNotEmpty()) {
                    SuggestionDropdown(
                        suggestions = programme.programmeSuggestions,
                        onSuggestionSelected = onProgrammeSelected,
                        suggestionContent = { suggestion ->
                            Column {
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                suggestion.departmentName?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onDismiss = {  }
                    )
                }
            }

            // Department with Suggestions
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmartAttendTextField(
                    value = programme.departmentName,
                    onValueChange = {
                        onDepartmentNameChanged(it)
                    },
                    label = "Department *",
                    placeholder = "Search or enter department",
                    modifier = Modifier.fillMaxWidth(),
                    isError = programme.departmentNameError != null,
                    errorMessage = programme.departmentNameError,
                    leadingIcon = { Icon(Icons.Default.Business, null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                if (programme.showDepartmentSuggestions && programme.departmentSuggestions.isNotEmpty()) {
                    SuggestionDropdown(
                        suggestions = programme.departmentSuggestions,
                        onSuggestionSelected = onDepartmentSelected,
                        suggestionContent = { suggestion ->
                            Column {
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = suggestion.universityName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onDismiss = { }
                    )
                }
            }

            // Year of Study
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Year of Study *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                YearChipSelector(
                    selectedYear = programme.yearOfStudy,
                    onYearSelected = onYearOfStudyChanged
                )
            }
            SmartAttendTextField(
                value = programme.expectedStudentCount,
                onValueChange = { onStudentNoChanged(it) },
                label = "Expected no of students *",
                placeholder = "Enter number of students",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Expanded Units Section
            AnimatedVisibility(
                visible = programme.isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Units Header with Add Button
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Teaching Units",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        FilledTonalButton(
                            onClick = onShowAddUnitForm,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(Icons.Default.Add, "Add Unit", modifier = Modifier.size(16.dp))
                            Text("Add Unit", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }

                    // Units List
                    UnitsGrid(
                        units = programme.units,
                        onRemoveUnit = onRemoveUnit
                    )
                }
            }

            // Remove Programme Button
            OutlinedButton(
                onClick = onRemoveProgramme,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Programme",
                    modifier = Modifier.size(18.dp)
                )
                Text("Remove Programme", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearChipSelector(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        (1..5).forEach { year ->
            FilterChip(
                selected = selectedYear == year,
                onClick = { onYearSelected(year) },
                label = { Text("Year $year") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (selectedYear == year)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (selectedYear == year)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun UnitsGrid(
    units: List<UnitUiState>,
    onRemoveUnit: (String) -> Unit
) {
    if (units.isEmpty()) {
        EmptyUnitsState()
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            units.forEach { unit ->
                UnitCard(
                    unit = unit,
                    onRemove = { onRemoveUnit(unit.id) }
                )
            }
        }
    }
}

@Composable
private fun UnitCard(
    unit: UnitUiState,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Unit Code
                Text(
                    text = unit.code,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Unit Name
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Unit Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (unit.lectureDay.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${unit.lectureDay}, ${unit.lectureTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (unit.lectureVenue.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = unit.lectureVenue,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Remove Button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Unit",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUnitCard(
    state: AddUnitState,
    onEvent: (SetupUiEvent) -> Unit
) {
    var isLectureDetailsSectionExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Add New Unit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Unit Code with Suggestions
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmartAttendTextField(
                    value = state.unitCode,
                    onValueChange = {
                        onEvent(SetupUiEvent.UnitCodeChanged(it))
                    },
                    label = "Unit Code *",
                    placeholder = "e.g., CS401",
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.unitCodeError != null,
                    errorMessage = state.unitCodeError,
                    leadingIcon = { Icon(Icons.Default.Code, null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Characters)
                )
                // Unit Suggestions Dropdown
                if (state.showUnitSuggestions && state.unitSuggestions.isNotEmpty()) {
                    SuggestionDropdown(
                        suggestions = state.unitSuggestions,
                        onSuggestionSelected = { suggestion ->
                            onEvent(SetupUiEvent.UnitSelected(suggestion))
                        },
                        suggestionContent = { suggestion ->
                            Column {
                                Text(
                                    text = suggestion.code,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onDismiss = { }
                    )
                }
            }

            // Unit Name
            SmartAttendTextField(
                value = state.unitName,
                onValueChange = { onEvent(SetupUiEvent.UnitNameChanged(it)) },
                label = "Unit Name *",
                placeholder = "e.g., Mobile Application Development",
                modifier = Modifier.fillMaxWidth(),
                isError = state.unitNameError != null,
                errorMessage = state.unitNameError,
                leadingIcon = { Icon(Icons.Default.Title, null) }
            )

            // Lecture Details Section
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lecture Details (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SmartAttendWidthSpacer(8.dp)
                    IconButton(
                        onClick = {
                            isLectureDetailsSectionExpanded = !isLectureDetailsSectionExpanded
                        },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (isLectureDetailsSectionExpanded)
                                Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isLectureDetailsSectionExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Responsive layout for mobile
                AnimatedVisibility(
                    visible = isLectureDetailsSectionExpanded
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Day
                        SmartAttendTextField(
                            value = state.lectureDay,
                            onValueChange = { onEvent(SetupUiEvent.UnitLectureDayChanged(it)) },
                            label = "Day",
                            placeholder = "e.g., Monday",
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        // Time
                        SmartAttendTextField(
                            value = state.lectureTime,
                            onValueChange = { onEvent(SetupUiEvent.UnitLectureTimeChanged(it)) },
                            label = "Time",
                            placeholder = "e.g., 9:00 AM",
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Schedule, null) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        // Venue
                        SmartAttendTextField(
                            value = state.lectureVenue,
                            onValueChange = { onEvent(SetupUiEvent.UnitLectureVenueChanged(it)) },
                            label = "Venue",
                            placeholder = "e.g., Room 101",
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onEvent(SetupUiEvent.CancelAddUnit) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onEvent(SetupUiEvent.SaveUnit) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Text("Add Unit")
                }
            }
        }
    }
}

@Composable
private fun EmptyProgrammesState(onAddProgramme: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No Programmes Added",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Add your teaching programmes and their units to start tracking attendance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            FilledTonalButton(
                onClick = onAddProgramme,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Text("Add First Programme", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun EmptyUnitsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "No Units Added",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Click 'Add Unit' to add teaching units",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Academic Setup",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

private fun getCurrentAcademicYearOptions(): List<String> {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)

    return listOf(
        "$currentYear",                          // e.g., 2026
        "${currentYear-1}-$currentYear",        // e.g., 2025-2026
        "$currentYear-${currentYear+1}"         // e.g., 2026-2027
    )
}

 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversitySetupScreen(
    navController: NavController,
    viewModel: SetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = uiState.currentSetupStep,
        pageCount = { SetupStep.entries.size }
    )
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Sync pager with viewModel step
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != uiState.currentSetupStep) {
            viewModel.onIntent(SetupUiEvent.ChangeStep(pagerState.currentPage))
        }
    }

    // Handle events
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SetupEvent.SetupComplete -> {
                navController.navigate(HomeRoute) {
                    popUpTo(0)
                }
            }
            is SetupEvent.ShowErrorMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Dismiss"
                    )
                }
            }
            is SetupEvent.ShowSuccessMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SetupStepTopBar(
                currentStep = SetupStep.fromIndex(pagerState.currentPage),
                totalSteps = SetupStep.entries.size,
                onBack = {
                    if (pagerState.currentPage > 0) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Pager
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false, // Disable swipe navigation
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (SetupStep.fromIndex(page)) {
                    SetupStep.INSTITUTION -> InstitutionStep(
                        uiState = uiState,
                        onEvent = viewModel::onIntent,
                    )
                    SetupStep.PROGRAMMES -> ProgrammesStep(
                        uiState = uiState,
                        onEvent = viewModel::onIntent,
                    )
                    SetupStep.UNITS -> UnitsStep(
                        uiState = uiState,
                        onEvent = viewModel::onIntent,
                    )
                    SetupStep.REVIEW -> ReviewStep(
                        uiState = uiState,
                        onEvent = viewModel::onIntent,
                        onEditStep = { step ->
                            scope.launch {
                                pagerState.animateScrollToPage(step.ordinal)
                            }
                        }
                    )
                }
            }

            // Bottom Navigation
            SetupStepBottomBar(
                currentStep = SetupStep.fromIndex(pagerState.currentPage),
                isStepValid = when (pagerState.currentPage) {
                    0 -> uiState.isInstitutionStepValid
                    1 -> uiState.isProgrammesStepValid
                    2 -> uiState.isUnitsStepValid
                    3 -> uiState.isSetupValid
                    else -> false
                },
                isStepConfirmed = uiState.isSetupConfirmed,
                isLoading = uiState.isLoading,
                onNext = {
                    focusManager.clearFocus()
                    when (SetupStep.fromIndex(pagerState.currentPage)) {
                        SetupStep.REVIEW -> {
                            viewModel.onIntent(SetupUiEvent.CompleteSetup)
                        }
                        else -> {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                },
                onPrevious = {
                    if (pagerState.currentPage > 0) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (uiState.isLoading) {
                LoadingDialog(message = "Setting up your academic profile...")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupStepTopBar(
    currentStep: SetupStep,
    totalSteps: Int,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentStep.setUpStepTitle(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Step ${currentStep.ordinal + 1} of $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
private fun SetupStepBottomBar(
    currentStep: SetupStep,
    isStepValid: Boolean,
    isStepConfirmed: Boolean,
    isLoading: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Previous Button (hidden on first step)
            if (currentStep != SetupStep.INSTITUTION) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Previous",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Next/Complete Button
            Button(
                onClick = onNext,
                modifier = Modifier.weight(if (currentStep == SetupStep.INSTITUTION) 1f else 1.5f),
                shape = RoundedCornerShape(12.dp),
                enabled = when (currentStep) {
                    SetupStep.REVIEW -> isStepValid && isStepConfirmed && !isLoading
                    else -> isStepValid && !isLoading
                }
            ) {
                if (isLoading && currentStep == SetupStep.REVIEW) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    val buttonText = when {
                        currentStep == SetupStep.REVIEW -> "Complete Setup"
                        else -> "Continue"
                    }
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                if (currentStep != SetupStep.REVIEW) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Step 1: Institution Details
@Composable
private fun InstitutionStep(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()
    val academicYearSuggestions = remember { getCurrentAcademicYearOptions() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp) // Space for bottom bar
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Welcome to Academic Setup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Let's start by adding your institution details. This information will be used to personalize your attendance tracking experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // University Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Institution Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // University Input
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "University Name",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    UniversityInputField(
                        value = uiState.universityName,
                        onValueChange = { onEvent(SetupUiEvent.UniversityNameChanged(it)) },
                        suggestions = uiState.universitySuggestions,
                        showSuggestions = uiState.showUniversitySuggestions,
                        isLoading = uiState.isLoadingSuggestions,
                        onSuggestionSelected = { onEvent(SetupUiEvent.UniversitySelected(it)) },
                        error = uiState.universityNameError
                    )
                }

                // Academic Year
                AcademicYearSelector(
                    selectedYear = uiState.academicYear,
                    onYearSelected = { onEvent(SetupUiEvent.AcademicYearChanged(it)) },
                    suggestions = academicYearSuggestions,
                    error = null
                )

                // Semester Selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current Semester",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SemesterChips(
                        selectedSemester = uiState.selectedSemester,
                        onSemesterSelected = { onEvent(SetupUiEvent.SemesterChanged(it)) }
                    )
                }
            }
        }

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "You can search for your institution or type it manually",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
* Generates a list of academic year strings for the current and next two years.
* e.g., "2024-2025", "2025-2026", "2026-2027"
*/
private fun getCurrentAcademicYearOptions(): List<String> {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return List(3) { index ->
        val startYear = currentYear + index
        val endYear = startYear + 1
        "$startYear-$endYear"
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniversityInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<UniversitySuggestion>,
    showSuggestions: Boolean,
    isLoading: Boolean,
    onSuggestionSelected: (UniversitySuggestion) -> Unit,
    error: String?
) {
    ExposedDropdownMenuBox(
        expanded = showSuggestions && suggestions.isNotEmpty(),
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("University Name") },
            placeholder = { Text("Search or enter your university") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions)
                }
            },
            isError = error != null,
            supportingText = error?.let {
                { Text(it) }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        ExposedDropdownMenu(
            expanded = showSuggestions && suggestions.isNotEmpty(),
            onDismissRequest = { }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = suggestion.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    onClick = { onSuggestionSelected(suggestion) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AcademicYearSelector(
    selectedYear: String,
    onYearSelected: (String) -> Unit,
    suggestions: List<String>,
    error: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Academic Year",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Quick select chips
        SingleChoiceChipsRow(
            items = suggestions,
            selectedItem = selectedYear,
            onItemSelected = onYearSelected,
            chipLabel = { it }
        )

        // Manual input
        OutlinedTextField(
            value = selectedYear,
            onValueChange = onYearSelected,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Or enter manually") },
            placeholder = { Text("e.g., 2026-2027") },
            leadingIcon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}


// Step 2: Programmes
@Composable
private fun ProgrammesStep(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            text = "Add Your Programmes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add the programmes you're teaching this semester",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Add Programme Button
        Button(
            onClick = { onEvent(SetupUiEvent.AddProgramme) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(4.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Add New Programme",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Programmes List
        if (uiState.programmes.isEmpty()) {
            EmptyProgrammesIllustration(onAddProgramme = { onEvent(SetupUiEvent.AddProgramme) })
        } else {
            uiState.programmes.forEachIndexed { index, programme ->
                ProgrammeCompactCard(
                    programme = programme,
                    onEvent = onEvent,
                    isExpanded = programme.isExpanded,
                    onToggleExpand = {
                        onEvent(SetupUiEvent.ToggleProgrammeExpanded(programme.id))
                    }
                )
            }
        }

        // Quick Tips
        if (uiState.programmes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "You'll add teaching units for each programme in the next step",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgrammeCompactCard(
    programme: ProgrammeUiState,
    onEvent: (SetupUiEvent) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.animateContentSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = if (programme.name.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                        if (programme.units.isNotEmpty()) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(4.dp, (-4).dp),
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = programme.units.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = programme.name.ifBlank { "New Programme" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (programme.name.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (programme.name.isNotBlank())
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (programme.name.isNotBlank() && programme.departmentName.isNotBlank()) {
                            Text(
                                text = "${programme.departmentName} • Year ${programme.yearOfStudy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Delete button
                    IconButton(
                        onClick = { onEvent(SetupUiEvent.RemoveProgramme(programme.id)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Expand/collapse icon
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Programme Name Input
                    OutlinedTextField(
                        value = programme.name,
                        onValueChange = { onEvent(SetupUiEvent.ProgrammeNameChanged(programme.id, it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Programme Name") },
                        placeholder = { Text("e.g., Bachelor of Computer Science") },
                        isError = programme.nameError != null,
                        supportingText = programme.nameError?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Department Input
                    OutlinedTextField(
                        value = programme.departmentName,
                        onValueChange = { onEvent(SetupUiEvent.DepartmentNameChanged(programme.id, it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Department") },
                        placeholder = { Text("e.g., Computing Department") },
                        isError = programme.departmentNameError != null,
                        supportingText = programme.departmentNameError?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Year of Study
                    Column {
                        Text(
                            text = "Year of Study",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        YearChipRow(
                            selectedYear = programme.yearOfStudy,
                            onYearSelected = {
                                onEvent(
                                    SetupUiEvent.OnYearOfStudyChanged(
                                        programme.id,
                                        it
                                    )
                                )
                            }
                        )
                    }

                    // Expected Students
                    OutlinedTextField(
                        value = programme.expectedStudentCount,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                onEvent(SetupUiEvent.NoOfExpectedStudentsChanged(programme.id, it))
                            }
                        },
                        label = { Text("Students") },
                        placeholder = { Text("Number") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun YearChipRow(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        (1..5).forEach { year ->
            AssistChip(
                onClick = {
                    selectedYear == year
                    onYearSelected(year)
                          },
                label = { Text("Y$year") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedYear == year)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (selectedYear == year)
                    BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                else
                    null
            )
        }
    }
}

@Composable
private fun EmptyProgrammesIllustration(
    onAddProgramme: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Illustration
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Text(
            text = "No Programmes Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Add your first programme to start building your academic structure",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Button(
            onClick = onAddProgramme,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Add First Programme",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// Step 3: Units
@Composable
private fun UnitsStep(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            text = "Add Teaching Units",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add units for each programme. Units with complete details will be highlighted.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Programme Tabs
        if (uiState.programmes.isNotEmpty()) {
            var selectedProgrammeId by remember {
                mutableStateOf(uiState.programmes.firstOrNull()?.id)
            }

            // Programme Tabs
            ScrollableTabRow(
                selectedTabIndex = uiState.programmes.indexOfFirst { it.id == selectedProgrammeId }
                    .coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 0.dp
            ) {
                uiState.programmes.forEach { programme ->
                    Tab(
                        selected = programme.id == selectedProgrammeId,
                        onClick = { selectedProgrammeId = programme.id },
                        text = {
                            Box {
                                Text(
                                    text = programme.name.takeIf { it.isNotBlank() }
                                        ?: "Programme ${uiState.programmes.indexOf(programme) + 1}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (programme.units.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = programme.units.size.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
            }

            // Selected Programme Content
            val selectedProgramme = uiState.programmes.find { it.id == selectedProgrammeId }
            selectedProgramme?.let { programme ->
                // Units Grid
                if (programme.units.isEmpty()) {
                    EmptyUnitsIllustration(
                        onAddUnit = { onEvent(SetupUiEvent.ShowAddUnitForm(programme.id)) }
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Unit Cards
                        programme.units.forEach { unit ->
                            UnitCompactCard(
                                unit = unit,
                                onRemove = { onEvent(SetupUiEvent.RemoveUnit(programme.id, unit.id)) },
                                modifier = Modifier
                                    .width(160.dp)
                                    .weight(1f) // This makes cards take equal width in the row
                            )
                        }

                        // Add Unit Card
                        AddUnitCard(
                            onClick = { onEvent(SetupUiEvent.ShowAddUnitForm(programme.id)) },
                            modifier = Modifier
                                .width(160.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        }

        // Add Unit Form (Modal-like)
        if (uiState.showAddUnitForm) {
            AddUnitFormSheet(
                state = uiState.addUnitState,
                onEvent = onEvent,
                onDismiss = { onEvent(SetupUiEvent.CancelAddUnit) }
            )
        }
    }
}

@Composable
private fun UnitCompactCard(
    unit: UnitUiState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            1.dp,
            if (unit.lectureDay.isNotBlank() && unit.lectureVenue.isNotBlank())
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Code and Remove Button
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = unit.code,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Unit Name
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Lecture Day Indicator
                if (unit.lectureDay.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = unit.lectureDay.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Venue Indicator
                if (unit.lectureVenue.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = unit.lectureVenue,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Complete Indicator
            if (unit.lectureDay.isNotBlank() && unit.lectureVenue.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Complete",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddUnitCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Add Unit",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AddUnitFormSheet(
    state: AddUnitState,
    onEvent: (SetupUiEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with dismiss
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add New Unit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            // Unit Code
            UnitInputField(
                value = state.unitCode,
                onValueChange = { onEvent(SetupUiEvent.UnitCodeChanged(it)) },
                suggestions = state.unitSuggestions,
                showSuggestions = state.showUnitSuggestions,
                onSuggestionSelected = { onEvent(SetupUiEvent.UnitSelected(it)) },
                error = state.unitCodeError,
                label = "Unit Code",
                placeholder = "e.g., CS401",
                leadingIcon = Icons.Default.Code,
            )

            // Unit Name
            OutlinedTextField(
                value = state.unitName,
                onValueChange = { onEvent(SetupUiEvent.UnitNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Unit Name") },
                placeholder = { Text("e.g., Mobile Application Development") },
                isError = state.unitNameError != null,
                supportingText = state.unitNameError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            // Lecture Details (Optional)
            var showLectureDetails by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLectureDetails = !showLectureDetails }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Add lecture details (optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (showLectureDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = showLectureDetails) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.lectureDay,
                        onValueChange = { onEvent(SetupUiEvent.UnitLectureDayChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lecture Day") },
                        placeholder = { Text("e.g., Monday") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = state.lectureTime,
                        onValueChange = { onEvent(SetupUiEvent.UnitLectureTimeChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lecture Time") },
                        placeholder = { Text("e.g., 9:00 AM - 11:00 AM") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.lectureVenue,
                        onValueChange = { onEvent(SetupUiEvent.UnitLectureVenueChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Venue") },
                        placeholder = { Text("e.g., Room 101") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onEvent(SetupUiEvent.SaveUnit) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.unitCode.isNotBlank() && state.unitName.isNotBlank()
                ) {
                    Text("Add Unit")
                }
            }
        }
    }
}

@Composable
private fun EmptyUnitsIllustration(
    onAddUnit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "No Units Added",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Add your first teaching unit for this programme",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onAddUnit,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Add First Unit",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// Step 4: Review
@Composable
private fun ReviewStep(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
    onEditStep: (SetupStep) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp)
    ) {
        // Header
        Text(
            text = "Review Your Setup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please review your academic setup before completing",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Summary Cards
        ReviewSummaryCard(
            title = "Institution",
            icon = Icons.Default.School,
            content = {
                Text(
                    text = uiState.universityName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${uiState.academicYear} • Semester ${uiState.selectedSemester}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onEdit = { onEditStep(SetupStep.INSTITUTION) }
        )

        ReviewSummaryCard(
            title = "Programmes",
            icon = Icons.Default.MenuBook,
            content = {
                uiState.programmes.forEach { programme ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = programme.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${programme.departmentName} • Year ${programme.yearOfStudy} • ${programme.expectedStudentCount} students",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${programme.units.size} units",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    if (programme != uiState.programmes.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            },
            onEdit = { onEditStep(SetupStep.PROGRAMMES) }
        )

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val totalUnits = uiState.programmes.sumOf { it.units.size }
                val totalStudents = uiState.programmes.sumOf {
                    it.expectedStudentCount.toIntOrNull() ?: 0
                }

                ReviewStatItem(
                    value = uiState.programmes.size.toString(),
                    label = "Programmes"
                )
                ReviewStatItem(
                    value = totalUnits.toString(),
                    label = "Units"
                )
                ReviewStatItem(
                    value = totalStudents.toString(),
                    label = "Students"
                )
            }
        }

        // Confirmation Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .clickable { onEvent(SetupUiEvent.ToggleSetupConfirmation(!uiState.isSetupConfirmed)) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isSetupConfirmed,
                onCheckedChange = { onEvent(SetupUiEvent.ToggleSetupConfirmation(it)) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "I confirm that all information provided is accurate",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (!uiState.isSetupConfirmed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Please confirm the accuracy of your information to continue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

    }
}

@Composable
private fun ReviewSummaryCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Edit")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ReviewStatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun <T> SingleChoiceChipsRow(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    chipLabel: (T) -> String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            AssistChip(
                onClick = {
                    item == selectedItem
                    onItemSelected(item)
                },
                label = { Text(chipLabel(item)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (item == selectedItem)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (item == selectedItem)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = if (item == selectedItem)
                    BorderStroke(
                        color = MaterialTheme.colorScheme.primary,
                        width = 1.dp
                    )
                else
                    null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<UnitSuggestion>,
    showSuggestions: Boolean,
    onSuggestionSelected: (UnitSuggestion) -> Unit,
    error: String?,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector
) {
    ExposedDropdownMenuBox(
        expanded = showSuggestions && suggestions.isNotEmpty(),
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Characters
            )
        )

        ExposedDropdownMenu(
            expanded = showSuggestions && suggestions.isNotEmpty(),
            onDismissRequest = { }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = suggestion.code,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = suggestion.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = { onSuggestionSelected(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SemesterChips(
    selectedSemester: Int,
    onSemesterSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            (1..3).forEach { semester ->
                FilterChip(
                    selected = selectedSemester == semester,
                    onClick = { onSemesterSelected(semester) },
                    label = {
                        Text(
                            text = "Sem $semester",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversitySetupScreen(
    navController: NavController,
    viewModel: SetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Handle events
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SetupEvent.SetupComplete -> {
                navController.navigate(HomeRoute) {
                    popUpTo(0)
                }
            }
            is SetupEvent.ShowErrorMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Dismiss"
                    )
                }
            }
            is SetupEvent.ShowSuccessMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SetupTopBar(
                navController = navController,
                canComplete = uiState.isSetupValid,
                onComplete = {
                    focusManager.clearFocus()
                    viewModel.onIntent(SetupUiEvent.CompleteSetup)
                },
                isLoading = uiState.isLoading
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SetupContent(
                uiState = uiState,
                onEvent = viewModel::onIntent,
                focusManager = focusManager,
                scrollState = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            )

            if (uiState.isLoading) {
                LoadingOverlay(message = "Setting up your academic profile...")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupTopBar(
    navController: NavController,
    canComplete: Boolean,
    onComplete: () -> Unit,
    isLoading: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Academic Setup",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            TextButton(
                onClick = onComplete,
                enabled = canComplete && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Finish",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun SetupContent(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit,
    focusManager: FocusManager,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Progress Indicator
        SetupProgressIndicator(
            currentStep = when {
                uiState.universityName.isBlank() -> 1
                uiState.programmes.isEmpty() -> 2
                uiState.programmes.all { it.units.isNotEmpty() } -> 4
                uiState.programmes.isNotEmpty() -> 3
                else -> 1
            },
            totalSteps = 4
        )

        // Welcome Message
        SetupWelcomeHeader()

        // University Details Section
        UniversitySetupSection(
            uiState = uiState,
            onEvent = onEvent
        )

        // Programmes Section
        ProgrammesSetupSection(
            uiState = uiState,
            onEvent = onEvent
        )

        // Quick Stats (if any programmes added)
        if (uiState.programmes.isNotEmpty()) {
            SetupQuickStats(programmes = uiState.programmes)
        }

        // Bottom Spacing
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SetupProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Setup Progress",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            progress = currentStep.toFloat() / totalSteps,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
private fun SetupWelcomeHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Welcome to Academic Setup!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Configure your institution details, add programmes, and set up teaching units to get started with attendance tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun UniversitySetupSection(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val academicYearSuggestions = remember { getCurrentAcademicYearOptions() }

    SetupExpandableCard(
        title = "Institution Details",
        icon = Icons.Default.School,
        isExpanded = isExpanded,
        onToggle = { isExpanded = !isExpanded },
        isComplete = uiState.universityName.isNotBlank() &&
                uiState.academicYear.isNotBlank() &&
                uiState.selectedSemester != 0
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // University Field
            UniversityInputField(
                value = uiState.universityName,
                onValueChange = { onEvent(SetupUiEvent.UniversityNameChanged(it)) },
                suggestions = uiState.universitySuggestions,
                showSuggestions = uiState.showUniversitySuggestions,
                isLoading = uiState.isLoadingSuggestions,
                onSuggestionSelected = { onEvent(SetupUiEvent.UniversitySelected(it)) },
                error = uiState.universityNameError
            )

            // Academic Year
            AcademicYearSelector(
                selectedYear = uiState.academicYear,
                onYearSelected = { onEvent(SetupUiEvent.AcademicYearChanged(it)) },
                suggestions = academicYearSuggestions,
                error = null
            )

            // Semester Selection
            SemesterSelector(
                selectedSemester = uiState.selectedSemester,
                onSemesterSelected = { onEvent(SetupUiEvent.SemesterChanged(it)) }
            )
        }
    }
}

/**
 * Generates a list of academic year strings for the current and next two years.
 * e.g., "2024-2025", "2025-2026", "2026-2027"
 */
private fun getCurrentAcademicYearOptions(): List<String> {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return List(3) { index ->
        val startYear = currentYear + index
        val endYear = startYear + 1
        "$startYear-$endYear"
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniversityInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<UniversitySuggestion>,
    showSuggestions: Boolean,
    isLoading: Boolean,
    onSuggestionSelected: (UniversitySuggestion) -> Unit,
    error: String?
) {
    ExposedDropdownMenuBox(
        expanded = showSuggestions && suggestions.isNotEmpty(),
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("University Name") },
            placeholder = { Text("Search or enter your university") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions)
                }
            },
            isError = error != null,
            supportingText = error?.let {
                { Text(it) }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        ExposedDropdownMenu(
            expanded = showSuggestions && suggestions.isNotEmpty(),
            onDismissRequest = { }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = suggestion.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    onClick = { onSuggestionSelected(suggestion) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AcademicYearSelector(
    selectedYear: String,
    onYearSelected: (String) -> Unit,
    suggestions: List<String>,
    error: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Academic Year",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Quick select chips
        SingleChoiceChipsRow(
            items = suggestions,
            selectedItem = selectedYear,
            onItemSelected = onYearSelected,
            chipLabel = { it }
        )

        // Manual input
        OutlinedTextField(
            value = selectedYear,
            onValueChange = onYearSelected,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Or enter manually") },
            placeholder = { Text("e.g., 2026-2027") },
            leadingIcon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun <T> SingleChoiceChipsRow(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    chipLabel: (T) -> String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            AssistChip(
                onClick = {
                    item == selectedItem
                    onItemSelected(item)
                          },
                label = { Text(chipLabel(item)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (item == selectedItem)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (item == selectedItem)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = if (item == selectedItem)
                    BorderStroke(
                        color = MaterialTheme.colorScheme.primary,
                        width = 1.dp
                    )
                else
                    null
            )
        }
    }
}

@Composable
private fun SemesterSelector(
    selectedSemester: Int,
    onSemesterSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Semester",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            (1..3).forEach { semester ->
                FilterChip(
                    selected = selectedSemester == semester,
                    onClick = { onSemesterSelected(semester) },
                    label = {
                        Text(
                            text = "Semester $semester",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun ProgrammesSetupSection(
    uiState: SetupUiState,
    onEvent: (SetupUiEvent) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    SetupExpandableCard(
        title = "Teaching Programmes",
        icon = Icons.Default.MenuBook,
        isExpanded = isExpanded,
        onToggle = { isExpanded = !isExpanded },
        isComplete = uiState.programmes.isNotEmpty() &&
                uiState.programmes.all { it.units.isNotEmpty() },
        action = {
            if (uiState.programmes.isNotEmpty()) {
                FilledTonalIconButton(
                    onClick = { onEvent(SetupUiEvent.AddProgramme) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Programme",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) {
        if (uiState.programmes.isEmpty()) {
            EmptyProgrammesState(
                onAddProgramme = { onEvent(SetupUiEvent.AddProgramme) }
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Programmes List
                uiState.programmes.forEachIndexed { index, programme ->
                    ProgrammeSetupItem(
                        programme = programme,
                        onEvent = onEvent,
                        isFirst = index == 0,
                        isLast = index == uiState.programmes.lastIndex
                    )
                }

                // Add Unit Form (if visible)
                if (uiState.showAddUnitForm) {
                    AddUnitForm(
                        state = uiState.addUnitState,
                        onEvent = onEvent,
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgrammeSetupItem(
    programme: ProgrammeUiState,
    onEvent: (SetupUiEvent) -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    var isExpanded by remember { mutableStateOf(programme.isExpanded) }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 16.dp else 0.dp,
            topEnd = if (isFirst) 16.dp else 0.dp,
            bottomStart = if (isLast) 16.dp else 0.dp,
            bottomEnd = if (isLast) 16.dp else 0.dp
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Programme Header
            ProgrammeHeader(
                programme = programme,
                isExpanded = isExpanded,
                onToggleExpanded = {
                    isExpanded = !isExpanded
                    onEvent(SetupUiEvent.ToggleProgrammeExpanded(programme.id))
                },
                onRemove = { onEvent(SetupUiEvent.RemoveProgramme(programme.id)) }
            )

            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                ProgrammeDetails(
                    programme = programme,
                    onEvent = onEvent,
                    focusManager = focusManager
                )
            }
        }
    }
}

@Composable
private fun ProgrammeHeader(
    programme: ProgrammeUiState,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Programme Icon with Status
            Box {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = if (programme.name.isNotBlank() && programme.units.isNotEmpty())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )

                if (programme.units.isNotEmpty()) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(4.dp, (-4).dp)
                    ) {
                        Text(
                            text = programme.units.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = programme.name.ifBlank { "New Programme" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (programme.name.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (programme.name.isNotBlank())
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (programme.units.isNotEmpty()) {
                    Text(
                        text = "${programme.units.size} ${if (programme.units.size == 1) "unit" else "units"} • Year ${programme.yearOfStudy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row {
            // Remove Button (only if not the only programme)
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Programme",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgrammeDetails(
    programme: ProgrammeUiState,
    onEvent: (SetupUiEvent) -> Unit,
    focusManager: FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Programme Name with Suggestions
        ProgrammeInputField(
            value = programme.name,
            onValueChange = { onEvent(SetupUiEvent.ProgrammeNameChanged(programme.id, it)) },
            suggestions = programme.programmeSuggestions,
            showSuggestions = programme.showProgrammeSuggestions,
            onSuggestionSelected = { suggestion ->
                // Perform a smart cast to the expected type
                if (suggestion is ProgrammeSuggestion) {
                    onEvent(SetupUiEvent.ProgrammeSelected(programme.id, suggestion))
                }
            },
            error = programme.nameError,
            label = "Programme Name",
            placeholder = "e.g., Bachelor of Computer Science"
        )
        // Department with Suggestions
        if (programme.selectedProgrammeId == null) {
            ProgrammeInputField(
                value = programme.departmentName,
                onValueChange = { onEvent(SetupUiEvent.DepartmentNameChanged(programme.id, it)) },
                suggestions = programme.departmentSuggestions,
                showSuggestions = programme.showDepartmentSuggestions,
                onSuggestionSelected = { suggestion ->
                    // Perform a smart cast to the expected type
                    if (suggestion is DepartmentSuggestion) {
                        onEvent(SetupUiEvent.DepartmentSelected(programme.id, suggestion))
                    }
                },
                error = programme.departmentNameError,
                label = "Department",
                placeholder = "e.g., Computing Department",
                leadingIcon = Icons.Default.Business
            )
        }


        // Year of Study and Students Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Year of Study
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Year of Study",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                YearSelector(
                    selectedYear = programme.yearOfStudy,
                    onYearSelected = { onEvent(SetupUiEvent.OnYearOfStudyChanged(programme.id, it)) }
                )
            }

            // Expected Students
            OutlinedTextField(
                value = programme.expectedStudentCount,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        onEvent(SetupUiEvent.NoOfExpectedStudentsChanged(programme.id, it))
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Students") },
                placeholder = { Text("e.g., 50") },
                leadingIcon = {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Units Section
        UnitsSection(
            programmeId = programme.id,
            units = programme.units,
            onAddUnit = { onEvent(SetupUiEvent.ShowAddUnitForm(programme.id)) },
            onRemoveUnit = { unitId -> onEvent(SetupUiEvent.RemoveUnit(programme.id, unitId)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgrammeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<Any>,
    showSuggestions: Boolean,
    onSuggestionSelected: (Any) -> Unit,
    error: String?,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector = Icons.Default.Search
) {
    ExposedDropdownMenuBox(
        expanded = showSuggestions && suggestions.isNotEmpty(),
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = showSuggestions && suggestions.isNotEmpty(),
            onDismissRequest = { /* Consider adding onDismissRequest = { onExpandedChange(false) } */ }
        ) {
            suggestions.forEach { suggestion ->
                // Use a 'when' statement for type checking
                when (suggestion) {
                    is ProgrammeSuggestion -> {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = suggestion.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    suggestion.departmentName?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = { onSuggestionSelected(suggestion) }
                        )
                    }

                    is DepartmentSuggestion -> {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = suggestion.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = suggestion.universityName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { onSuggestionSelected(suggestion) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearSelector(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        (1..5).forEach { year ->
            AssistChip(
                onClick = { onYearSelected(year) },
                label = { Text("Year $year") },
                //selected = selectedYear == year,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedYear == year)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (selectedYear == year)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun UnitsSection(
    programmeId: String,
    units: List<UnitUiState>,
    onAddUnit: () -> Unit,
    onRemoveUnit: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with Add Button
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Teaching Units",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            FilledTonalButton(
                onClick = onAddUnit,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Add Unit",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Units Grid
        if (units.isEmpty()) {
            EmptyUnitsState()
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(units) { unit ->
                    UnitCard(
                        unit = unit,
                        onRemove = { onRemoveUnit(unit.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitCard(
    unit: UnitUiState,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with Code and Remove
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = unit.code,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Unit",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Unit Name
            Text(
                text = unit.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Lecture Details (if available)
            if (unit.lectureDay.isNotBlank() || unit.lectureTime.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = buildString {
                            if (unit.lectureDay.isNotBlank()) append(unit.lectureDay)
                            if (unit.lectureTime.isNotBlank()) {
                                if (isNotBlank()) append(", ")
                                append(unit.lectureTime)
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (unit.lectureVenue.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = unit.lectureVenue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AddUnitForm(
    state: AddUnitState,
    onEvent: (SetupUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLectureDetails by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Add New Unit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Unit Code with Suggestions
            UnitInputField(
                value = state.unitCode,
                onValueChange = { onEvent(SetupUiEvent.UnitCodeChanged(it)) },
                suggestions = state.unitSuggestions,
                showSuggestions = state.showUnitSuggestions,
                onSuggestionSelected = { onEvent(SetupUiEvent.UnitSelected(it)) },
                error = state.unitCodeError,
                label = "Unit Code",
                placeholder = "e.g., CS401",
                leadingIcon = Icons.Default.Code
            )

            // Unit Name
            OutlinedTextField(
                value = state.unitName,
                onValueChange = { onEvent(SetupUiEvent.UnitNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Unit Name") },
                placeholder = { Text("e.g., Mobile Application Development") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Title,
                        contentDescription = null
                    )
                },
                isError = state.unitNameError != null,
                supportingText = state.unitNameError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Toggle Lecture Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLectureDetails = !showLectureDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Add lecture details (optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (showLectureDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Lecture Details (Optional)
            AnimatedVisibility(visible = showLectureDetails) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.lectureDay,
                        onValueChange = { onEvent(SetupUiEvent.UnitLectureDayChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lecture Day") },
                        placeholder = { Text("e.g., Monday") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.lectureTime,
                        onValueChange = { onEvent(SetupUiEvent.UnitLectureTimeChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lecture Time") },
                        placeholder = { Text("e.g., 9:00 AM - 11:00 AM") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.lectureVenue,
                        onValueChange = { onEvent(SetupUiEvent.UnitLectureVenueChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Venue") },
                        placeholder = { Text("e.g., Room 101, Block A") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { onEvent(SetupUiEvent.CancelAddUnit) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onEvent(SetupUiEvent.SaveUnit) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    enabled = state.unitCode.isNotBlank() && state.unitName.isNotBlank()
                ) {
                    Text("Add Unit")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<UnitSuggestion>,
    showSuggestions: Boolean,
    onSuggestionSelected: (UnitSuggestion) -> Unit,
    error: String?,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector
) {
    ExposedDropdownMenuBox(
        expanded = showSuggestions && suggestions.isNotEmpty(),
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Characters
            )
        )

        ExposedDropdownMenu(
            expanded = showSuggestions && suggestions.isNotEmpty(),
            onDismissRequest = { }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = suggestion.code,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = suggestion.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = { onSuggestionSelected(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SetupExpandableCard(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isComplete: Boolean,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isComplete)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )

                        if (isComplete) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Complete",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(2.dp, 2.dp)
                            )
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isComplete)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    action?.invoke()

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun EmptyProgrammesState(
    onAddProgramme: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "No Programmes Added",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Add your teaching programmes to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onAddProgramme,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Add Programme",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun EmptyUnitsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = "No units yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Tap 'Add Unit' to add teaching units",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SetupQuickStats(programmes: List<ProgrammeUiState>) {
    val totalUnits = programmes.sumOf { it.units.size }
    val totalStudents = programmes.sumOf { it.expectedStudentCount.toIntOrNull() ?: 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickStatItem(
                value = programmes.size.toString(),
                label = "Programmes",
                icon = Icons.Default.School
            )

            VerticalDivider(
                modifier = Modifier.height(40.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )

            QuickStatItem(
                value = totalUnits.toString(),
                label = "Units",
                icon = Icons.Default.Book
            )

            VerticalDivider(
                modifier = Modifier.height(40.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )

            QuickStatItem(
                value = totalStudents.toString(),
                label = "Students",
                icon = Icons.Default.People
            )
        }
    }
}

@Composable
private fun QuickStatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

 */