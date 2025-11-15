package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeUnitCrossRef
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.domain.models.University
import com.amos_tech_code.smartattend.domain.response.LecturerUniversitiesResponse

fun lecturerUniversitiesResponseToEntities(response: LecturerUniversitiesResponse):
        Triple<List<UniversityEntity>, List<ProgrammeEntity>, List<UnitEntity>> {

    val universities = mutableListOf<UniversityEntity>()
    val programmes = mutableListOf<ProgrammeEntity>()
    val units = mutableSetOf<UnitEntity>() // Use Set to avoid duplicates

    response.universities.forEach { university ->
        universities.add(
            UniversityEntity(
                id = university.id,
                name = university.name,
                isActive = false
            )
        )

        university.programmes.forEach { programme ->
            programmes.add(
                ProgrammeEntity(
                    id = programme.id,
                    universityId = university.id,
                    name = programme.name,
                    department = programme.department,
                    yearOfStudy = programme.yearOfStudy
                )
            )

            programme.units.forEach { unit ->
                units.add(
                    UnitEntity(
                        id = unit.id,
                        code = unit.code,
                        name = unit.name
                    )
                )
            }
        }
    }

    return Triple(universities, programmes, units.toList())
}

// Function to create programme-unit relationships
fun createProgrammeUnitRelationships(response: LecturerUniversitiesResponse): List<ProgrammeUnitCrossRef> {
    val crossRefs = mutableListOf<ProgrammeUnitCrossRef>()

    response.universities.forEach { university ->
        university.programmes.forEach { programme ->
            programme.units.forEach { unit ->
                crossRefs.add(
                    ProgrammeUnitCrossRef(
                        programmeId = programme.id,
                        unitId = unit.id
                    )
                )
            }
        }
    }

    return crossRefs
}


fun UniversityWithProgrammesAndUnits.toDomain(): University {
    return University(
        id = university.id,
        name = university.name,
        isActive = university.isActive,
        programmes = programmes.map { it.toDomain() }
    )
}

fun ProgrammeWithUnits.toDomain(): Programme {
    return Programme(
        id = programme.id,
        name = programme.name,
        department = programme.department,
        yearOfStudy = programme.yearOfStudy,
        units = units.map { it.toDomain() }
    )
}

fun UnitEntity.toDomain(): UnitModel {
    return UnitModel(
        id = id,
        code = code,
        name = name
    )
}
