package com.amos_tech_code.smartattend.ui.feature.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.domain.response.DepartmentSuggestion
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.ui.components.LoadingDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonSize
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.components.SmartAttendWidthSpacer
import com.amos_tech_code.smartattend.ui.components.SuggestionDropdown
import com.amos_tech_code.smartattend.ui.components.UniversitySuggestionDropdown
import com.amos_tech_code.smartattend.ui.navigation.HomeRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

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

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversitySetupScreen(
    navController: NavController,
    viewModel: SetupViewModel = koinViewModel()
) {

    val focusManager = LocalFocusManager.current
    val state by viewModel.setupState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            SetUpEvents.SetupComplete -> {
                navController.navigate(HomeRoute)
                Toast.makeText(navController.context, "Details Uploaded Successfully", Toast.LENGTH_SHORT).show()
            }
            is SetUpEvents.ShowErrorMessage -> {
                scope.launch {
                    snackBarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Academic Setup",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configure Your Academic Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set up your institution, programmes, and teaching units to start managing attendance",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Institution Details Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Institution Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // University Name
                    SmartAttendTextField(
                        value = state.universityName,
                        onValueChange = { viewModel.onUniversityNameChange(it) },
                        label = "University Name *",
                        placeholder = "Enter your university name",
                        leadingIcon = { Icon(Icons.Default.School, "University") },
                        isError = state.universityNameError != null,
                        errorMessage = state.universityNameError
                    )
                }
            }

            // Programmes & Units Setup Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Programmes & Units",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        ElevatedAssistChip(
                            onClick = { viewModel.onShowAddProgrammeForm() },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Programme",
                                )
                            },
                            colors = AssistChipDefaults.elevatedAssistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            elevation = AssistChipDefaults.elevatedAssistChipElevation(2.dp)
                        )
                    }

                    if (state.programmes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Workspaces,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No Programmes Added",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text =  "Add programmes and units you teach to start tracking attendance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.programmes.forEach { programme ->
                                ProgrammeItem(
                                    programme = programme,
                                    onEdit = { viewModel.onEditProgramme(programme.id) },
                                    onDeleteProgramme = { viewModel.onDeleteProgramme(programme.id) },
                                    onDeleteUnit = { viewModel.onDeleteUnit(programme.id, it) },
                                    onAddUnit = { viewModel.onShowAddUnitForm(programme.id) }
                                )
                            }
                        }
                    }

                    // Add Programme Form
                    if (state.showAddProgrammeForm) {
                        AddProgrammeForm(
                            state = state.addProgrammeState,
                            onProgrammeNameChange = { viewModel.onProgrammeNameChange(it) },
                            onDepartmentChange = { viewModel.onDepartmentChange(it) },
                            onYearOfStudyChange = { viewModel.onYearOfStudyChange(it) },
                            onSave = { viewModel.onSaveProgramme() },
                            onCancel = { viewModel.onCancelAddProgramme() }
                        )
                    }

                    // Add Unit Form
                    if (state.showAddUnitForm) {
                        AddUnitForm(
                            state = state.addUnitState,
                            programmes = state.programmes,
                            onUnitNameChange = { viewModel.onUnitNameChange(it) },
                            onUnitCodeChange = { viewModel.onUnitCodeChange(it) },
                            onSelectedProgrammeChange = { viewModel.onSelectedProgrammeChange(it) },
                            onSave = { viewModel.onSaveUnit() },
                            onCancel = { viewModel.onCancelAddUnit() }
                        )
                    }
                }
            }

            // Complete Setup Button
            SmartAttendPrimaryButton(
                text = "Complete Academic Setup",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onCompleteSetup()
                },
                modifier = Modifier.fillMaxWidth(),
                //isLoading = state.isLoading,
                enabled = state.isSetupValid && !state.isLoading
            )
        }
    }

    if (state.isLoading) {
        LoadingDialog(message = "Setting up academic profile...")
    }
}


@Composable
private fun ProgrammeItem(
    programme: Programme,
    onEdit: () -> Unit,
    onDeleteProgramme: () -> Unit,
    onDeleteUnit: (String) -> Unit,
    onAddUnit: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.shadow(
            elevation = 2.dp,
            shape = MaterialTheme.shapes.medium,
            ambientColor = MaterialTheme.colorScheme.background,
            spotColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Programme Header - Improved layout
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row: Programme name and actions
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top // Align to top to handle long text
                ) {
                    Text(
                        text = programme.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onAddUnit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                "Add Unit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                "Edit Programme",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteProgramme,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete Programme",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Second row: Programme details
                Text(
                    text = "${programme.department} • Year ${programme.yearOfStudy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Units List
            if (programme.units.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Teaching Units:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        programme.units.forEach { unit ->
                            UnitChip(
                                unit = unit,
                                onDelete = { onDeleteUnit(unit.id) }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No units added yet. Click + to add units.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitChip(
    unit: TeachingUnit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = "Unit",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = unit.code,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Unit",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddProgrammeForm(
    state: AddProgrammeState,
    onProgrammeNameChange: (String) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onYearOfStudyChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Add New Programme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        SmartAttendTextField(
            value = state.programmeName,
            onValueChange = onProgrammeNameChange,
            label = "Programme Name *",
            placeholder = "e.g., Bachelor of Science in Computer Science",
            isError = state.programmeNameError != null,
            errorMessage = state.programmeNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        SmartAttendTextField(
            value = state.department,
            onValueChange = onDepartmentChange,
            label = "Department *",
            placeholder = "e.g., IT Department",
            isError = state.departmentError != null,
            errorMessage = state.departmentError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        SmartAttendTextField(
            value = state.yearOfStudy,
            onValueChange = onYearOfStudyChange,
            label = "Year of Study *",
            placeholder = "e.g., 1, 2, 3, 4",
            isError = state.yearOfStudyError != null,
            errorMessage = state.yearOfStudyError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartAttendSecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            SmartAttendPrimaryButton(
                text = "Add",
                onClick = onSave,
                size = SmartAttendButtonSize.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUnitForm(
    state: AddUnitState,
    programmes: List<Programme>,
    onUnitNameChange: (String) -> Unit,
    onUnitCodeChange: (String) -> Unit,
    onSelectedProgrammeChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Add Teaching Unit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        // Programme Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            SmartAttendTextField(
                value = state.selectedProgrammeName,
                onValueChange = { },
                label = "Select Programme *",
                placeholder = "Choose a programme",
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = state.selectedProgrammeError != null,
                errorMessage = state.selectedProgrammeError
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                programmes.forEach { programme ->
                    DropdownMenuItem(
                        text = {
                            Text("${programme.name} (Year ${programme.yearOfStudy})")
                        },
                        onClick = {
                            onSelectedProgrammeChange(programme.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        SmartAttendTextField(
            value = state.unitName,
            onValueChange = onUnitNameChange,
            label = "Unit Name *",
            placeholder = "e.g., Mobile Application Development",
            isError = state.unitNameError != null,
            errorMessage = state.unitNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        SmartAttendTextField(
            value = state.unitCode,
            onValueChange = onUnitCodeChange,
            label = "Unit Code *",
            placeholder = "e.g., CS401",
            isError = state.unitCodeError != null,
            errorMessage = state.unitCodeError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Characters)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartAttendSecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            SmartAttendPrimaryButton(
                text = "Add Unit",
                onClick = onSave,
                size = SmartAttendButtonSize.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

 */