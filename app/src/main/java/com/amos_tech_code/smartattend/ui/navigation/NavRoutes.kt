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

@Serializable
object SetUpRoute : NavRoutes

@Serializable
object StartSessionRoute : NavRoutes

@Serializable
object LiveAttendanceRoute : NavRoutes

@Serializable
object StudentLookupRoute : NavRoutes

@Serializable
object NotificationsRoute : NavRoutes

@Serializable
object SettingsRoute : NavRoutes

@Serializable
object ExportRoute : NavRoutes

@Serializable
object AllExportsRoute: NavRoutes

@Serializable
data class ExportDetailsRoute(val exportId: String) : NavRoutes