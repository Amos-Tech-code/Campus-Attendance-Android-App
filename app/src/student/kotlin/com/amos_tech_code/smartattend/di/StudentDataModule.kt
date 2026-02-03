package com.amos_tech_code.smartattend.di

import android.app.Application
import androidx.room.Room
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.local.room.ClassTrackDatabase
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.repository.AttendanceSessionRepository
import com.amos_tech_code.smartattend.data.repository.EnrollmentRepository
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val studentDataModule = module {

    // Provide Session
    single { ClassTrackSession(get()) }

    single<SessionProvider> { get<ClassTrackSession>() }

    // Device Info provider
    single { DeviceInfoProvider(androidContext()) }

    // Provide Room Database
    single {
        Room.databaseBuilder(
            get<Application>(),
            ClassTrackDatabase::class.java,
            "class_track_student_db"
        )
            //.fallbackToDestructiveMigration(false) // optional, use only for development
            .fallbackToDestructiveMigrationOnDowngrade(true)
            //.addMigrations(MIGRATION_1_2) // Add migration
            .build()
    }

    // Provide DAO
    single { get<ClassTrackDatabase>().enrollmentDao() }

    single { get<ClassTrackDatabase>().attendanceDao() }

    // Repository
    single { EnrollmentRepository(get(), get()) }

    single { AttendanceSessionRepository(get(), get()) }

}