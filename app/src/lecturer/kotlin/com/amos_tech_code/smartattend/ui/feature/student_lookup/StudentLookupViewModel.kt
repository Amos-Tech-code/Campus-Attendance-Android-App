package com.amos_tech_code.smartattend.ui.feature.student_lookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.ui.feature.setup.AttendanceMethod
import com.amos_tech_code.smartattend.ui.feature.setup.AttendanceRecord
import com.amos_tech_code.smartattend.ui.feature.setup.AttendanceStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.filter

class StudentLookupViewModel : ViewModel() {

    private val _state = MutableStateFlow(StudentLookupState())
    val state = _state.asStateFlow()

    private val _event = Channel<StudentLookupEvent>()
    val event = _event.receiveAsFlow()

    fun onEvent(event: StudentLookupUiEvent) {
        when (event) {
            is StudentLookupUiEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            StudentLookupUiEvent.ClearSearch -> {
                _state.update {
                    it.copy(
                        searchQuery = "",
                        searchedStudent = null,
                        searchPerformed = false,
                        searchError = null
                    )
                }
            }
            StudentLookupUiEvent.SearchStudent -> {
                searchStudent()
            }
            is StudentLookupUiEvent.ApproveDeviceRequest -> {
                approveDeviceRequest(event.requestId)
            }
            is StudentLookupUiEvent.RejectDeviceRequest -> {
                rejectDeviceRequest(event.requestId)
            }
        }
    }

    private fun searchStudent() {
        val query = _state.value.searchQuery.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null) }

            try {
                // Simulate API call delay
                delay(1000)

                // Mock search logic - replace with actual API call
                val mockStudent = createMockStudent(query)
                val mockAttendanceRecords = createMockAttendanceRecords()
                val mockDeviceRequests = createMockDeviceRequests()

                _state.update {
                    it.copy(
                        isSearching = false,
                        searchPerformed = true,
                        searchedStudent = mockStudent,
                        attendanceRecords = mockAttendanceRecords,
                        pendingDeviceRequests = mockDeviceRequests
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSearching = false,
                        searchError = "Failed to search student: ${e.message}"
                    )
                }
                _event.send(StudentLookupEvent.ShowErrorMessage("Search failed. Please try again."))
            }
        }
    }

    private fun approveDeviceRequest(requestId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingDeviceRequest = true) }

            try {
                // Simulate API call
                delay(500)

                // Update local state
                val updatedRequests = _state.value.pendingDeviceRequests.filter { it.id != requestId }
                _state.update {
                    it.copy(
                        pendingDeviceRequests = updatedRequests,
                        isUpdatingDeviceRequest = false
                    )
                }

                _event.send(StudentLookupEvent.DeviceRequestUpdated(requestId, "approved"))

            } catch (e: Exception) {
                _state.update { it.copy(isUpdatingDeviceRequest = false) }
                _event.send(StudentLookupEvent.ShowErrorMessage("Failed to approve device request"))
            }
        }
    }

    private fun rejectDeviceRequest(requestId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingDeviceRequest = true) }

            try {
                // Simulate API call
                delay(500)

                // Update local state
                val updatedRequests = _state.value.pendingDeviceRequests.filter { it.id != requestId }
                _state.update {
                    it.copy(
                        pendingDeviceRequests = updatedRequests,
                        isUpdatingDeviceRequest = false
                    )
                }

                _event.send(StudentLookupEvent.DeviceRequestUpdated(requestId, "rejected"))

            } catch (e: Exception) {
                _state.update { it.copy(isUpdatingDeviceRequest = false) }
                _event.send(StudentLookupEvent.ShowErrorMessage("Failed to reject device request"))
            }
        }
    }

    // Mock data generators
    private fun createMockStudent(query: String): StudentWithDetails? {
        // Return null if no match (for testing "not found" scenario)
        if (query.contains("notfound", ignoreCase = true)) return null

        return StudentWithDetails(
            id = "student_${System.currentTimeMillis()}",
            name = if (query.matches(Regex(".*[a-zA-Z].*"))) {
                // If query contains letters, use it as name
                query.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            } else {
                "John Doe" // Default name for registration number search
            },
            registrationNo = if (query.matches(Regex("\\d+"))) {
                // If query is all digits, use it as registration number
                query
            } else {
                "U123/2021" // Default registration number
            },
            department = "Computer Science",
            semester = "4",
            deviceVerified = query.contains("verified", ignoreCase = true),
            lastLogin = "2024-01-15 09:45 AM",
            currentDevice = "Samsung Galaxy S21 Ultra"
        )
    }

    private fun createMockAttendanceRecords(): List<AttendanceRecord> {
        return listOf(
            AttendanceRecord(
                id = "record_1",
                sessionId = "session_1",
                courseName = "Mobile Application Development",
                courseCode = "CS401",
                date = "2024-01-15",
                time = "10:05 AM",
                status = AttendanceStatus.PRESENT,
                method = AttendanceMethod.QR_CODE,
                location = "Room 301, CS Building",
                distance = 15,
                deviceVerified = true,
                locationVerified = true,
                verified = true,
                lecturerName = "Dr. Smith",
                sessionDuration = "90 min"
            ),
            AttendanceRecord(
                id = "record_2",
                sessionId = "session_2",
                courseName = "Software Engineering",
                courseCode = "CS402",
                date = "2024-01-14",
                time = "02:15 PM",
                status = AttendanceStatus.LATE,
                method = AttendanceMethod.MANUAL_CODE,
                location = "Room 205, Main Building",
                distance = 45,
                deviceVerified = true,
                locationVerified = true,
                verified = true,
                lecturerName = "Prof. Johnson",
                sessionDuration = "90 min"
            ),
            AttendanceRecord(
                id = "record_3",
                sessionId = "session_3",
                courseName = "Database Systems",
                courseCode = "CS301",
                date = "2024-01-13",
                time = "08:20 AM",
                status = AttendanceStatus.ABSENT,
                method = AttendanceMethod.GPS,
                location = null,
                distance = null,
                deviceVerified = false,
                locationVerified = false,
                verified = false,
                lecturerName = "Dr. Williams",
                sessionDuration = "90 min"
            )
        )
    }

    private fun createMockDeviceRequests(): List<DeviceChangeRequest> {
        return listOf(
            DeviceChangeRequest(
                id = "device_req_1",
                studentId = "student_1",
                studentName = "John Doe",
                studentRegNo = "U123/2021",
                requestDate = "2024-01-15 10:30 AM",
                newDeviceInfo = "Samsung Galaxy S22 Ultra",
                status = DeviceRequestStatus.PENDING
            ),
            DeviceChangeRequest(
                id = "device_req_2",
                studentId = "student_2",
                studentName = "Jane Smith",
                studentRegNo = "U124/2021",
                requestDate = "2024-01-14 03:15 PM",
                newDeviceInfo = "Google Pixel 7",
                status = DeviceRequestStatus.PENDING
            )
        )
    }
}