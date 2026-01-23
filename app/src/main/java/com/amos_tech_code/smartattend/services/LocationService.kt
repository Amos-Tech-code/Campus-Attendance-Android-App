package com.amos_tech_code.smartattend.services

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.amos_tech_code.smartattend.domain.models.LocationData

interface LocationService {

    fun isLocationEnabled(): Boolean

    fun promptEnableGPS(
        activity: Activity,
        enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    )
    suspend fun getCurrentLocation(): LocationData
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String?

//    fun shouldRequestLocationPermission(): Boolean
//
//    fun getPermissionState(activity: Activity): LocationPermissionState

}