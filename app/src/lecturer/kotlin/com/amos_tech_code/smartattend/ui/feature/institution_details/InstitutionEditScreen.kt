package com.amos_tech_code.smartattend.ui.feature.institution_details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import kotlinx.coroutines.launch

// ==================== PROGRAMME BOTTOM SHEET ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgrammeEditBottomSheet(
    programme: ProgrammeEntity?,
    units: List<UnitEntity>,
    onDismiss: () -> Unit,
    onSave: (ProgrammeEdit, List<UnitEdit>) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            ) {
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp))
            }
        }
    ) {
        var programmeName by remember { mutableStateOf(programme?.name ?: "") }
        var yearOfStudy by remember { mutableStateOf(programme?.yearOfStudy?.toString() ?: "1") }
        var expectedStudents by remember { mutableStateOf(programme?.expectedStudentCount?.toString() ?: "0") }
        var isActive by remember { mutableStateOf(programme != null) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        // Unit edits
        val unitEdits = remember(units) {
            units.map { unit ->
                UnitEdit(
                    id = unit.id,
                    code = unit.code,
                    name = unit.name,
                    semester = unit.semester,
                    lectureDay = unit.lectureDay,
                    lectureTime = unit.lectureTime,
                    lectureVenue = unit.lectureVenue,
                    isActive = true
                )
            }
        }
        var updatedUnits by remember { mutableStateOf(unitEdits) }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (programme == null) "Add Programme" else "Edit Programme",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }

            // Programme Name
            item {
                OutlinedTextField(
                    value = programmeName,
                    onValueChange = { programmeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Programme Name") },
                    placeholder = { Text("e.g., Bachelor of Computer Science") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = programmeName.isBlank()
                )
            }

            // Year of Study
            item {
                OutlinedTextField(
                    value = yearOfStudy,
                    onValueChange = { if (it.all { char -> char.isDigit() }) yearOfStudy = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Year of Study") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }

            // Expected Students
            item {
                OutlinedTextField(
                    value = expectedStudents,
                    onValueChange = { if (it.all { char -> char.isDigit() }) expectedStudents = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Expected Students") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }

            // Active Switch (only for existing programmes)
            if (programme != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = if (isActive)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Programme Active",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Units Section
            if (units.isNotEmpty()) {
                item {
                    Text(
                        text = "Associated Units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(units) { unit ->
                    UnitEditItem(
                        unit = unit,
                        onUpdate = { updatedUnit ->
                            updatedUnits = updatedUnits.map {
                                if (it.id == unit.id) updatedUnit else it
                            }
                        }
                    )
                }
            }

            // Delete Button (for existing programmes)
            if (programme != null) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Delete Programme",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        val programmeEdit = ProgrammeEdit(
                            id = programme?.id ?: "",
                            name = programmeName,
                            departmentId = programme?.departmentId,
                            departmentName = "", // This would come from department data
                            yearOfStudy = yearOfStudy.toIntOrNull() ?: 1,
                            expectedStudentCount = expectedStudents.toIntOrNull() ?: 0,
                            isActive = isActive
                        )
                        onSave(programmeEdit, updatedUnits)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = programmeName.isNotBlank() && yearOfStudy.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Programme") },
                text = { Text("Are you sure you want to delete this programme? This will remove all associated units.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(programme?.id ?: "")
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ==================== UNIT BOTTOM SHEET ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitEditBottomSheet(
    unit: UnitEntity?,
    programmes: List<ProgrammeEntity>,
    onDismiss: () -> Unit,
    onSave: (UnitEdit, String) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        var unitCode by remember { mutableStateOf(unit?.code ?: "") }
        var unitName by remember { mutableStateOf(unit?.name ?: "") }
        var semester by remember { mutableStateOf(unit?.semester?.toString() ?: "1") }
        var lectureDay by remember { mutableStateOf(unit?.lectureDay ?: "") }
        var lectureTime by remember { mutableStateOf(unit?.lectureTime ?: "") }
        var lectureVenue by remember { mutableStateOf(unit?.lectureVenue ?: "") }
        var isActive by remember { mutableStateOf(unit != null) }
        var selectedProgrammeId by remember { mutableStateOf(programmes.firstOrNull()?.id ?: "") }
        var showDeleteDialog by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (unit == null) "Add Unit" else "Edit Unit",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }

            // Programme Selection (for new units)
            if (unit == null && programmes.isNotEmpty()) {
                item {
                    Text(
                        text = "Select Programme",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    programmes.take(3).forEach { programme ->
                        ProgrammeSelectionChip(
                            programme = programme,
                            isSelected = selectedProgrammeId == programme.id,
                            onSelected = { selectedProgrammeId = programme.id }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (programmes.size > 3) {
                        TextButton(
                            onClick = { /* Show all programmes */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View all ${programmes.size} programmes")
                        }
                    }
                }
            }

            // Unit Code
            item {
                OutlinedTextField(
                    value = unitCode,
                    onValueChange = { unitCode = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Unit Code") },
                    placeholder = { Text("e.g., CS401") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = unitCode.isBlank(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                    )
                )
            }

            // Unit Name
            item {
                OutlinedTextField(
                    value = unitName,
                    onValueChange = { unitName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Unit Name") },
                    placeholder = { Text("e.g., Mobile Application Development") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = unitName.isBlank()
                )
            }

            // Semester
            item {
                OutlinedTextField(
                    value = semester,
                    onValueChange = { if (it.all { char -> char.isDigit() }) semester = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Semester") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }

            // Lecture Details Section
            item {
                Text(
                    text = "Lecture Details (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = lectureDay,
                    onValueChange = { lectureDay = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lecture Day") },
                    placeholder = { Text("e.g., Monday") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = lectureTime,
                    onValueChange = { lectureTime = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lecture Time") },
                    placeholder = { Text("e.g., 9:00 AM - 11:00 AM") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = lectureVenue,
                    onValueChange = { lectureVenue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Venue") },
                    placeholder = { Text("e.g., Room 101") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Active Switch (for existing units)
            if (unit != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Book,
                                contentDescription = null,
                                tint = if (isActive)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Unit Active",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Delete Button (for existing units)
            if (unit != null) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Delete Unit",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        val unitEdit = UnitEdit(
                            id = unit?.id ?: "",
                            code = unitCode,
                            name = unitName,
                            semester = semester.toIntOrNull() ?: 1,
                            lectureDay = lectureDay.takeIf { it.isNotBlank() },
                            lectureTime = lectureTime.takeIf { it.isNotBlank() },
                            lectureVenue = lectureVenue.takeIf { it.isNotBlank() },
                            isActive = isActive
                        )
                        onSave(unitEdit, selectedProgrammeId)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = unitCode.isNotBlank() && unitName.isNotBlank() &&
                            (unit != null || selectedProgrammeId.isNotBlank()),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Unit") },
                text = { Text("Are you sure you want to delete this unit?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(unit?.id ?: "")
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProgrammeSelectionChip(
    programme: ProgrammeEntity,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelected() },
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = programme.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = "Year ${programme.yearOfStudy} • ${programme.expectedStudentCount} students",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun UnitEditItem(
    unit: UnitEntity,
    onUpdate: (UnitEdit) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var lectureDay by remember { mutableStateOf(unit.lectureDay ?: "") }
    var lectureTime by remember { mutableStateOf(unit.lectureTime ?: "") }
    var lectureVenue by remember { mutableStateOf(unit.lectureVenue ?: "") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${unit.code} - ${unit.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Semester ${unit.semester}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = if (isExpanded) "Close" else "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lectureDay,
                        onValueChange = { lectureDay = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lecture Day") },
                        placeholder = { Text(unit.lectureDay ?: "Not set") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lectureTime,
                        onValueChange = { lectureTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lecture Time") },
                        placeholder = { Text(unit.lectureTime ?: "Not set") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lectureVenue,
                        onValueChange = { lectureVenue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Venue") },
                        placeholder = { Text(unit.lectureVenue ?: "Not set") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            onUpdate(
                                UnitEdit(
                                    id = unit.id,
                                    code = unit.code,
                                    name = unit.name,
                                    semester = unit.semester,
                                    lectureDay = lectureDay.takeIf { it.isNotBlank() },
                                    lectureTime = lectureTime.takeIf { it.isNotBlank() },
                                    lectureVenue = lectureVenue.takeIf { it.isNotBlank() },
                                    isActive = true
                                )
                            )
                            isExpanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

// ==================== ACADEMIC TERM BOTTOM SHEET ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicTermBottomSheet(
    terms: List<AcademicTermEntity>,
    activeTerm: AcademicTermEntity?,
    onDismiss: () -> Unit,
    onSetActive: (String) -> Unit,
    onAddTerm: (String, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        var showAddTerm by remember { mutableStateOf(false) }
        var newAcademicYear by remember { mutableStateOf("") }
        var newSemester by remember { mutableStateOf("1") }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Academic Terms",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }

            // Add Term Button
            item {
                Button(
                    onClick = { showAddTerm = !showAddTerm },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        if (showAddTerm) "Cancel" else "Add New Term",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Add Term Form
            if (showAddTerm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Add New Academic Term",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = newAcademicYear,
                                onValueChange = { newAcademicYear = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Academic Year") },
                                placeholder = { Text("e.g., 2025-2026") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = newSemester,
                                onValueChange = { if (it.all { char -> char.isDigit() }) newSemester = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Semester") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )

                            Button(
                                onClick = {
                                    if (newAcademicYear.isNotBlank()) {
                                        onAddTerm(newAcademicYear, newSemester.toIntOrNull() ?: 1)
                                        newAcademicYear = ""
                                        newSemester = "1"
                                        showAddTerm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = newAcademicYear.isNotBlank(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Add Term")
                            }
                        }
                    }
                }
            }

            // Terms List
            if (terms.isNotEmpty()) {
                item {
                    Text(
                        text = "Existing Terms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(terms) { term ->
                    TermEditItem(
                        term = term,
                        isActive = term.id == activeTerm?.id,
                        onSetActive = { onSetActive(term.id) }
                    )
                }
            } else {
                item {
                    EmptyStateMessage(
                        message = "No academic terms added yet",
                        icon = Icons.Default.CalendarMonth
                    )
                }
            }
        }
    }
}

@Composable
private fun TermEditItem(
    term: AcademicTermEntity,
    isActive: Boolean,
    onSetActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = term.academicYear,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Semester ${term.semester}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isActive) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onSetActive,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Set Active")
                }
            }
        }
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