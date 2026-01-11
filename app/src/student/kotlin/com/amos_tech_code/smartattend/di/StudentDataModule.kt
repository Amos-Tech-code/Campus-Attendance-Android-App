package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val studentDataModule = module {

    // Provide Session
    single { ClassTrackSession(get()) }

    single<SessionProvider> { get<ClassTrackSession>() }

    // Device Info provider
    single { DeviceInfoProvider(androidContext()) }


}