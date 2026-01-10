package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.services.LocationService
import com.amos_tech_code.smartattend.services.impl.LocationServiceImpl
import org.koin.dsl.module

val localModule = module {

    // Location Service
    single<LocationService> {
        LocationServiceImpl(get())
    }

}