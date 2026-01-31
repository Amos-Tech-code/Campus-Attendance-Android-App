package com.amos_tech_code.smartattend

import SessionHistoryScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.amos_tech_code.smartattend.ui.feature.home.HomeScreen
import com.amos_tech_code.smartattend.ui.feature.live_attendance.LiveAttendanceScreen
import com.amos_tech_code.smartattend.ui.feature.notification.NotificationScreen
import com.amos_tech_code.smartattend.ui.feature.profile.ProfileScreen
import com.amos_tech_code.smartattend.ui.feature.settings.SettingsScreen
import com.amos_tech_code.smartattend.ui.feature.setup.UniversitySetupScreen
import com.amos_tech_code.smartattend.ui.feature.signIn.SignInScreen
import com.amos_tech_code.smartattend.ui.feature.start_session.StartSessionScreen
import com.amos_tech_code.smartattend.ui.feature.student_lookup.StudentLookupScreen
import com.amos_tech_code.smartattend.ui.navigation.AttendanceHistoryRoute
import com.amos_tech_code.smartattend.ui.navigation.HomeRoute
import com.amos_tech_code.smartattend.ui.navigation.LiveAttendanceRoute
import com.amos_tech_code.smartattend.ui.navigation.NavRoutes
import com.amos_tech_code.smartattend.ui.navigation.NotificationsRoute
import com.amos_tech_code.smartattend.ui.navigation.ProfileRoute
import com.amos_tech_code.smartattend.ui.navigation.SetUpRoute
import com.amos_tech_code.smartattend.ui.navigation.SettingsRoute
import com.amos_tech_code.smartattend.ui.navigation.SignInRoute
import com.amos_tech_code.smartattend.ui.navigation.SmartAttendNavHost
import com.amos_tech_code.smartattend.ui.navigation.StartSessionRoute
import com.amos_tech_code.smartattend.ui.navigation.StudentLookupRoute

@Composable
fun App(
    navController: NavHostController,
    startDestination: NavRoutes
) {
    SmartAttendNavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        composable<SignInRoute> {
            SignInScreen(navController)
        }

        composable<SetUpRoute> {
            UniversitySetupScreen(navController)
        }

        composable<HomeRoute> {
            HomeScreen(navController)
        }

        composable<StartSessionRoute> {
            StartSessionScreen(navController)
        }

        composable<LiveAttendanceRoute> {
            LiveAttendanceScreen(navController)
        }

        composable<ProfileRoute> {
            ProfileScreen(navController)
        }

        composable<StudentLookupRoute> {
            StudentLookupScreen(navController)
        }

        composable<AttendanceHistoryRoute> {
            SessionHistoryScreen(navController)
        }

        composable<NotificationsRoute> {
            NotificationScreen(navController)
        }

        composable<SettingsRoute> {
            SettingsScreen(navController)
        }

    }
}
