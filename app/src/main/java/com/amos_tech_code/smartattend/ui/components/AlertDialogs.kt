package com.amos_tech_code.smartattend.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amos_tech_code.smartattend.R

@Composable
fun SuccessAlertDialog(
    title: String = "Success",
    message: String,
    icon: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = "Great!",
    onConfirmButtonClick: () -> Unit,
    dismissButtonText: String? = null,
    onDismissButtonClick: (() -> Unit)? = null,
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    dismissButtonColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    messageTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true
    )
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = if (icon == null) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = "Success Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else icon,
        title = {
            Column {
                Text(
                    text = title,
                    style = titleTextStyle,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = messageTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmButtonClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = confirmButtonColor
                )
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = if (dismissButtonText != null && onDismissButtonClick != null) {
            {
                TextButton(
                    onClick = {
                        onDismissButtonClick()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = dismissButtonColor
                    )
                ) {
                    Text(text = dismissButtonText)
                }
            }
        } else null,
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        properties = properties
    )
}


@Composable
fun ErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    positiveButtonText: String = "OK",
    onPositiveButtonClick: (() -> Unit)? = null,
    negativeButtonText: String? = null,
    onNegativeButtonClick: (() -> Unit)? = null,
    icon: ImageVector? = Icons.Outlined.ErrorOutline
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .shadow(
                    elevation = 32.dp,
                    shape = MaterialTheme.shapes.extraLarge,
                    clip = false
                ),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Error message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (negativeButtonText != null) {
                        TextButton(
                            onClick = {
                                onNegativeButtonClick?.invoke()
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = negativeButtonText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onPositiveButtonClick?.invoke()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = MaterialTheme.shapes.large,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = positiveButtonText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


// Enhanced version with retry capability
@Composable
fun NetworkErrorDialog(
    modifier: Modifier = Modifier,
    title: String = "Connection Error",
    message: String = "Unable to connect to the server. Please check your internet connection and try again.",
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    ErrorDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        modifier = modifier,
        positiveButtonText = if (onRetry != null) "Retry" else "OK",
        onPositiveButtonClick = onRetry,
        negativeButtonText = if (onRetry != null) "Cancel" else null,
        onNegativeButtonClick = onDismiss,
        icon = Icons.Default.WifiOff
    )
}


@Composable
fun ProfileCompletionRequiredDialog(
    onDismissRequest: () -> Unit,
    onCompleteProfile: () -> Unit,
    onRemindLater: (() -> Unit)? = null,
    isDismissible: Boolean = false // Don't allow dismissing for mandatory profile completion
) {
    WarningAlertDialog(
        title = "Profile Setup Required",
        message = "Please complete your profile setup to start tracking attendance. This includes adding your institution and department details.",
        onDismissRequest = onDismissRequest,
        confirmButtonText = "Complete Profile",
        onConfirmButtonClick = onCompleteProfile,
        dismissButtonText = if (onRemindLater != null) "Remind Me Later" else null,
        onDismissButtonClick = onRemindLater,
        icon = {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Profile Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        isDismissible = isDismissible,
        confirmButtonColor = MaterialTheme.colorScheme.primary
    )
}


@Composable
fun WarningAlertDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = "Continue",
    onConfirmButtonClick: () -> Unit,
    dismissButtonText: String? = null,
    onDismissButtonClick: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = "Warning Icon",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
    },
    isDismissible: Boolean = true,
    confirmButtonColor: Color = MaterialTheme.colorScheme.error,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    messageTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true
    )
) {
    AlertDialog(
        onDismissRequest = {
            if (isDismissible) {
                onDismissRequest()
            }
        },
        icon = icon,
        title = {
            Column {
                Text(
                    text = title,
                    style = titleTextStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = messageTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmButtonColor
                )
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = if (dismissButtonText != null && onDismissButtonClick != null) {
            {
                TextButton(
                    onClick = {
                        onDismissButtonClick()
                        onDismissRequest()
                    }
                ) {
                    Text(text = dismissButtonText)
                }
            }
        } else null,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        properties = properties
    )
}