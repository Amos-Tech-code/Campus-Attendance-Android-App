package com.amos_tech_code.smartattend.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: NavRoutes,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
) {
    /**
     * Student Bottom Navigation Items
     */
    object Home : BottomNavItem(
        route = HomeRoute,
        title = "Home",
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Attendance : BottomNavItem(
        route = AttendanceRoute(),
        title = "Attendance",
        selectedIcon = Icons.Default.QrCodeScanner,
        unselectedIcon = Icons.Outlined.QrCodeScanner,
        badgeCount = 0
    )

    object History : BottomNavItem(
        route = AttendanceHistoryRoute,
        title = "History",
        selectedIcon = Icons.Default.History,
        unselectedIcon = Icons.Outlined.History
    )

    object Profile : BottomNavItem(
        route = ProfileRoute,
        title = "Profile",
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    /**
     * Lecturer Bottom Navigation Items
     */
    object Dashboard : BottomNavItem(
        route = HomeRoute,
        title = "Dashboard",
        selectedIcon = Icons.Default.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        badgeCount = 0
    )

    object StartSession : BottomNavItem(
        route = StartSessionRoute,
        title = "New Session",
        selectedIcon = Icons.Default.QrCodeScanner,
        unselectedIcon = Icons.Outlined.QrCodeScanner,
    )

    object LiveAttendance : BottomNavItem(
        route = LiveAttendanceRoute,
        title = "Live View",
        selectedIcon = Icons.Default.People,
        unselectedIcon = Icons.Outlined.People,
        badgeCount = 0 // Active sessions count
    )
}