package com.amos_tech_code.smartattend.domain.request

import kotlinx.serialization.Serializable

@Serializable
data class LecturerMarkAttendanceRequest(
    val sessionCode: String,
    val unitCode: String,
    val studentRegNo: String,

    )