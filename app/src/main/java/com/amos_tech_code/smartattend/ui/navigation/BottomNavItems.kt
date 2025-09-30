package com.amos_tech_code.smartattend.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StudentBottomNavItem(
    val route: NavRoutes,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
) {
    object Home : StudentBottomNavItem(
        route = HomeRoute,
        title = "Home",
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Attendance : StudentBottomNavItem(
        route = AttendanceRoute,
        title = "Attendance",
        selectedIcon = Icons.Default.QrCodeScanner,
        unselectedIcon = Icons.Outlined.QrCodeScanner,
        badgeCount = 1
    )

    object History : StudentBottomNavItem(
        route = AttendanceHistoryRoute,
        title = "History",
        selectedIcon = Icons.Default.History,
        unselectedIcon = Icons.Outlined.History
    )

    object Profile : StudentBottomNavItem(
        route = ProfileRoute,
        title = "Profile",
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}