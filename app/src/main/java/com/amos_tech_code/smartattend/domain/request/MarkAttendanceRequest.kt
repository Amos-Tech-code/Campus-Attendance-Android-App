package com.amos_tech_code.smartattend.domain.request

import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import kotlinx.serialization.Serializable

@Serializable
data class MarkAttendanceRequest(
    val sessionCode: String,
    val unitCode: String,
    val deviceId: String,
    val programmeId: String? = null, // Required if requiresProgrammeSelection is true in verify session response
    val studentLat: Double? = null,
    val studentLng: Double? = null,
    val methodUsed: AttendanceMethod
)

@Serializable
data class VerifySessionRequest(
    val sessionCode: String,
    val unitCode: String
)