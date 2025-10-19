package com.amos_tech_code.smartattend.di

import org.koin.dsl.module

val lecturerPresentationModule = module {
    includes(viewModelModule)
}