package com.amos_tech_code.smartattend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

// Primary Brand Colors - Professional Blue (Trust, Security, Education)
val Primary10 = Color(0xFF000F5C)
val Primary20 = Color(0xFF001E8F)
val Primary30 = Color(0xFF002FBF)
val Primary40 = Color(0xFF0041E8)
val Primary80 = Color(0xFF8AA6FF)
val Primary90 = Color(0xFFD6E1FF)
val Primary95 = Color(0xFFECF0FF)
val Primary99 = Color(0xFFFDFBFF)

// Secondary Colors - Complementary Teal (Modern, Fresh)
val Secondary10 = Color(0xFF00201D)
val Secondary20 = Color(0xFF003732)
val Secondary30 = Color(0xFF005048)
val Secondary40 = Color(0xFF006A60)
val Secondary80 = Color(0xFF5EE0D1)
val Secondary90 = Color(0xFF7CF7E7)
val Secondary95 = Color(0xFFB6FFF4)
val Secondary99 = Color(0xFFF2FFFC)

// Tertiary Colors - Vibrant Purple (Engagement, Innovation)
val Tertiary10 = Color(0xFF270057)
val Tertiary20 = Color(0xFF420085)
val Tertiary30 = Color(0xFF5D00B4)
val Tertiary40 = Color(0xFF7800E3)
val Tertiary80 = Color(0xFFD6A4FF)
val Tertiary90 = Color(0xFFEDD6FF)
val Tertiary95 = Color(0xFFF7EAFF)
val Tertiary99 = Color(0xFFFDFBFF)

// Neutral Colors - Professional Grays
val Neutral0 = Color(0xFF000000)
val Neutral10 = Color(0xFF1A1B1F)
val Neutral20 = Color(0xFF2F3036)
val Neutral90 = Color(0xFFE1E2E6)
val Neutral95 = Color(0xFFF0F0F4)
val Neutral99 = Color(0xFFFDFBFF)
val Neutral100 = Color(0xFFFFFFFF)

// Neutral Variant Colors
val NeutralVariant30 = Color(0xFF45464F)
val NeutralVariant50 = Color(0xFF767680)
val NeutralVariant60 = Color(0xFF90909A)
val NeutralVariant80 = Color(0xFFC6C6D0)
val NeutralVariant90 = Color(0xFFE2E2EC)

// Error Colors
val Error10 = Color(0xFF410002)
val Error20 = Color(0xFF690005)
val Error30 = Color(0xFF93000A)
val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val Error90 = Color(0xFFFFDAD6)
val Error100 = Color(0xFFFFFFFF)

// Success Colors
val Success40 = Color(0xFF00A86B)
val Success80 = Color(0xFF5EE0A2)
val Success90 = Color(0xFF7CF7B9)

// Warning Colors
val Warning40 = Color(0xFFE6B400)
val Warning80 = Color(0xFFFFD95C)
val Warning90 = Color(0xFFFFE999)

// Extended Color Palette for specific use cases
val SurfaceDim = Color(0xFFDED8E3)
val SurfaceBright = Color(0xFFFDF7FF)
val SurfaceContainerLow = Color(0xFFF3EDF8)
val SurfaceContainerHigh = Color(0xFFECE6F1)

// Data Visualization Colors
val Data1 = Color(0xFF4361EE)  // Blue
val Data2 = Color(0xFF3A0CA3)  // Purple
val Data3 = Color(0xFF4CC9F0)  // Cyan
val Data4 = Color(0xFF7209B7)  // Violet
val Data5 = Color(0xFFF72585)  // Pink

// Colors.kt
val LiveAttendanceGreen = Color(0xFF10B981)
val LiveAttendanceGreenLight = Color(0xFFD1FAE5)
val LiveAttendanceAmber = Color(0xFFF59E0B)
val LiveAttendanceAmberLight = Color(0xFFFEF3C7)
val LiveAttendanceRed = Color(0xFFEF4444)
val LiveAttendanceRedLight = Color(0xFFFEE2E2)
val LiveAttendanceBlue = Color(0xFF3B82F6)
val LiveAttendanceBlueLight = Color(0xFFDBEAFE)
val LiveAttendancePurple = Color(0xFF8B5CF6)
val LiveAttendancePurpleLight = Color(0xFFEDE9FE)

val PresentColor = LiveAttendanceGreen
val PendingColor = LiveAttendanceAmber
val AbsentColor = LiveAttendanceRed
val NeutralColor = Color(0xFF6B7280)
val NeutralLightColor = Color(0xFFF3F4F6)

// Theme.kt
val MaterialTheme.attendanceColors
    get() = AttendanceColors(
        present = LiveAttendanceGreen,
        presentLight = LiveAttendanceGreenLight,
        pending = LiveAttendanceAmber,
        pendingLight = LiveAttendanceAmberLight,
        absent = LiveAttendanceRed,
        absentLight = LiveAttendanceRedLight,
        flagged = LiveAttendanceRed,
        flaggedLight = LiveAttendanceRedLight
    )

data class AttendanceColors(
    val present: Color,
    val presentLight: Color,
    val pending: Color,
    val pendingLight: Color,
    val absent: Color,
    val absentLight: Color,
    val flagged: Color,
    val flaggedLight: Color
)