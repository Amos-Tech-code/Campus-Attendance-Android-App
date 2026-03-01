package com.amos_tech_code.smartattend.domain.models

/**
 * Represents the method used for marking attendance.
 */
enum class AttendanceMethod {
    QR_CODE, // Qr Code scan
    MANUAL_CODE, // Manually entering session code if allowed
    ANY, // QR Code and Manually entering session code if allowed
    //LECTURER_MANUAL // Lecturer manually signing for student
}

/**
 * Represents the status of an attendance session.
 */
enum class AttendanceSessionStatus {
    SCHEDULED, ACTIVE, ENDED, CANCELLED
}

/**
 * Represents the type of an attendance session.
 */
enum class AttendanceSessionType {
    REGULAR, MAKEUP, SPECIAL
}

/**
 * Represents the format of an attendance export record.
 */
enum class ExportFormat {
    PDF, CSV
}

/**
 * Represents the type of a flag for flagged student.
 */
enum class FlagType {
    LOCATION_MISMATCH, DEVICE_MISMATCH,
    METHOD_NOT_ALLOWED, SUSPICIOUS_DEVICE, OUTSIDE_SCHEDULE_WINDOW
}

enum class SeverityLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Represents the source of enrollment for a student.
 */
enum class StudentEnrollmentSource {
    ATTENDANCE, SELF, MANUAL
}

/**
 * Represents device status change request/registration status.
 */
enum class DeviceStatus {
    ACTIVE,           // Registered and approved device
    PENDING,          // Waiting for approval
    REJECTED         // Rejected device change
}

/**
 * Represents Notification type
 */
enum class NotificationType {
    ATTENDANCE_MARKED,
    ATTENDANCE_REVOKED,
    DEVICE_APPROVED,
    DEVICE_REJECTED,
    DEVICE_REQUEST,
    SESSION_STARTED,
    SESSION_ENDED,
    SUSPICIOUS_ACTIVITY,
    SUPPORT_RESPONSE,
    SYSTEM_ALERT,
    //ADMIN_ALERT
}