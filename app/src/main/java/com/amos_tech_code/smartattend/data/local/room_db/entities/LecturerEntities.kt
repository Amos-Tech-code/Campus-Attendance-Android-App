package com.amos_tech_code.smartattend.data.local.room_db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

// University Entity
@Entity(tableName = "universities")
data class UniversityEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isActive: Boolean = false // true for current active university
)


// Programme Entity
@Entity(
    tableName = "programmes",
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
data class ProgrammeEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val name: String,
    val department: String,
    val yearOfStudy: Int
)


// Unit Entity
@Entity(
    tableName = "units"
)
data class UnitEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val name: String
)


// Junction table for Programme-Unit many-to-many relationship
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


/**
 * One-to-Many Relationships
 */
// One-to-Many: University to Programmes
data class UniversityWithProgrammes(
    @Embedded val university: UniversityEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "universityId"
    )
    val programmes: List<ProgrammeEntity>
)


// One-to-Many: Programme to Units (using junction table)
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

// One-to-Many: Unit to Programmes (if needed)
data class UnitWithProgrammes(
    @Embedded val unit: UnitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProgrammeUnitCrossRef::class,
            parentColumn = "unitId",
            entityColumn = "programmeId"
        )
    )
    val programmes: List<ProgrammeEntity>
)

// Complete hierarchy
data class UniversityWithProgrammesAndUnits(
    @Embedded val university: UniversityEntity,
    @Relation(
        entity = ProgrammeEntity::class,
        parentColumn = "id",
        entityColumn = "universityId"
    )
    val programmes: List<ProgrammeWithUnits> = emptyList()
)
