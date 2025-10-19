package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.utils.DeviceInfoProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val studentPresentationModule = module {

    includes(viewModelModule)

    single { DeviceInfoProvider(androidContext()) }

}