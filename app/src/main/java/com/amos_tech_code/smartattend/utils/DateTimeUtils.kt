package com.amos_tech_code.smartattend.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

/**
 * Converts ISO-8601 date-time string to epoch millis.
 *
 * Supports:
 * - 2026-03-01T09:00:42.867300850Z
 * - 2026-03-01T09:00:42Z
 * - 2026-03-01T09:00:42+03:00
 * - 2026-03-01T09:00:42.867300850
 *
 * If timezone is missing, UTC is assumed.
 */
@OptIn(ExperimentalTime::class)
fun String.toEpochMillisOrNull(): Long? {
    if (this.isBlank()) return null
    return runCatching {
        // First try parsing as Instant (requires timezone or Z)
        Instant.parse(this).toEpochMilliseconds()
    }.getOrElse {
        // If that fails, assume UTC and parse as LocalDateTime
        runCatching {
            LocalDateTime.parse(this)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        }.getOrNull()
    }
}

/**
 * Parses ISO-8601 string that MUST contain timezone (Z or offset).
 *
 * Examples:
 *  - 2026-03-01T09:00:42Z
 *  - 2026-03-01T09:00:42.867300850Z
 *  - 2026-03-01T09:00:42+03:00
 *
 * Throws IllegalArgumentException if timezone is missing or invalid.
 */
@OptIn(ExperimentalTime::class)
fun String.toEpochMillisStrict(): Long =
    Instant.parse(this).toEpochMilliseconds()