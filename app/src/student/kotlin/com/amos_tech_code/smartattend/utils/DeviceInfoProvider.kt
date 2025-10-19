package com.amos_tech_code.smartattend.utils

import android.content.Context
import android.os.Build
import com.amos_tech_code.smartattend.domain.models.request.DeviceInfo
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdInfo
import kotlinx.coroutines.tasks.await
import android.provider.Settings
import android.util.Log
import java.security.MessageDigest

class DeviceInfoProvider(
    private val context: Context
) {

    suspend fun getDeviceInfo(): DeviceInfo {
        return try {
            val appSetIdInfo: AppSetIdInfo = AppSet.getClient(context).appSetIdInfo.await()
            DeviceInfo(
                deviceId = appSetIdInfo.id,
                model = "${Build.MANUFACTURER} ${Build.MODEL}",
                os = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
            )
        } catch (e: Exception) {
            Log.e("DeviceInfoProvider", "Error getting device info", e)
            // Fallback if Play Services not available
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val rawId = "$androidId-${Build.MANUFACTURER}-${Build.MODEL}"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashedId = digest.digest(rawId.toByteArray()).joinToString("") { "%02x".format(it) }

            DeviceInfo(
                deviceId = hashedId,
                model = "${Build.MANUFACTURER} ${Build.MODEL}",
                os = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
            )
        }
    }
}
