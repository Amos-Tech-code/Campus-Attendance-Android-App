package com.amos_tech_code.smartattend.domain.models

// Location Data Class
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)