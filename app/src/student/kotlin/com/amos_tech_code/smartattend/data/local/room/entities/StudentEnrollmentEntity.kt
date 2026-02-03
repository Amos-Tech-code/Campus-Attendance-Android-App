package com.amos_tech_code.smartattend.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amos_tech_code.smartattend.domain.models.StudentEnrollmentSource

@Entity(tableName = "student_enrollment")
data class StudentEnrollmentEntity(
    @PrimaryKey
    val enrollmentId: String,
    val registrationNumber: String,
    val fullName: String,

    @Embedded(prefix = "university_")
    val university: UniversityEmbedded,

    @Embedded(prefix = "programme_")
    val programme: ProgrammeEmbedded,

    @Embedded(prefix = "term_")
    val academicTerm: AcademicTermEmbedded,

    val yearOfStudy: Int,
    val enrollmentDate: Long,
    val enrollmentSource: StudentEnrollmentSource,
    val isActive: Boolean
)


/**
 * Embedded classes for StudentEnrollmentEntity
 * to represent the nested objects
 */
data class UniversityEmbedded(
    @ColumnInfo(name = "university_id") val id: String,
    @ColumnInfo(name = "university_name") val name: String
)

data class ProgrammeEmbedded(
    @ColumnInfo(name = "programme_id") val id: String,
    @ColumnInfo(name = "programme_name") val name: String
)

data class AcademicTermEmbedded(
    @ColumnInfo(name = "term_id") val id: String,
    @ColumnInfo(name = "academic_year") val academicYear: String,
    val semester: Int,
    @ColumnInfo(name = "term_active") val isActive: Boolean
)

