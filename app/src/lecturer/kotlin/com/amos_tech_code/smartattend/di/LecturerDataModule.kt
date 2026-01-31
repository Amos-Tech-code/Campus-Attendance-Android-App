package com.amos_tech_code.smartattend.di

import android.app.Application
import androidx.room.Room
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.local.room_db.ClassTrackProDatabase
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import org.koin.dsl.module

val lecturerDataModule  = module {

    // Provide Session
    single { ClassTrackProSession(get()) }

    single<SessionProvider> { get<ClassTrackProSession>() }

    // Provide Room Database
    single {
        Room.databaseBuilder(
            get<Application>(),
            ClassTrackProDatabase::class.java,
            "class_track_lecturer_db"
        )
            //.fallbackToDestructiveMigration(false) // optional, use only for development
            .fallbackToDestructiveMigrationOnDowngrade(true)
            //.addMigrations(MIGRATION_1_2) // Add migration
            .build()
    }

    // Provide DAO
    single { get<ClassTrackProDatabase>().lecturerAcademicsDao() }

    single { get<ClassTrackProDatabase>().attendanceHistoryDao() }

    // Provide Repository
    single { AcademicSetUpRepository(get(), get(), get(), get()) }

    single { UniversityRepository(get()) }

}