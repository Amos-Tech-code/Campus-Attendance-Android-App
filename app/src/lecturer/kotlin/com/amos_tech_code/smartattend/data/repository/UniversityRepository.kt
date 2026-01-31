package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.models.UniversityStatistics
import com.amos_tech_code.smartattend.models.UniversityWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UniversityRepository(
    private val lecturerAcademicsDao: LecturerAcademicsDao
) {

    fun observeActiveUniversities(): Flow<List<UniversityEntity>> {
        return lecturerAcademicsDao.observeAllUniversities()
            .map { universities -> universities.filter { it.isActive } }
    }

    fun observeActiveUniversity(): Flow<UniversityEntity?> {
        return lecturerAcademicsDao.observeActiveUniversity()
    }

    suspend fun getUniversityStatistics(universityId: String): UniversityStatistics {
        return lecturerAcademicsDao.getUniversityStatistics(universityId)
    }

    fun observeUniversityStatistics(universityId: String): Flow<UniversityStatistics> {
        return lecturerAcademicsDao.observeUniversityStatistics(universityId)
    }

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

    fun observeUnitsForUniversity(universityId: String): Flow<List<UnitEntity>> {
        return lecturerAcademicsDao.observeUnitsForUniversity(universityId)
    }
}