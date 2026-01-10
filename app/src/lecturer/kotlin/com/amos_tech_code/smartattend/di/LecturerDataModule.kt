package com.amos_tech_code.smartattend.di

import android.app.Application
import androidx.room.Room
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.local.room_db.ClassTrackProDatabase
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val lecturerDataModule  = module {

    // Provide Session
    single { ClassTrackProSession(get()) }

    single<SessionProvider> { get<ClassTrackProSession>() }

    // Provide Coroutine Dispatchers
    single<CoroutineDispatcher> { Dispatchers.IO }

    // Provide Room Database
    single {
        Room.databaseBuilder(
            get<Application>(),
            ClassTrackProDatabase::class.java,
            "lecturer_academics_db"
        )
            .fallbackToDestructiveMigration(true) // optional, use only for development
            .build()
    }

    // Provide DAO
    single { get<ClassTrackProDatabase>().lecturerAcademicsDao() }

}