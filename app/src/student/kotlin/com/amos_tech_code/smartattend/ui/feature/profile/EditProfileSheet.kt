package com.amos_tech_code.smartattend.ui.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amos_tech_code.smartattend.domain.request.UpdateStudentProfileRequest
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (UpdateStudentProfileRequest) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    var registrationNo by remember { mutableStateOf(student.registrationNo) }
    var isLoading by remember { mutableStateOf(false) }

    // Track if fields have been touched/interacted with
    var isNameTouched by remember { mutableStateOf(false) }
    var isRegistrationNoTouched by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Form
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmartAttendTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isNameTouched = true
                    },
                    label = "Full Name",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = isNameTouched && name.isBlank(),
                    errorMessage = if (isNameTouched && name.isBlank()) "Name is required" else ""
                )

                SmartAttendTextField(
                    value = registrationNo,
                    onValueChange = {
                        registrationNo = it
                        isRegistrationNoTouched = true
                    },
                    label = "Registration Number",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Numbers, contentDescription = null)
                    },
                    isError = isRegistrationNoTouched && registrationNo.isBlank(),
                    errorMessage = if (isRegistrationNoTouched && registrationNo.isBlank()) "Registration number is required" else ""
                )
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
                        isLoading = true
                        onSave(
                            UpdateStudentProfileRequest(
                                fullName = name,
                                registrationNumber = registrationNo
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && registrationNo.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}