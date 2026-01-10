package com.amos_tech_code.smartattend.ui.feature.live_attendance

import androidx.compose.runtime.Stable
import com.amos_tech_code.smartattend.domain.response.AttendanceMarkedEventDto
import com.amos_tech_code.smartattend.domain.response.LiveAttendanceSnapshot
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Live Attendance State
@Stable
data class LiveAttendanceState(
    // Session Information
    val session: StartAttendanceSessionResponse? = null,

    // Live Attendance Data
    val programmes: List<ProgrammeAttendance> = emptyList(),

    // Processed Student Lists
    val allStudents: List<StudentAttendance> = emptyList(), // Combined list of all attended students

    // UI State
    val showQrCode: Boolean = false,
    val isLoading: Boolean = false,
    val isConnected: Boolean = true,
    val connectionState: ConnectionState = ConnectionState.CONNECTED,
    val lastUpdate: String = "",

    // Filtering and Sorting State
    val filterOptions: FilterOptions = FilterOptions(),
    val sortOptions: SortOptions = SortOptions()
) {
    // Derived properties for filtered and sorted data
    val filteredStudents: List<StudentAttendance>
        get() {
            var filtered = allStudents

            // Apply programme filter
            filterOptions.selectedProgrammeId?.let { programmeId ->
                filtered = filtered.filter { it.programmeId == programmeId }
            }

            // Apply flagged-only filter
//            if (filterOptions.showOnlyFlagged) {
//                filtered = filtered.filter { it.isSuspicious }
//            }

            // Apply sorting
            filtered = filtered.sortedWith(getComparator(sortOptions.sortBy, sortOptions.sortOrder))

            return filtered
        }

    val flaggedStudents: List<StudentAttendance>
        get() = filteredStudents.filter { it.isSuspicious }

    val approvedStudents: List<StudentAttendance>
        get() = filteredStudents.filter { !it.isSuspicious }

    // Get available programmes for filtering
    val programmeOptions: List<ProgrammeOption>
        get() = programmes.map { programme ->
            ProgrammeOption(
                id = programme.programmeId,
                name = programme.programmeName,
                years = listOf(programme.yearGroups.firstOrNull()?.year ?: 1)
            )
        }

    // Statistics for current filter
    val attendanceStats: AttendanceStats
        get() = calculateStatsForFilter()

    private fun calculateStatsForFilter(): AttendanceStats {
        if (programmes.isEmpty()) return AttendanceStats()

        // Calculate total expected based on current filter
        val totalExpected = if (filterOptions.selectedProgrammeId != null) {
            programmes.find { it.programmeId == filterOptions.selectedProgrammeId }
                ?.yearGroups?.sumOf { it.noOfExpectedStudents } ?: 0
        } else {
            programmes.flatMap { it.yearGroups }.sumOf { it.noOfExpectedStudents }
        }

        val totalAttended = filteredStudents.size
        val totalFlagged = filteredStudents.count { it.isSuspicious }
        val totalApproved = totalAttended - totalFlagged
        val totalAbsent = maxOf(0, totalExpected - totalAttended)

        // Calculate percentages
        val attendancePercentage = if (totalExpected > 0) {
            (totalAttended * 100f / totalExpected)
        } else 0f

        val approvedPercentage = if (totalExpected > 0) {
            (totalApproved * 100f / totalExpected)
        } else 0f

        val flaggedPercentage = if (totalExpected > 0) {
            (totalFlagged * 100f / totalExpected)
        } else 0f

        val absentPercentage = if (totalExpected > 0) {
            (totalAbsent * 100f / totalExpected)
        } else 0f

        return AttendanceStats(
            totalExpectedStudents = totalExpected,
            totalFlagged = totalFlagged,
            totalAttended = totalAttended,
            totalAbsent = totalAbsent,
            attendancePercentage = attendancePercentage,
            approvedPercentage = approvedPercentage,
            flaggedPercentage = flaggedPercentage,
            absentPercentage = absentPercentage
        )
    }

    private fun getComparator(sortBy: SortBy, sortOrder: SortOrder): Comparator<StudentAttendance> {
        return when (sortBy) {
            SortBy.NAME -> compareBy<StudentAttendance> { it.student.name }
            SortBy.REG_NO -> compareBy { it.student.registrationNo }
            SortBy.TIME -> compareBy { it.timestamp }
            SortBy.STATUS -> compareBy { it.isSuspicious } // Flagged comes after non-flagged
        }.let { comparator ->
            if (sortOrder == SortOrder.DESCENDING) comparator.reversed() else comparator
        }
    }
}

// Update FilterOptions
data class FilterOptions(
    val selectedProgrammeId: String? = null,
    val selectedYear: Int? = null,
    val showOnlyFlagged: Boolean = false
)

// Add SortOptions
data class SortOptions(
    val sortBy: SortBy = SortBy.TIME, // Default to time
    val sortOrder: SortOrder = SortOrder.DESCENDING // Default to newest first
)

// Update SortType enum
enum class SortBy {
    NAME, REG_NO, TIME, STATUS
}

// Add SortOrder enum
enum class SortOrder {
    ASCENDING, DESCENDING;

    fun toggle(): SortOrder = when (this) {
        ASCENDING -> DESCENDING
        DESCENDING -> ASCENDING
    }

    val displayName: String
        get() = when (this) {
            ASCENDING -> "Ascending"
            DESCENDING -> "Descending"
        }
}

