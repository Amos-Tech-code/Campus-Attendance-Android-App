package com.amos_tech_code.smartattend.ui.feature.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.R
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.components.SmartAttendWidthSpacer
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = koinViewModel()
) {

    // Collect state and events
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Keyboard controller for handling keyboard state
    val keyboardController = LocalSoftwareKeyboardController.current
    // Snackbar host state for showing snackbars
    val snackBarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is RegisterEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_LONG).show()
            }
            is RegisterEvent.NavigateToHome -> {
                // Show success and navigate
            }

            RegisterEvent.NavigateToLogin -> {
                navController.navigateUp()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Student Registration",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background decoration
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_register),
                        contentDescription = "Login",
                        modifier = Modifier.size(48.dp)
                    )
                }

                SmartAttendHeightSpacer(8.dp)
                // Header Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Join SmartAttendance",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    SmartAttendHeightSpacer(8.dp)
                    Text(
                        text = "Register your device for secure attendance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                SmartAttendHeightSpacer(8.dp)

                // Registration Form
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Full Name Field
                    SmartAttendNameTextField(
                        value = state.fullName,
                        onValueChange = { viewModel.updateFullName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.fullNameError != null,
                        errorMessage = state.fullNameError
                    )

                    // Registration Number Field
                    SmartAttendRegNoTextField(
                        value = state.regNo,
                        onValueChange = { viewModel.updateRegNo(it) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.regNoError != null,
                        errorMessage = state.regNoError
                    )
                }

                // Device Registration Info Card
                InfoCard(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.PhoneAndroid,
                    title = "Device Registration",
                    description = "This device will be securely linked to your account for attendance marking.",
                    iconTint = MaterialTheme.colorScheme.primary
                )

                SmartAttendHeightSpacer(24.dp)

                // Register Button
                SmartAttendPrimaryButton(
                    text = "Register Device",
                    onClick = {
                        keyboardController?.hide()
                        viewModel.register()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    isLoading = state.isLoading
                )

                // Login Prompt
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already registered?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    SmartAttendWidthSpacer(4.dp)
                    SmartAttendTextButton(
                        text = "Login here",
                        onClick = {
                            viewModel.navigateToLogin()
                        }
                    )
                }

                SmartAttendHeightSpacer(16.dp)
            }
        }
    }
}


@Composable
private fun SmartAttendNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    SmartAttendTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = "Full Name",
        placeholder = "Enter your full name as per university records",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Full Name",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
private fun SmartAttendRegNoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingMessage: String? = "This will be your login ID"
) {
    SmartAttendTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = "Registration Number",
        placeholder = "e.g., U123/2021",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = "Registration Number",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        isError = isError,
        errorMessage = errorMessage,
        supportingMessage = supportingMessage,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            imeAction = ImeAction.Done
        )
    )
}


// Reusable Info Card Component
@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = MaterialTheme.typography.labelSmall.lineHeight
            )
        }
    }
}
