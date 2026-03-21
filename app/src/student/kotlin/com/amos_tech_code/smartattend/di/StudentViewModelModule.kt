package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.ui.feature.attendance.AttendanceViewModel
import com.amos_tech_code.smartattend.ui.feature.device_change.DeviceChangeViewModel
import com.amos_tech_code.smartattend.ui.feature.history.HistoryViewModel
import com.amos_tech_code.smartattend.ui.feature.home.HomeViewModel
import com.amos_tech_code.smartattend.ui.feature.login.LoginViewModel
import com.amos_tech_code.smartattend.ui.feature.notification.StudentNotificationViewModel
import com.amos_tech_code.smartattend.ui.feature.profile.ProfileViewModel
import com.amos_tech_code.smartattend.ui.feature.register.RegisterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val studentViewModelModule = module {

    viewModel { RegisterViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get(), get()) }

    viewModel { HomeViewModel(get(), get(), get()) }
    // Provide ContentResolver
    single { androidContext().contentResolver }

    viewModel { AttendanceViewModel(
        get(), get(), get(), get(), get(), get()
    )}
    viewModel { HistoryViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { StudentNotificationViewModel(get()) }
    viewModel { DeviceChangeViewModel(get(), get(), get()) }

}