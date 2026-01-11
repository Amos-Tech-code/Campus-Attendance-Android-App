package com.amos_tech_code.smartattend.ui.feature.signIn

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.R
import com.amos_tech_code.smartattend.ui.components.ErrorDialog
import com.amos_tech_code.smartattend.ui.components.LoadingDialog
import com.amos_tech_code.smartattend.ui.components.NetworkErrorDialog
import com.amos_tech_code.smartattend.ui.components.SmartAttendHeightSpacer
import com.amos_tech_code.smartattend.ui.components.SmartAttendWidthSpacer
import com.amos_tech_code.smartattend.ui.navigation.HomeRoute
import com.amos_tech_code.smartattend.ui.navigation.SignInRoute
import com.amos_tech_code.smartattend.ui.theme.Success40
import com.amos_tech_code.smartattend.ui.theme.Warning40
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = koinViewModel()
) {

    // Collect state and events
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showErrorDialog by remember { mutableStateOf(false) }
    var showNetworkErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var errorTitle by remember { mutableStateOf("Error") }

    // Observe events
    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is SignInEvent.ShowErrorDialog -> {
                errorTitle = "Error"
                errorMessage = event.message
                showErrorDialog = true
            }

            SignInEvent.NavigateToHome -> {
                navController.navigate(HomeRoute) {
                    popUpTo(SignInRoute) { inclusive = true }
                }
            }

            is SignInEvent.ShowNetworkErrorDialog -> {
                errorTitle = "Network Error"
                errorMessage = event.message
                showNetworkErrorDialog = true
            }
        }
    }

    val isLoading = state is SignInState.Loading
    val activity = LocalContext.current as ComponentActivity

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    )
    { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background with gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // App Logo and Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // App Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SmartAttendance",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                SmartAttendHeightSpacer(16.dp)

                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Welcome Text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Lecturer Portal",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_class), // Use lecturer icon
                                    contentDescription = "Lecturer Portal Icon",
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = "Sign in to manage your classes and track student attendance with ease",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }

                        // Features List
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FeatureItem(
                                icon = Icons.Default.QrCode,
                                title = "Generate Attendance Codes",
                                description = "Create secure QR codes and time-bound access codes"
                            )
                            FeatureItem(
                                icon = Icons.Default.BarChart,
                                title = "Track Student Attendance",
                                description = "Monitor real-time attendance and view detailed reports"
                            )
                            FeatureItem(
                                icon = Icons.Default.Security,
                                title = "Secure & Verified",
                                description = "GPS verification and device binding for secure attendance"
                            )
                        }

                        // Google Sign In Button
                        GoogleSignInButton(
                            isLoading = viewModel.googleAuthUiLoading.value,
                            onClick = {
                                viewModel.initiateGoogleLogin(activity)
                            }
                        )

                        // Terms and Privacy
                        Text(
                            text = "By signing in, you agree to our Terms of Service and Privacy Policy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                SmartAttendHeightSpacer(32.dp)

                // Support Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Support,
                            contentDescription = "Support",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Need Help?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Contact support for account issues or technical assistance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                SmartAttendHeightSpacer(16.dp)
            }
        }
    }


    // Error Dialog
    if (showErrorDialog) {
        ErrorDialog(
            title = errorTitle,
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }

    if (showNetworkErrorDialog) {
        NetworkErrorDialog(
            title = errorTitle,
            message = errorMessage,
            onDismiss = { showNetworkErrorDialog = false },
            onRetry = {
                // You can add retry logic here if needed
                viewModel.initiateGoogleLogin(activity)
                showNetworkErrorDialog = false
            }
        )
    }

    if (isLoading) {
        LoadingDialog(message = "Signing in...")
    }

}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val googleColors = listOf(
        MaterialTheme.colorScheme.primary,      // Blue-like
        Success40,     // Green-like
        MaterialTheme.colorScheme.error,        // Red-like
        Warning40   // Yellow-like
    )

    val borderBrush = remember {
        Brush.horizontalGradient(
            colors = googleColors,
            startX = 0f,
            endX = Float.POSITIVE_INFINITY
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            2.dp,
            borderBrush
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = "Google"
                    )
                }

                SmartAttendWidthSpacer(12.dp)

                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}