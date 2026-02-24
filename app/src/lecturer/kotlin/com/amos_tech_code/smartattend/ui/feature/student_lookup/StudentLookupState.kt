package com.amos_tech_code.smartattend.ui.feature.student_lookup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.amos_tech_code.smartattend.domain.models.AttendanceMethod
import com.amos_tech_code.smartattend.ui.theme.AbsentColor
import com.amos_tech_code.smartattend.ui.theme.NeutralVariant50
import com.amos_tech_code.smartattend.ui.theme.PendingColor
import com.amos_tech_code.smartattend.ui.theme.PresentColor

//sealed class StudentLookupState {
//    data object Nothing : StudentLookupState()
//    data object Loading : StudentLookupState()
//    data object Success : StudentLookupState()
//    data class Error(val message: String) : StudentLookupState()
//}

data class StudentLookupState(
    // Search State
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchPerformed: Boolean = false,

    // Student Data
    val searchedStudent: StudentWithDetails? = null,
    val attendanceRecords: List<AttendanceRecord> = emptyList(),

    // Device Requests
    val pendingDeviceRequests: List<DeviceChangeRequest> = emptyList(),
    val isUpdatingDeviceRequest: Boolean = false,

    // Error State
    val searchError: String? = null,
    val deviceRequestError: String? = null
)


data class StudentWithDetails(
    val id: String,
    val name: String,
    val registrationNo: String,
    val department: String,
    val semester: String,
    val deviceVerified: Boolean,
    val lastLogin: String,
    val currentDevice: String? = null,
    val email: String? = null,
    val phone: String? = null
)

data class DeviceChangeRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentRegNo: String,
    val requestDate: String,
    val newDeviceInfo: String,
    val status: DeviceRequestStatus = DeviceRequestStatus.PENDING,
    val reason: String? = null,
    val oldDeviceInfo: String? = null
)

enum class DeviceRequestStatus {
    PENDING, APPROVED, REJECTED
}

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, PENDING
}

data class Student(
    val name: String = "",
    val registrationNo: String = "",
    val email: String = "",
    val department: String = "",
    val semester: String = "",
    val profileImage: String? = null
)


data class AttendanceRecord(
    val id: String,
    val sessionId: String,
    val courseName: String,
    val courseCode: String,
    val date: String,
    val time: String,
    val status: AttendanceStatus,
    val method: AttendanceMethod,
    val location: String? = null,
    val distance: Int? = null,
    val deviceVerified: Boolean = true,
    val locationVerified: Boolean = true,
    val verified: Boolean = false,
    val lecturerName: String = "",
    val sessionDuration: String = "60 min"
) {
    // Helper property for display
    val displayDateTime: String
        get() = "$date • $time"

    // Helper property for status color
    val statusColor: Color
        get() = when (status) {
            AttendanceStatus.PRESENT -> PresentColor
            AttendanceStatus.ABSENT -> AbsentColor
            AttendanceStatus.LATE -> PendingColor
            AttendanceStatus.PENDING -> NeutralVariant50
        }

    // Helper property for status icon
    val statusIcon: ImageVector
        get() = when (status) {
            AttendanceStatus.PRESENT -> Icons.Default.CheckCircle
            AttendanceStatus.ABSENT -> Icons.Default.Cancel
            AttendanceStatus.LATE -> Icons.Default.Schedule
            AttendanceStatus.PENDING -> Icons.Default.Pending
        }
}