package com.amos_tech_code.smartattend.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toAmPmTime(): String {
    val inputFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        Locale.getDefault()
    )
    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    return try {
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}