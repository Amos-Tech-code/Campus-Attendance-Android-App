package com.amos_tech_code.smartattend

import android.app.Application
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.di.dataModule
import com.amos_tech_code.smartattend.di.flavorPresentationModule
import com.amos_tech_code.smartattend.notifications.SmartAttendNotificationManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmartAttendApplication : Application() {

    lateinit var notificationManager: SmartAttendNotificationManager
        private set

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SmartAttendApplication)
            // Provide the property value for isDebug
            properties(mapOf("isDebug" to BuildConfig.DEBUG))
            modules(
                listOf(
                    dataModule,
                    flavorPresentationModule
                )
            )

            val apiService: ApiService by inject()
            val session: SessionProvider by inject()
            // Initialize Notification Manager
            notificationManager = SmartAttendNotificationManager(
                apiService = apiService,
                session = session,
                context = this@SmartAttendApplication
            )
            notificationManager.createChannels()
            notificationManager.getAndStoreToken()
        }

    }
}