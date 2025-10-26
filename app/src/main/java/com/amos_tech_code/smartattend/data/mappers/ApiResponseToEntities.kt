package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.domain.models.Programme
import com.amos_tech_code.smartattend.domain.models.UnitModel
import com.amos_tech_code.smartattend.domain.models.University
import com.amos_tech_code.smartattend.domain.models.response.LecturerUniversitiesResponse

fun lecturerUniversitiesResponseToEntities(response: LecturerUniversitiesResponse): Triple<List<UniversityEntity>, List<ProgrammeEntity>, List<UnitEntity>> {
    val universities = mutableListOf<UniversityEntity>()
    val programmes = mutableListOf<ProgrammeEntity>()
    val units = mutableListOf<UnitEntity>()

    response.universities.forEach { uni ->
        universities.add(UniversityEntity(id = uni.id, name = uni.name))
        uni.programmes.forEach { prog ->
            programmes.add(
                ProgrammeEntity(
                    id = prog.id,
                    universityId = uni.id,
                    name = prog.name,
                    department = prog.department,
                    yearOfStudy = prog.yearOfStudy
                )
            )
            prog.units.forEach { unit ->
                units.add(
                    UnitEntity(
                        id = unit.id,
                        programmeId = prog.id,
                        code = unit.code,
                        name = unit.name
                    )
                )
            }
        }
    }
    return Triple(universities, programmes, units)
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
