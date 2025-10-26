package com.amos_tech_code.smartattend.services

import android.app.Activity
import android.content.Context
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.LocationPermissionState

interface LocationService {

    fun isLocationEnabled(): Boolean

    fun promptEnableGPS(
        activity: Activity,
        enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    )
    suspend fun getCurrentLocation(): LocationData
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String?
    fun shouldRequestLocationPermission(): Boolean

    fun getPermissionState(activity: Activity): LocationPermissionState

}