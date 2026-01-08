package com.amos_tech_code.smartattend.domain.models

enum class AttendanceMethod {
    QR_CODE, // Qr Code scan
    MANUAL_CODE, // Manually entering session code if allowed
    ANY, // QR Code and Manually entering session code if allowed
    //LECTURER_MANUAL // Lecturer manually signing for student
}

enum class AttendanceSessionStatus {
    SCHEDULED, ACTIVE, ENDED, CANCELLED, EXPIRED
}

enum class AttendanceSessionType {
    REGULAR, MAKEUP, SPECIAL
}

enum class FlagType {
    LOCATION_MISMATCH, DEVICE_MISMATCH,
    METHOD_NOT_ALLOWED, SUSPICIOUS_DEVICE, OUTSIDE_SCHEDULE_WINDOW
}

enum class SeverityLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class StudentEnrollmentSource {
    ATTENDANCE, SELF, MANUAL
}

enum class LiveAttendanceEventType {
    INITIAL_STATE, ATTENDANCE_MARKED
}

enum class LocationPermissionState {
    GRANTED, DENIED_SHOW_RATIONALE, DENIED_NEVER_ASK
}