package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeWithUnits
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.models.UniversityWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UniversityRepository(
    private val lecturerAcademicsDao: LecturerAcademicsDao
) {
    /**
     * Observes all universities with their statistics.
     * @return A Flow of all universities with their statistics.
     */
    fun observeAllUniversitiesWithStats(): Flow<List<UniversityWithStats>> {
        return lecturerAcademicsDao.observeAllUniversities().map { universities ->
            universities.map { university ->
                UniversityWithStats(
                    university = university,
                    statistics = lecturerAcademicsDao.getUniversityStatistics(university.id)
                )
            }
        }
    }


    /**
     * Marks a specific university as the active one in the local database.
     * All other universities will be marked as inactive.
     * @param universityId The ID of the university to set as active.
     */
    suspend fun setActiveUniversity(universityId: String) {
        lecturerAcademicsDao.setActiveUniversity(universityId)
    }

    suspend fun getActiveUniversity(): UniversityEntity? = lecturerAcademicsDao.getActiveUniversity()

    suspend fun getActiveAcademicTerm(universityId: String): AcademicTermEntity? =
        lecturerAcademicsDao.getActiveAcademicTermForUniversity(universityId)

    suspend fun getUniversitiesWithProgrammesAndUnits(): List<UniversityWithProgrammesAndUnits> =
        lecturerAcademicsDao.getUniversitiesWithProgrammesAndUnits()

    suspend fun getProgrammesForUniversity(universityId: String): List<ProgrammeWithUnits> {
        return lecturerAcademicsDao.getUniversityWithProgrammesAndUnits(universityId)?.programmes ?: emptyList()
    }

    fun getUnitsForProgramme(programmeId: String, programmes: List<ProgrammeWithUnits>): List<UnitEntity> {
        return programmes.find { it.programme.id == programmeId }?.units ?: emptyList()
    }

    suspend fun getAllUnitsForUniversity(universityId: String): List<UnitEntity> =
        lecturerAcademicsDao.getAllUnitsForUniversity(universityId)

}