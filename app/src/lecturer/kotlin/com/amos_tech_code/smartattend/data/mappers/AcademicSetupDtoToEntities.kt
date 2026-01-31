package com.amos_tech_code.smartattend.data.mappers

import com.amos_tech_code.smartattend.data.local.room_db.entities.DepartmentEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.domain.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.LecturerAcademicSetupResponse

/**
 *Function to convert API response to database entities
 */
fun lecturerUniversitiesResponseToEntities(response: LecturerAcademicSetupResponse):
        Triple<List<UniversityEntity>, List<ProgrammeEntity>, List<UnitEntity>> {

    val universities = mutableListOf<UniversityEntity>()
    val programmes = mutableListOf<ProgrammeEntity>()
    val units = mutableListOf<UnitEntity>()

    response.universities.forEach { universitySetup ->
        // University
        universities.add(
            UniversityEntity(
                id = universitySetup.university.id,
                name = universitySetup.university.name,
                isActive = false // Will be set based on active logic
            )
        )

        // Departments and Programmes
        universitySetup.programmes.forEach { programmeSetup ->
            // Programme
            programmes.add(
                ProgrammeEntity(
                    id = programmeSetup.programme.id,
                    universityId = universitySetup.university.id,
                    departmentId = programmeSetup.department.id,
                    name = programmeSetup.programme.name,
                    yearOfStudy = programmeSetup.yearOfStudy,
                    expectedStudentCount = programmeSetup.expectedStudentCount
                )
            )

            // Units
            programmeSetup.units.forEach { unitSetup ->
                units.add(
                    UnitEntity(
                        id = unitSetup.unitId,
                        universityId = universitySetup.university.id,
                        code = unitSetup.code,
                        name = unitSetup.name,
                        semester = unitSetup.semester,
                        lectureDay = unitSetup.lectureDay,
                        lectureTime = unitSetup.lectureTime,
                        lectureVenue = unitSetup.lectureVenue
                    )
                )
            }
        }
    }

    return Triple(universities, programmes, units)
}

/**
 * Function to convert single university setup response to entities
 */
fun academicSetupResponseToEntities(response: AcademicSetupResponse, universityId: String? = null):
        Pair<UniversityEntity, Triple<List<DepartmentEntity>, List<ProgrammeEntity>, List<UnitEntity>>> {

    val actualUniversityId = universityId ?: response.university.id

    // University
    val university = UniversityEntity(
        id = actualUniversityId,
        name = response.university.name,
        isActive = response.isActive
    )

    // Collect all departments from programmes
    val departments = mutableListOf<DepartmentEntity>()
    val programmes = mutableListOf<ProgrammeEntity>()
    val units = mutableListOf<UnitEntity>()

    // First pass: collect departments
    response.programmes.forEach { programmeResponse ->
        departments.add(
            DepartmentEntity(
                id = programmeResponse.departmentId,
                universityId = actualUniversityId,
                name = programmeResponse.departmentName
            )
        )
    }

    // Second pass: create programmes and units
    response.programmes.forEach { programmeResponse ->
        // Programme
        programmes.add(
            ProgrammeEntity(
                id = programmeResponse.programmeId,
                universityId = actualUniversityId,
                departmentId = programmeResponse.departmentId,
                name = programmeResponse.programmeName,
                yearOfStudy = programmeResponse.yearOfStudy,
                expectedStudentCount = programmeResponse.expectedStudentCount
            )
        )

        // Units
        programmeResponse.units.forEach { unitResponse ->
            units.add(
                UnitEntity(
                    id = unitResponse.unitId,
                    universityId = actualUniversityId,
                    code = unitResponse.code,
                    name = unitResponse.name,
                    semester = unitResponse.semester,
                    lectureDay = unitResponse.lectureDay,
                    lectureTime = unitResponse.lectureTime,
                    lectureVenue = unitResponse.lectureVenue
                )
            )
        }
    }

    return Pair(university, Triple(departments.distinctBy { it.id }, programmes, units))

}