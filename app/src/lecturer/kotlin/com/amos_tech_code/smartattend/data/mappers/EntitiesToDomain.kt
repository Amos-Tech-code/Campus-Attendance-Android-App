package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.domain.models.University

/**
 *Extension functions for converting entities to domain models
 */
// Convert UniversityWithProgrammesAndUnits to University domain model
fun UniversityWithProgrammesAndUnits.toDomain(): University {
    return University(
        id = university.id,
        name = university.name,
        isActive = university.isActive,
        programmes = programmes.map { it.toDomain() }
    )
}

// Convert ProgrammeWithUnits to Programme domain model
fun ProgrammeWithUnits.toDomain(): Programme {
    return Programme(
        id = programme.id,
        name = programme.name,
        departmentId = programme.departmentId,
        departmentName = "", // You'll need to fetch department name separately
        yearOfStudy = programme.yearOfStudy,
        expectedStudentCount = programme.expectedStudentCount,
        units = units.map { it.toDomain() }
    )
}

// Convert UnitEntity to Unit domain model
fun UnitEntity.toDomain(): UnitModel {
    return UnitModel(
        id = id,
        code = code,
        name = name,
        semester = semester,
        lectureDay = lectureDay,
        lectureTime = lectureTime,
        lectureVenue = lectureVenue
    )
}

// Convert UniversityEntity to University domain model (without programmes)
fun UniversityEntity.toDomain(): University {
    return University(
        id = id,
        name = name,
        isActive = isActive,
        programmes = emptyList()
    )
}

// Convert ProgrammeEntity to Programme domain model (without units)
fun ProgrammeEntity.toDomain(): Programme {
    return Programme(
        id = id,
        name = name,
        departmentId = departmentId,
        departmentName = "", // You'll need to fetch department name separately
        yearOfStudy = yearOfStudy,
        expectedStudentCount = expectedStudentCount,
        units = emptyList()
    )
}