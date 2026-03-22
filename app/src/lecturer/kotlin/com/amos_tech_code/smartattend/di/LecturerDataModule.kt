package com.amos_tech_code.smartattend.di

import android.app.Application
import androidx.room.Room
import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.local.room_db.ClassTrackProDatabase
import com.amos_tech_code.smartattend.data.local.room_db.migrations.MIGRATION_1_2
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.repository.AcademicSetUpRepository
import com.amos_tech_code.smartattend.data.repository.AccountRepository
import com.amos_tech_code.smartattend.data.repository.AttendanceRepository
import com.amos_tech_code.smartattend.data.repository.ExportRepository
import com.amos_tech_code.smartattend.data.repository.SessionRepository
import com.amos_tech_code.smartattend.data.repository.StudentLookupRepository
import com.amos_tech_code.smartattend.data.repository.UniversityRepository
import com.amos_tech_code.smartattend.services.FileDownloadManager
import org.koin.dsl.module

val lecturerDataModule  = module {

    // Provide Session
    single { ClassTrackProSession(get()) }
    // Provide SessionProvider
    single<SessionProvider> { get<ClassTrackProSession>() }
    // Provide the FileDownloadManager
    single { FileDownloadManager }

    // Provide Room Database
    single {
        Room.databaseBuilder(
            get<Application>(),
            ClassTrackProDatabase::class.java,
            "class_track_lecturer_db"
        )
            //.fallbackToDestructiveMigration(false) // optional, use only for development
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addMigrations(MIGRATION_1_2) // Add migration
            .build()
    }

    // Provide DAOs
    single { get<ClassTrackProDatabase>().lecturerAcademicsDao() }

    single { get<ClassTrackProDatabase>().attendanceHistoryDao() }

    single { get<ClassTrackProDatabase>().attendanceExportDao() }

    // Provide Repositories
    single { AccountRepository(get(), get(), get(), get(), get()) }

    single { AcademicSetUpRepository(get(), get(), get(), get()) }

    single { UniversityRepository(get()) }

    single { AttendanceRepository(get(), get()) }

    single { SessionRepository(get(), get(), get()) }

    single { ExportRepository(get(), get(), get(), get(), get( )) }

    single { StudentLookupRepository(get()) }

}