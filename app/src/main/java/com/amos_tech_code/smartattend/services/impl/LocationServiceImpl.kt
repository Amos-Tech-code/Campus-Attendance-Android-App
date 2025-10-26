package com.amos_tech_code.smartattend.services.impl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amos_tech_code.smartattend.domain.models.LocationData
import com.amos_tech_code.smartattend.domain.models.LocationPermissionState
import com.amos_tech_code.smartattend.services.LocationService
import com.amos_tech_code.smartattend.utils.LocationServiceException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationServiceImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context),
    private val geocoder: Geocoder = Geocoder(context)
) : LocationService {

    // Function to check if GPS is enabled
    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Function to prompt user to enable GPS
    override fun promptEnableGPS(
        activity: Activity,
        enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(activity)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    enableGpsLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    //sendEx.printStackTrace()
                }
            } else {
                Toast.makeText(
                    activity,
                    "GPS is required for location services",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override suspend fun getCurrentLocation(): LocationData {
        return try {
            // Check permission first
            if (!hasLocationPermission(context)) {
                throw SecurityException("Location permission not granted")
            }

            // Create a CancellationTokenSource for cancellation control
            val cancellationTokenSource = CancellationTokenSource()

            // Create a CurrentLocationRequest (the correct type)
            val currentLocationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(15000) // Set timeout duration
                .build()

            // Get current location
            val location = fusedLocationClient.getCurrentLocation(
                currentLocationRequest,
                cancellationTokenSource.token
            ).await()

            if (location == null) {
                throw Exception("Unable to get current location - location is null")
            }

            // Get address from location
            val address = getAddressFromLocation(location.latitude, location.longitude)

            LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                address = address,
                accuracy = location.accuracy,
                timestamp = System.currentTimeMillis()
            )

        } catch (e: SecurityException) {
            throw LocationServiceException("Location permission denied", e)
        } catch (e: Exception) {
            throw LocationServiceException("Failed to get location: ${e.message}", e)
        }
    }

    override suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            // The getFromLocation method might be deprecated on newer APIs,
            // but this implementation is functionally correct for now.
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                // Build address string
                val addressParts = mutableListOf<String>()
                address.thoroughfare?.let { addressParts.add(it) } // Street
                address.subLocality?.let { addressParts.add(it) } // Area
                address.locality?.let { addressParts.add(it) } // City
                address.countryName?.let { addressParts.add(it) } // Country
                addressParts.takeIf { it.isNotEmpty() }?.joinToString(", ")
            }
        } catch (e: Exception) {
            // Geocoding might fail, return null
            null
        }
    }


    // method to check if we need to request permission
    override fun shouldRequestLocationPermission(): Boolean {
        return !hasLocationPermission(context)
    }

    // method to get permission state
    override fun getPermissionState(activity: Activity): LocationPermissionState {
        return when {
            hasLocationPermission(activity) -> LocationPermissionState.GRANTED
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ->
                LocationPermissionState.DENIED_SHOW_RATIONALE

            else -> LocationPermissionState.DENIED_NEVER_ASK
        }
    }

    private fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }


}
