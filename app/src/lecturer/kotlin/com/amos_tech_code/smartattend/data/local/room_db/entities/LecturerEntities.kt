package com.amos_tech_code.smartattend.data.local.room_db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "universities")
data class UniversityEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isActive: Boolean = false
)

@Entity(tableName = "academic_terms")
data class AcademicTermEntity(
    @PrimaryKey
    val id: String,
    val universityId: String,
    val academicYear: String,
    val semester: Int,
    val isActive: Boolean = false
)

@Entity(
    tableName = "departments",
    foreignKeys = [
        ForeignKey(
            entity = UniversityEntity::class,
            parentColumns = ["id"],
            childColumns = ["universityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("universityId")]
)
data class DepartmentEntity(
    @PrimaryKey
    val id: String,
    val universityId: String,
    val name: String
)

@Entity(
    tableName = "programmes",
    foreignKeys = [
        ForeignKey(
            entity = UniversityEntity::class,
            parentColumns = ["id"],
            childColumns = ["universityId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DepartmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("universityId"),
        Index("departmentId")
    ]
)
data class ProgrammeEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val departmentId: String,
    val name: String,
    val yearOfStudy: Int,
    val expectedStudentCount: Int
)

@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = UniversityEntity::class,
            parentColumns = ["id"],
            childColumns = ["universityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("universityId")]
)
data class UnitEntity(
    @PrimaryKey
    val id: String,
    val universityId: String,
    val code: String,
    val name: String,
    val semester: Int,
    val lectureDay: String? = null,
    val lectureTime: String? = null,
    val lectureVenue: String? = null
)

@Entity(
    tableName = "programme_units",
    primaryKeys = ["programmeId", "unitId"],
    foreignKeys = [
        ForeignKey(
            entity = ProgrammeEntity::class,
            parentColumns = ["id"],
            childColumns = ["programmeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unitId")]
)
data class ProgrammeUnitCrossRef(
    val programmeId: String,
    val unitId: String
)


// One-to-Many: Programme to Units
data class ProgrammeWithUnits(
    @Embedded val programme: ProgrammeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProgrammeUnitCrossRef::class,
            parentColumn = "programmeId",
            entityColumn = "unitId"
        )
    )
    val units: List<UnitEntity> = emptyList()
)

// One-to-Many: University to Programmes with Units
data class UniversityWithProgrammesAndUnits(
    @Embedded val university: UniversityEntity,
    @Relation(
        entity = ProgrammeEntity::class,
        parentColumn = "id",
        entityColumn = "universityId"
    )
    val programmes: List<ProgrammeWithUnits> = emptyList()
)
