package com.amos_tech_code.smartattend.data.local

import com.amos_tech_code.smartattend.domain.models.DeviceStatus

interface SessionProvider {

    fun getValidToken(): String?

    fun saveFcmToken(token: String)

    fun getFCMToken(): String?

    fun hasFCMTokenBeenUpdated(): Boolean

    fun setFCMUpdated(status: Boolean)

    fun updateDeviceStatus(deviceStatus: DeviceStatus)

}
