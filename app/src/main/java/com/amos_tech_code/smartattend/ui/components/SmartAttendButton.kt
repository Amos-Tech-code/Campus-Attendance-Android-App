package com.amos_tech_code.smartattend.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun SmartAttendPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    size: SmartAttendButtonSize = SmartAttendButtonSize.Large
) {
    SmartAttendButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        isLoading = isLoading,
        buttonStyle = SmartAttendButtonStyle.Primary,
        size = size
    ) {
        Text(
            text = text,
            style = when (size) {
                SmartAttendButtonSize.Small -> MaterialTheme.typography.labelMedium
                SmartAttendButtonSize.Medium -> MaterialTheme.typography.bodyMedium
                SmartAttendButtonSize.Large -> MaterialTheme.typography.bodyLarge
            },
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun SmartAttendSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    size: SmartAttendButtonSize = SmartAttendButtonSize.Medium
) {
    SmartAttendButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        buttonStyle = SmartAttendButtonStyle.Secondary,
        size = size
    ) {
        Text(
            text = text,
            style = when (size) {
                SmartAttendButtonSize.Small -> MaterialTheme.typography.labelMedium
                SmartAttendButtonSize.Medium -> MaterialTheme.typography.bodyMedium
                SmartAttendButtonSize.Large -> MaterialTheme.typography.bodyLarge
            },
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun SmartAttendErrorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    size: SmartAttendButtonSize = SmartAttendButtonSize.Medium
) {
    SmartAttendButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        buttonStyle = SmartAttendButtonStyle.Error,
        size = size
    ) {
        Text(
            text = text,
            style = when (size) {
                SmartAttendButtonSize.Small -> MaterialTheme.typography.labelMedium
                SmartAttendButtonSize.Medium -> MaterialTheme.typography.bodyMedium
                SmartAttendButtonSize.Large -> MaterialTheme.typography.bodyLarge
            },
            fontWeight = FontWeight.Medium
        )
    }
}



// Additional Button Style
@Composable
fun SmartAttendOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            SmartAttendWidthSpacer(8.dp)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun SmartAttendTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SmartAttendPrimaryButtonWithLeadingIcon(
    text: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    size: SmartAttendButtonSize = SmartAttendButtonSize.Large
) {
    SmartAttendButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        isLoading = isLoading,
        buttonStyle = SmartAttendButtonStyle.Primary,
        size = size
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Only show icon and text when not loading
            if (!isLoading) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = when (size) {
                        SmartAttendButtonSize.Small -> MaterialTheme.typography.labelMedium
                        SmartAttendButtonSize.Medium -> MaterialTheme.typography.bodyMedium
                        SmartAttendButtonSize.Large -> MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SmartAttendButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonStyle: SmartAttendButtonStyle = SmartAttendButtonStyle.Primary,
    size: SmartAttendButtonSize = SmartAttendButtonSize.Large,
    content: @Composable RowScope.() -> Unit
) {
    val containerColor = when (buttonStyle) {
        SmartAttendButtonStyle.Primary -> MaterialTheme.colorScheme.primary
        SmartAttendButtonStyle.Secondary -> MaterialTheme.colorScheme.secondary
        SmartAttendButtonStyle.Tertiary -> MaterialTheme.colorScheme.tertiary
        SmartAttendButtonStyle.Error -> MaterialTheme.colorScheme.error
        SmartAttendButtonStyle.Surface -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (buttonStyle) {
        SmartAttendButtonStyle.Primary -> MaterialTheme.colorScheme.onPrimary
        SmartAttendButtonStyle.Secondary -> MaterialTheme.colorScheme.onSecondary
        SmartAttendButtonStyle.Tertiary -> MaterialTheme.colorScheme.onTertiary
        SmartAttendButtonStyle.Error -> MaterialTheme.colorScheme.onError
        SmartAttendButtonStyle.Surface -> MaterialTheme.colorScheme.onSurface
    }

    val disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    val height = when (size) {
        SmartAttendButtonSize.Small -> 40.dp
        SmartAttendButtonSize.Medium -> 48.dp
        SmartAttendButtonSize.Large -> 56.dp
    }

    val shape = when (size) {
        SmartAttendButtonSize.Small -> MaterialTheme.shapes.small
        SmartAttendButtonSize.Medium -> MaterialTheme.shapes.medium
        SmartAttendButtonSize.Large -> MaterialTheme.shapes.large
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        contentPadding = when (size) {
            SmartAttendButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            SmartAttendButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            SmartAttendButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        }
    ) {
        AnimatedVisibility(
            visible = isLoading
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        }

        AnimatedVisibility(visible = !isLoading) {
            content()
        }
    }
}

// Button Style Enum
enum class SmartAttendButtonStyle {
    Primary, Secondary, Tertiary, Error, Surface
}

// Button Size Enum
enum class SmartAttendButtonSize {
    Small, Medium, Large
}