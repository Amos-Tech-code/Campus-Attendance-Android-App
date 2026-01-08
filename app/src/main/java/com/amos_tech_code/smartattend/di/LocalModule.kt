package com.amos_tech_code.smartattend.di

import android.app.Application
import androidx.room.Room
import com.amos_tech_code.smartattend.data.local.room_db.ClassTrackDatabase
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.services.LocationService
import com.amos_tech_code.smartattend.services.impl.LocationServiceImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val localModule = module {

    single { SmartAttendSession(get()) }

    // Provide Coroutine Dispatchers
    single<CoroutineDispatcher> { Dispatchers.IO }

    // Provide Room Database
    single {
        Room.databaseBuilder(
            get<Application>(),
            ClassTrackDatabase::class.java,
            "lecturer_academics_db"
        )
            .fallbackToDestructiveMigration(true) // optional, use only for development
            .build()
    }

    // Provide DAO
    single { get<ClassTrackDatabase>().lecturerAcademicsDao() }

    // Location Service
    single<LocationService> {
        LocationServiceImpl(get())
    }

}