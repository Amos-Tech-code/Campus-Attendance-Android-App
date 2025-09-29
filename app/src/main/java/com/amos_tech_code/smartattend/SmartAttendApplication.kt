package com.amos_tech_code.smartattend

import android.app.Application
import com.amos_tech_code.smartattend.di.studentPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmartAttendApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SmartAttendApplication)
            modules(
                listOf(studentPresentationModule)
            )
        }

    }
}