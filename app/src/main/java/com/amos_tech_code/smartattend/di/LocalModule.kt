package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.services.LocationService
import com.amos_tech_code.smartattend.services.impl.LocationServiceImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val localModule = module {

    // Location Service
    single<LocationService> {
        LocationServiceImpl(get())
    }

    // Provide Coroutine Dispatchers
    single<CoroutineDispatcher> { Dispatchers.IO }

}