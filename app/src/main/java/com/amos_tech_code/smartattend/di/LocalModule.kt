package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import org.koin.dsl.module

val localModule = module {

    single { SmartAttendSession(get()) }

}