package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.data.repositories.AuthRepository
import com.amos_tech_code.smartattend.data.repositories.DeviceChangeRepository
import com.amos_tech_code.smartattend.data.repositories.NotificationRepository
import org.koin.dsl.module

val repositoryModule = module {

    single { AuthRepository(get()) }

    single { NotificationRepository(get()) }

    single { DeviceChangeRepository(get()) }

}