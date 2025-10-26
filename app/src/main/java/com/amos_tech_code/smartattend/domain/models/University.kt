package com.amos_tech_code.smartattend.domain.models

data class University(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val programmes: List<Programme> = emptyList()
)

data class Programme(
    val id: String,
    val name: String,
    val department: String,
    val yearOfStudy: Int,
    val units: List<UnitModel>
)

data class UnitModel(
    val id: String,
    val code: String,
    val name: String
)
