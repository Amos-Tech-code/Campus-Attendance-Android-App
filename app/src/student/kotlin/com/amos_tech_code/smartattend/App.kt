package com.amos_tech_code.smartattend

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.amos_tech_code.smartattend.ui.feature.login.LoginScreen
import com.amos_tech_code.smartattend.ui.feature.register.RegisterScreen
import com.amos_tech_code.smartattend.ui.navigation.RegisterRoute
import com.amos_tech_code.smartattend.ui.navigation.SignInRoute
import com.amos_tech_code.smartattend.ui.navigation.SmartAttendNavHost

@Composable
fun App(
    navController: NavHostController,
    //startDestination: NavRoutes
) {
    SmartAttendNavHost(
        navController = navController,
        startDestination = SignInRoute,
    ) {
        composable<RegisterRoute> {
            RegisterScreen(navController)
        }

        composable<SignInRoute> {
            LoginScreen(navController)
        }
    }

}