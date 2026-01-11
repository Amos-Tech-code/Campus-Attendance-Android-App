package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.data.repositories.AttendanceRepository
import com.amos_tech_code.smartattend.data.repositories.AuthRepository
import org.koin.dsl.module

val repositoryModule = module {

    single { AuthRepository(get()) }

    single { AttendanceRepository(get(), get()) }

}