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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            icon = Icons.AutoMirrored.Filled.MenuBook,
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

/**
 * Generates a list of academic year strings for the previous, current and next two years.
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