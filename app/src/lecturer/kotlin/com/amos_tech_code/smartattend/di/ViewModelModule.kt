package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.ui.feature.home.HomeViewModel
import com.amos_tech_code.smartattend.ui.feature.live_attendance.LiveAttendanceViewModel
import com.amos_tech_code.smartattend.ui.feature.notification.NotificationViewModel
import com.amos_tech_code.smartattend.ui.feature.profile.ProfileViewModel
import com.amos_tech_code.smartattend.ui.feature.settings.SettingsViewModel
import com.amos_tech_code.smartattend.ui.feature.setup.SetupViewModel
import com.amos_tech_code.smartattend.ui.feature.signIn.SignInViewModel
import com.amos_tech_code.smartattend.ui.feature.start_session.StartSessionViewModel
import com.amos_tech_code.smartattend.ui.feature.student_lookup.StudentLookupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { SignInViewModel(get(), get()) }

    viewModel { SetupViewModel(get(), get()) }

    viewModel { HomeViewModel(get()) }

    viewModel { LiveAttendanceViewModel() }

    viewModel { ProfileViewModel() }

    viewModel { StartSessionViewModel(get()) }

    viewModel { StudentLookupViewModel() }

    viewModel { NotificationViewModel() }

    viewModel { SettingsViewModel() }


}