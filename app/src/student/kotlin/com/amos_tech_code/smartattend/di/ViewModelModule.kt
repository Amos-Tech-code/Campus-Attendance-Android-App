package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.ui.feature.login.LoginViewModel
import com.amos_tech_code.smartattend.ui.feature.register.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { RegisterViewModel() }

    viewModel { LoginViewModel() }
}