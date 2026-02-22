package com.amos_tech_code.smartattend.ui.feature.export.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceExportEntity
import com.amos_tech_code.smartattend.domain.models.ExportFormat
import com.amos_tech_code.smartattend.ui.feature.export.formatDate
import com.amos_tech_code.smartattend.ui.feature.export.formatFileSize
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExportDetailsDialog(
    export: AttendanceExportEntity,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Format and File Name
                DetailRow(
                    icon = if (export.exportFormat == ExportFormat.PDF)
                        Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                    label = "Format",
                    value = export.exportFormat.name,
                    valueColor = if (export.exportFormat == ExportFormat.PDF)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                // Programme
                DetailRow(
                    icon = Icons.Default.School,
                    label = "Programme",
                    value = export.programmeName ?: "N/A"
                )

                // Unit
                DetailRow(
                    icon = Icons.Default.Description,
                    label = "Unit",
                    value = "${export.unitCode ?: "N/A"} - ${export.unitName ?: "N/A"}"
                )

                // Week Range (if available)
                export.weekRange?.let {
                    DetailRow(
                        icon = Icons.Default.Info,
                        label = "Week Range",
                        value = it
                    )
                }

                // Date Created
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Created",
                    value = formatDate(export.createdAt)
                )

                // File Size
                DetailRow(
                    icon = Icons.Default.Info,
                    label = "File Size",
                    value = formatFileSize(export.fileSize)
                )

                // Download Status
                DetailRow(
                    icon = Icons.Default.Info,
                    label = "Status",
                    value = if (export.localFilePath != null) "Downloaded" else "Not Downloaded",
                    valueColor = if (export.localFilePath != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Expiry (if available)
                export.expiresAt?.let {
                    DetailRow(
                        icon = Icons.Default.Info,
                        label = "Expires",
                        value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(java.util.Date(it))
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }

                if (export.localFilePath != null) {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onView()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View")
                    }
                } else {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onDownload()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Download")
                    }
                }

                TextButton(
                    onClick = {
                        onDismiss()
                        onShare()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Share")
                }

                TextButton(
                    onClick = {
                        onDismiss()
                        onDelete()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    fileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Export") },
        text = { Text("Are you sure you want to delete '$fileName'? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}