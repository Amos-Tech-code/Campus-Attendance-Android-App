package com.amos_tech_code.smartattend.ui.feature.login

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.amos_tech_code.smartattend.ui.components.ErrorDialog
import com.amos_tech_code.smartattend.ui.components.LoadingDialog
import com.amos_tech_code.smartattend.ui.components.NetworkErrorDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendPrimaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendSecondaryButton
import com.amos_tech_code.smartattend.ui.components.SmartAttendTextField
import com.amos_tech_code.smartattend.ui.navigation.HomeRoute
import com.amos_tech_code.smartattend.ui.navigation.RegisterRoute
import com.amos_tech_code.smartattend.ui.navigation.SignInRoute
import com.amos_tech_code.smartattend.utils.ErrorMessageType
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = koinViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Collect state and events
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showErrorDialog by remember { mutableStateOf(false) }
    var showNetworkErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is LoginEvent.ShowErrorMessage -> {
                errorMessage = event.message
                if (event.type == ErrorMessageType.NETWORK) showNetworkErrorDialog = true else showErrorDialog = true
            }

            LoginEvent.NavigateToHome -> {
                navController.navigate(HomeRoute) {
                    popUpTo(SignInRoute) { inclusive = true }
                }
            }

            LoginEvent.NavigateToRegister -> {
                navController.navigate(RegisterRoute)
            }
        }
    }

    Scaffold(
        topBar = {
            
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Student Login",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    )
    { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background decoration with subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
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
                // Welcome Icon with Animation
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
                        painter = painterResource(id = R.drawable.ic_login), // Use your login icon
                        contentDescription = "Login",
                        modifier = Modifier.size(48.dp)
                    )
                }

                SmartAttendHeightSpacer(8.dp)

                // Header Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sign in to mark your attendance securely",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                SmartAttendHeightSpacer(8.dp)

                // Login Form
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Registration Number Field
                    SmartAttendTextField(
                        value = state.regNo,
                        onValueChange = { viewModel.updateRegNo(it) },
                        label = "Registration Number",
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.regNoError != null,
                        errorMessage = state.regNoError,
                        supportingMessage = "Enter your registration number",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        )
                    )
                }

                SmartAttendHeightSpacer(16.dp)

                // Login Button
                SmartAttendPrimaryButton(
                    text = "Sign In",
                    onClick = {
                        keyboardController?.hide()
                        viewModel.login()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                )

                SmartAttendHeightSpacer(16.dp)

                // Divider with text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Text(
                        text = "New to SmartAttendance?",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }

                SmartAttendHeightSpacer(16.dp)

                // Register Button
                SmartAttendSecondaryButton(
                    text = "Create Account",
                    onClick = {
                        viewModel.navigateToRegister()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )

                // Support Text
                Text(
                    text = "Having trouble signing in? Contact support.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp)
                )

                SmartAttendHeightSpacer(16.dp)
            }
        }
    }

    if (showErrorDialog) {
        ErrorDialog(
            title = "Login Failed",
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }

    if (showNetworkErrorDialog) {
        NetworkErrorDialog(
            title = "Login Failed",
            message = errorMessage,
            onDismiss = { showNetworkErrorDialog = false },
            onRetry = {
                // You can add retry logic here if needed
                viewModel.login()
                showNetworkErrorDialog = false
            }
        )
    }

    if (state.isLoading) {
        LoadingDialog(message = "Signing in...")
    }
}
