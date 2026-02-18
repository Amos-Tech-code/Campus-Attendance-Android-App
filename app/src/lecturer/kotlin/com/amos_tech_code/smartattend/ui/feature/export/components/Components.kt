package com.amos_tech_code.smartattend.ui.feature.export

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.domain.models.AttendanceSessionType
import com.amos_tech_code.smartattend.domain.models.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    state: ExportUiState,
    onDismiss: () -> Unit,
    onProgrammeSelected: (ProgrammeEntity) -> Unit,
    onUnitSelected: (UnitEntity) -> Unit,
    onWeekRangeSelected: (String) -> Unit,
    onSessionTypeSelected: (AttendanceSessionType?) -> Unit,
    onYearOfStudySelected: (Int) -> Unit,
    onSemesterSelected: (Int) -> Unit,
    onFormatSelected: (ExportFormat) -> Unit,
    onExport: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "New Export",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure your attendance export",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            HorizontalDivider()

            // Programme Selection
            ExportSection(
                title = "Programme",
                isRequired = true
            ) {
                ProgrammeSelector(
                    programmes = state.programmes,
                    selectedProgramme = state.selectedProgramme,
                    onProgrammeSelected = onProgrammeSelected
                )
            }

            // Unit Selection
            ExportSection(
                title = "Unit",
                isRequired = true
            ) {
                UnitSelector(
                    units = state.units,
                    selectedUnit = state.selectedUnit,
                    onUnitSelected = onUnitSelected,
                    enabled = state.selectedProgramme != null
                )
            }

            // Week Range
            ExportSection(
                title = "Week Range",
                isRequired = true
            ) {
                WeekRangeSelector(
                    selectedRange = state.weekRange,
                    onRangeSelected = onWeekRangeSelected
                )
            }

            // Session Type
            ExportSection(
                title = "Session Type",
                isRequired = false
            ) {
                SessionTypeChipGroup(
                    selectedType = state.sessionType,
                    onTypeSelected = onSessionTypeSelected
                )
            }

            // Year of Study & Semester
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExportSection(
                    modifier = Modifier.weight(1f),
                    title = "Year",
                    isRequired = true
                ) {
                    YearOfStudySelector(
                        selectedYear = state.yearOfStudy,
                        onYearSelected = onYearOfStudySelected
                    )
                }
                ExportSection(
                    modifier = Modifier.weight(1f),
                    title = "Semester",
                    isRequired = true
                ) {
                    SemesterSelector(
                        selectedSemester = state.semester,
                        onSemesterSelected = onSemesterSelected
                    )
                }
            }

            // Export Format
            ExportSection(
                title = "Format",
                isRequired = true
            ) {
                FormatSelector(
                    selectedFormat = state.selectedFormat,
                    onFormatSelected = onFormatSelected
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export Button
            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = state.canExport
            ) {
                if (state.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Export")
                }
            }
        }
    }
}

