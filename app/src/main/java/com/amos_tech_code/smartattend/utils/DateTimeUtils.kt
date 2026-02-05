package com.amos_tech_code.smartattend.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a given timestamp (in milliseconds) into a user-friendly date and time string.
 * Example: "MMM dd, yyyy 'at' hh:mm a" -> "Feb 28, 2026 at 10:30 AM"
 *
 * @return A formatted date and time string.
 */
fun Long.formatDateTime(): String {
    val date = Date(this)
    val format = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return format.format(date)
}

/**
 * Formats a given timestamp (in milliseconds) into a user-friendly date string.
 * Example: "MMM dd, yyyy" -> "Feb 28, 2026"
 */
fun Long.formatDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 *
 * @return A formatted AM/PM time string.
 */
fun String.toAmPmTime(): String {
    val inputFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        Locale.getDefault()
    )
    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    return try {
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (_: Exception) {
        this
    }
}