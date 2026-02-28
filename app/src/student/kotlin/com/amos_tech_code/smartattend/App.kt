package com.amos_tech_code.smartattend

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.amos_tech_code.smartattend.ui.feature.attendance.AttendanceScreen
import com.amos_tech_code.smartattend.ui.feature.history.HistoryScreen
import com.amos_tech_code.smartattend.ui.feature.home.HomeScreen
import com.amos_tech_code.smartattend.ui.feature.login.LoginScreen
import com.amos_tech_code.smartattend.ui.feature.profile.ProfileScreen
import com.amos_tech_code.smartattend.ui.feature.register.RegisterScreen
import com.amos_tech_code.smartattend.ui.navigation.AttendanceHistoryRoute
import com.amos_tech_code.smartattend.ui.navigation.AttendanceRoute
import com.amos_tech_code.smartattend.ui.navigation.HomeRoute
import com.amos_tech_code.smartattend.ui.navigation.NavRoutes
import com.amos_tech_code.smartattend.ui.navigation.ProfileRoute
import com.amos_tech_code.smartattend.ui.navigation.RegisterRoute
import com.amos_tech_code.smartattend.ui.navigation.SignInRoute
import com.amos_tech_code.smartattend.ui.navigation.SmartAttendNavHost

@Composable
fun App(
    navController: NavHostController,
    startDestination: NavRoutes
) {
    SmartAttendNavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<RegisterRoute> {
            RegisterScreen(navController)
        }

        composable<SignInRoute> {
            LoginScreen(navController)
        }

        composable<HomeRoute> {
            HomeScreen(navController)
        }

        composable<AttendanceRoute> { backStackEntry ->
            val screen = backStackEntry.toRoute<AttendanceRoute>().screen
            AttendanceScreen(navController, screen)

        }

        composable<AttendanceHistoryRoute> {
            HistoryScreen(navController)
        }

        composable<ProfileRoute> {
            ProfileScreen(navController)
        }
    }

}