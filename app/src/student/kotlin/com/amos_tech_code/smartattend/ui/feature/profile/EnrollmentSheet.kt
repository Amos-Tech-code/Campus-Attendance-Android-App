package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentBottomSheet(
    state: ProfileScreenState,
    onDismiss: () -> Unit,
    onUniversitySearch: (String) -> Unit,
    onProgrammeSearch: (String, String) -> Unit,
    onEnroll: (String, String, String, String) -> Unit
) {
    var selectedUniversity by remember { mutableStateOf<UniversitySuggestion?>(null) }
    var selectedProgramme by remember { mutableStateOf<ProgrammeSuggestion?>(null) }
    var universityQuery by remember { mutableStateOf("") }
    var programmeQuery by remember { mutableStateOf("") }
    var showUniversityDropdown by remember { mutableStateOf(false) }
    var showProgrammeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(universityQuery) {
        if (universityQuery.length >= 2) {
            onUniversitySearch(universityQuery)
            showUniversityDropdown = true
        } else {
            showUniversityDropdown = false
        }
    }

    LaunchedEffect(programmeQuery) {
        if (selectedUniversity != null && programmeQuery.length >= 2) {
            onProgrammeSearch(selectedUniversity!!.id, programmeQuery)
            showProgrammeDropdown = true
        } else {
            showProgrammeDropdown = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enroll in Program",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // University Selection
                Column {
                    Text(
                        text = "Select University",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box {
                        OutlinedTextField(
                            value = universityQuery,
                            onValueChange = {
                                universityQuery = it
                                selectedUniversity = null
                                selectedProgramme = null
                                programmeQuery = ""
                            },
                            label = { Text("Search university...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.School, contentDescription = null)
                            },
                            trailingIcon = {
                                if (selectedUniversity != null) {
                                    IconButton(onClick = {
                                        selectedUniversity = null
                                        universityQuery = ""
                                        selectedProgramme = null
                                        programmeQuery = ""
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )

                        if (showUniversityDropdown && state.universitySuggestions.isNotEmpty()) {
                            DropdownMenu(
                                expanded = showUniversityDropdown,
                                onDismissRequest = { showUniversityDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                state.universitySuggestions.forEach { university ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(university.name)
                                                Text(
                                                    text = "Match: ${university.matchType.replaceFirstChar { it.uppercase() }}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedUniversity = university
                                            universityQuery = university.name
                                            showUniversityDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Programme Selection
                Column {
                    Text(
                        text = "Select Programme",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box {
                        OutlinedTextField(
                            value = programmeQuery,
                            onValueChange = {
                                programmeQuery = it
                                selectedProgramme = null
                            },
                            label = { Text("Search programme...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Book, contentDescription = null)
                            },
                            trailingIcon = {
                                if (selectedProgramme != null) {
                                    IconButton(onClick = {
                                        selectedProgramme = null
                                        programmeQuery = ""
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            enabled = selectedUniversity != null
                        )

                        if (showProgrammeDropdown && state.programmeSuggestions.isNotEmpty()) {
                            DropdownMenu(
                                expanded = showProgrammeDropdown,
                                onDismissRequest = { showProgrammeDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                state.programmeSuggestions.forEach { programme ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(programme.name)
                                                if (programme.departmentName != null) {
                                                    Text(
                                                        text = programme.departmentName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedProgramme = programme
                                            programmeQuery = programme.name
                                            showProgrammeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedUniversity != null && !showProgrammeDropdown) {
                        Text(
                            text = "Start typing to search programmes in ${selectedUniversity!!.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Selected Preview
                if (selectedUniversity != null && selectedProgramme != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Enrollment Preview",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Divider()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = selectedUniversity!!.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = selectedProgramme!!.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (selectedUniversity != null && selectedProgramme != null) {
                                onEnroll(
                                    selectedUniversity!!.id,
                                    selectedUniversity!!.name,
                                    selectedProgramme!!.id,
                                    selectedProgramme!!.name
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedUniversity != null && selectedProgramme != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Enroll Now")
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentInfoBottomSheet(
    enrollment: EnrollmentUiState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enrollment Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            color = if (enrollment.isActive)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (enrollment.isActive) "ACTIVE" else "INACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (enrollment.isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Details Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailRow(
                    label = "Enrollment ID",
                    value = enrollment.enrollmentId,
                    icon = Icons.Default.Badge
                )

                DetailRow(
                    label = "University",
                    value = enrollment.university,
                    icon = Icons.Default.School
                )

                DetailRow(
                    label = "Programme",
                    value = enrollment.programme,
                    icon = Icons.Default.Book
                )

                DetailRow(
                    label = "Year of Study",
                    value = "Year ${enrollment.yearOfStudy}",
                    icon = Icons.Default.Timeline
                )

                DetailRow(
                    label = "Semester",
                    value = enrollment.semester,
                    icon = Icons.Default.DateRange
                )

                DetailRow(
                    label = "Enrollment Date",
                    value = enrollment.enrollmentDate,
                    icon = Icons.Default.CalendarToday
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}


@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateYearBottomSheet(
    currentYear: Int,
    onDismiss: () -> Unit,
    onUpdate: (Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentYear) }
    val years = listOf(1, 2, 3, 4, 5, 6) // Adjust based on your needs

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Update Year of Study",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Select your current year of study",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Year Selection Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(years) { year ->
                    YearSelectionChip(
                        year = year,
                        isSelected = year == selectedYear,
                        onClick = { selectedYear = year }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onUpdate(selectedYear) },
                    modifier = Modifier.weight(1f),
                    enabled = selectedYear != currentYear
                ) {
                    Text("Update to Year $selectedYear")
                }
            }
        }
    }
}


@Composable
fun YearSelectionChip(
    year: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = if (isSelected) CardDefaults.cardElevation(4.dp)
        else CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Year",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}