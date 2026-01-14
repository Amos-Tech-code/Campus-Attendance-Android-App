package com.amos_tech_code.smartattend.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * Data class for QR code content
 */
@Serializable
data class QRCodeData(
    val sessionCode: String,
    val unitCode: String,
    val sessionId: String,
    val timestamp: Long,
    val version: String
) {
    companion object {
        fun fromJson(json: String): QRCodeData? {
            return try {
                Json.decodeFromString<QRCodeData>(json)
            } catch (e: Exception) {
                null
            }
        }

        fun isValid(qrCodeData: QRCodeData): Boolean {
            return qrCodeData.sessionCode.isNotBlank() &&
                    qrCodeData.unitCode.isNotBlank() &&
                    qrCodeData.sessionId.isNotBlank()
                    //&& System.currentTimeMillis() - qrCodeData.timestamp < 24 * 60 * 60 * 1000 // 24 hours
        }
    }
}