// Programme-based attendance data
@Stable
data class ProgrammeAttendance(
    val programmeId: String,
    val programmeName: String,
    val yearGroups: List<YearGroup>
)

@Stable
data class YearGroup(
    val year: Int,
    val noOfExpectedStudents: Int
)

// Student Attendance with Programme Context
@Stable
data class StudentAttendance(
    // Student Information
    val student: Student,

    // Programme Context
    val programmeId: String,
    val programmeName: String,
    val yearOfStudy: Int,

    // Attendance Details
    val timestamp: String,
    // Verification Status
    val isSuspicious: Boolean = false,
    val suspiciousReason: String? = null
)

// Statistics
@Stable
data class AttendanceStats(
    // Overall Statistics
    val totalExpectedStudents: Int = 0,
    val totalFlagged: Int = 0, // Suspicious
    val totalAttended: Int = 0, // Approved + Flagged
    val totalAbsent: Int = 0, // Expected - Attended

    // Percentages
    val attendancePercentage: Float = 0f,
    val approvedPercentage: Float = 0f,
    val flaggedPercentage: Float = 0f,
    val absentPercentage: Float = 0f,

    // Programme-wise breakdown
    val programmeStats: List<ProgrammeStats> = emptyList()
)

@Stable
data class ProgrammeStats(
    val programmeId: String,
    val programmeName: String,
    val presentCount: Int,
    val flaggedCount: Int,
    val absentCount: Int,
    val totalExpected: Int,
    val attendancePercentage: Float
)

data class Student(
    val id: String = "",
    val name: String = "",
    val registrationNo: String = ""
)

// Connection State
enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    //ERROR
}

// UI Options
data class ProgrammeOption(
    val id: String,
    val name: String,
    val years: List<Int>
)

fun LiveAttendanceState.updateFromSnapshot(snapshot: LiveAttendanceSnapshot): LiveAttendanceState {
    // Convert DTOs to domain models
    val programmes = snapshot.programmes.map { programmeDto ->
        ProgrammeAttendance(
            programmeId = programmeDto.programmeId,
            programmeName = programmeDto.programmeName,
            yearGroups = listOf(YearGroup(
                year = programmeDto.yearOfStudy,
                noOfExpectedStudents = programmeDto.noOfExpectedStudents
            ))
        )
    }

    // Process all students into one list
    val allStudents = mutableListOf<StudentAttendance>()

    snapshot.programmes.forEach { programmeDto ->
        programmeDto.students.forEach { studentDto ->
            val studentAttendance = StudentAttendance(
                student = Student(
                    id = studentDto.studentId,
                    registrationNo = studentDto.regNo,
                    name = studentDto.name
                ),
                programmeId = programmeDto.programmeId,
                programmeName = programmeDto.programmeName,
                yearOfStudy = programmeDto.yearOfStudy,
                timestamp = studentDto.attendedAt,
                isSuspicious = studentDto.isSuspicious,
                suspiciousReason = studentDto.suspiciousReason,
            )

            allStudents.add(studentAttendance)
        }
    }

    // Create updated state
    return this.copy(
        programmes = programmes,
        allStudents = allStudents,
        lastUpdate = getCurrentTime()
    )
}

fun LiveAttendanceState.updateFromAttendanceEvent(event: AttendanceMarkedEventDto): LiveAttendanceState {
    val studentDto = event.student

    // Create student attendance object
    val newStudent = StudentAttendance(
        student = Student(
            id = studentDto.studentId,
            registrationNo = studentDto.regNo,
            name = studentDto.name
        ),
        programmeId = event.programmeId,
        programmeName = programmes.find { it.programmeId == event.programmeId }?.programmeName ?: "Unknown",
        yearOfStudy = programmes.find { it.programmeId == event.programmeId }
            ?.yearGroups?.firstOrNull()?.year ?: 1,
        timestamp = studentDto.attendedAt,
        isSuspicious = studentDto.isSuspicious,
        suspiciousReason = studentDto.suspiciousReason
    )

    // Remove existing student if exists, then add new one
    val updatedAllStudents = allStudents
        .filterNot { it.student.id == studentDto.studentId }
        .toMutableList()
    updatedAllStudents.add(newStudent)

    return this.copy(
        allStudents = updatedAllStudents,
        lastUpdate = getCurrentTime()
    )
}

// Add helper to check if any filters are active
fun LiveAttendanceState.hasActiveFilters(): Boolean {
    return filterOptions.selectedProgrammeId != null || filterOptions.showOnlyFlagged
}

// Add helper to get active filter description
fun LiveAttendanceState.getActiveFilterDescription(): String {
    val filters = mutableListOf<String>()

    filterOptions.selectedProgrammeId?.let { programmeId ->
        programmes.find { it.programmeId == programmeId }?.let { programme ->
            filters.add(programme.programmeName)
        }
    }

    if (filterOptions.showOnlyFlagged) {
        filters.add("Flagged Only")
    }

    return if (filters.isEmpty()) "All Programmes" else filters.joinToString(" • ")
}

// Helper function for time
fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}

// A tip I got from my backend how suspiciousReason string is assigned
//flag types are in an enum {LOCATION_MISMATCH, OUTSIDE_SCHEDULE_WINDOW }
//suspiciousReason = flags.joinToString { it.type.name }
