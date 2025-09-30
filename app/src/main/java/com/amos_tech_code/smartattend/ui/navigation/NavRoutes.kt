package com.amos_tech_code.smartattend.ui.navigation

import kotlinx.serialization.Serializable

interface NavRoutes

@Serializable
object OnboardingRoute : NavRoutes

@Serializable
object SignInRoute : NavRoutes

@Serializable
object RegisterRoute : NavRoutes

@Serializable
object HomeRoute : NavRoutes

@Serializable
object AttendanceRoute : NavRoutes

@Serializable
object AttendanceHistoryRoute : NavRoutes

@Serializable
object ProfileRoute : NavRoutes