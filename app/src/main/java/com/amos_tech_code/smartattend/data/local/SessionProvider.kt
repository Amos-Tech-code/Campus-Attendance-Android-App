package com.amos_tech_code.smartattend.data.local

import com.amos_tech_code.smartattend.domain.models.DeviceStatus

interface SessionProvider {

    fun getValidToken(): String?

    fun saveFcmToken(token: String)

    fun getFCMToken(): String?

    fun updateDeviceStatus(deviceStatus: DeviceStatus)
}
