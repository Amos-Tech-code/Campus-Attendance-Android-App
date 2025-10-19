package com.amos_tech_code.smartattend.ui.feature.student_lookup

import com.amos_tech_code.smartattend.ui.feature.setup.AttendanceRecord

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