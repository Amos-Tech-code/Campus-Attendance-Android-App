package com.amos_tech_code.smartattend.ui.feature.setup

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
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
import com.amos_tech_code.smartattend.ui.components.LoadingDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendButtonSize
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendSecondaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.navigation.HomeRoute
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversitySetupScreen(
    navController: NavController,
    viewModel: SetupViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val state by viewModel.setupState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            SetUpEvents.SetupComplete -> {
                navController.navigate(HomeRoute)
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