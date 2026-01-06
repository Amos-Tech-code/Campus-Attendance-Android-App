package com.amos_tech_code.smartattend

import android.app.Application
import com.amos_tech_code.smartattend.di.dataModule
import com.amos_tech_code.smartattend.di.flavorPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmartAttendApplication : Application() {

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
        }

    }
}