@Composable
fun ExportSection(
    modifier: Modifier = Modifier,
    title: String,
    isRequired: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            if (isRequired) {
                Text(
                    text = "*",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgrammeSelector(
    programmes: List<ProgrammeEntity>,
    selectedProgramme: ProgrammeEntity?,
    onProgrammeSelected: (ProgrammeEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedProgramme?.name ?: "",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            placeholder = { Text("Select programme") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            programmes.forEach { programme ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(programme.name)
                            Text(
                                text = "Year ${programme.yearOfStudy}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    onClick = {
                        onProgrammeSelected(programme)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SessionTypeChipGroup(
    selectedType: AttendanceSessionType?,
    onTypeSelected: (AttendanceSessionType?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("All") }
        )
        AttendanceSessionType.values().forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearOfStudySelector(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "Year $selectedYear",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..4).forEach { year ->
                DropdownMenuItem(
                    text = { Text("Year $year") },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterSelector(
    selectedSemester: Int,
    onSemesterSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "Semester $selectedSemester",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..3).forEach { semester ->
                DropdownMenuItem(
                    text = { Text("Semester ${'$'}semester") },
                    onClick = {
                        onSemesterSelected(semester)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FormatSelector(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FormatOption(
            modifier = Modifier.weight(1f),
            format = ExportFormat.PDF,
            isSelected = selectedFormat == ExportFormat.PDF,
            onClick = { onFormatSelected(ExportFormat.PDF) }
        )
        FormatOption(
            modifier = Modifier.weight(1f),
            format = ExportFormat.CSV,
            isSelected = selectedFormat == ExportFormat.CSV,
            onClick = { onFormatSelected(ExportFormat.CSV) }
        )
    }
}

@Composable
fun FormatOption(
    modifier: Modifier = Modifier,
    format: ExportFormat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        if (format == ExportFormat.PDF)
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val borderColor = if (isSelected) {
        if (format == ExportFormat.PDF)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (format == ExportFormat.PDF)
                    Icons.Outlined.PictureAsPdf
                else
                    Icons.Outlined.TableChart,
                contentDescription = null,
                tint = if (format == ExportFormat.PDF)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = format.name,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (format == ExportFormat.PDF)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekRangeSelector(
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxWeeks: Int = 13
) {
    var expanded by remember { mutableStateOf(false) }
    var customRangeStart by remember { mutableStateOf("") }
    var customRangeEnd by remember { mutableStateOf("") }
    var showCustomDialog by remember { mutableStateOf(false) }

    val predefinedRanges = listOf(
        "ALL" to "All Weeks",
        "1-7" to "Weeks 1-7",
        "8-13" to "Weeks 8-13",
        "1-13" to "Full Semester"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick select chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(predefinedRanges) { (range, label) ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            item {
                FilterChip(
                    selected = selectedRange.startsWith("custom"),
                    onClick = { showCustomDialog = true },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Custom")
                        }
                    }
                )
            }
        }

        // Custom range dialog
        if (showCustomDialog) {
            AlertDialog(
                onDismissRequest = { showCustomDialog = false },
                title = { Text("Custom Week Range") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customRangeStart,
                            onValueChange = { customRangeStart = it },
                            label = { Text("Start Week") },
                            placeholder = { Text("e.g., 3") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = customRangeStart.isNotBlank() &&
                                    (customRangeStart.toIntOrNull() ?: 0) !in 1..maxWeeks,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = customRangeEnd,
                            onValueChange = { customRangeEnd = it },
                            label = { Text("End Week") },
                            placeholder = { Text("e.g., 10") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = customRangeEnd.isNotBlank() &&
                                    (customRangeEnd.toIntOrNull() ?: 0) !in 1..maxWeeks,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Valid range: 1 - $maxWeeks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val start = customRangeStart.toIntOrNull()
                            val end = customRangeEnd.toIntOrNull()
                            if (start != null && end != null &&
                                start in 1..maxWeeks &&
                                end in 1..maxWeeks &&
                                start <= end) {
                                onRangeSelected("custom-$start-$end")
                                showCustomDialog = false
                            }
                        },
                        enabled = customRangeStart.isNotBlank() &&
                                customRangeEnd.isNotBlank() &&
                                (customRangeStart.toIntOrNull() ?: 0) in 1..maxWeeks &&
                                (customRangeEnd.toIntOrNull() ?: 0) in 1..maxWeeks &&
                                (customRangeStart.toIntOrNull() ?: 0) <= (customRangeEnd.toIntOrNull() ?: 0)
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Selected range display
        if (selectedRange.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when {
                                selectedRange == "ALL" -> "All Weeks Selected"
                                selectedRange.startsWith("custom") -> {
                                    val parts = selectedRange.split("-")
                                    if (parts.size == 3) {
                                        "Weeks ${parts[1]} - ${parts[2]}"
                                    } else {
                                        "Custom Range"
                                    }
                                }
                                else -> "Weeks $selectedRange"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (selectedRange.startsWith("custom")) {
                        IconButton(
                            onClick = {
                                customRangeStart = ""
                                customRangeEnd = ""
                                onRangeSelected("ALL")
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelector(
    units: List<UnitEntity>,
    selectedUnit: UnitEntity?,
    onUnitSelected: (UnitEntity) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredUnits = remember(units, searchQuery) {
        if (searchQuery.isBlank()) {
            units
        } else {
            units.filter {
                it.code.contains(searchQuery, ignoreCase = true) ||
                        it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Selected unit display or dropdown trigger
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = it }
        ) {
            OutlinedTextField(
                value = selectedUnit?.let { "${it.code} - ${it.name}" } ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    if (selectedUnit != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { /* Clear selection handled by parent */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                placeholder = {
                    Text(
                        if (enabled) "Select a unit" else "Select a programme first"
                    )
                },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
//                    disabledLeadingContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
//                    disabledTrailingContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )

            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Search bar
                DropdownMenuItem(
                    text = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search units...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    },
                    onClick = {}
                )

                if (filteredUnits.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No units found",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        onClick = {}
                    )
                } else {
                    filteredUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = unit.code,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = unit.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (unit.lectureDay != null || unit.lectureTime != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (unit.lectureDay != null) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = unit.lectureDay.take(3),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            if (unit.lectureTime != null) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            onClick = {
                                onUnitSelected(unit)
                                expanded = false
                                searchQuery = ""
                            }
                        )
                    }
                }
            }
        }

        // Unit details card (if selected)
        if (selectedUnit != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unit header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = selectedUnit.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = selectedUnit.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Schedule info
                    if (selectedUnit.lectureDay != null || selectedUnit.lectureTime != null || selectedUnit.lectureVenue != null) {
                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (selectedUnit.lectureDay != null && selectedUnit.lectureTime != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "${selectedUnit.lectureDay}, ${selectedUnit.lectureTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (selectedUnit.lectureVenue != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = selectedUnit.lectureVenue,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Semester info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Semester ${selectedUnit.semester}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
