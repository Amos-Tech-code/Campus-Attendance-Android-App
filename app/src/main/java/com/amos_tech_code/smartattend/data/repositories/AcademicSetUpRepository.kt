package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.data.mappers.lecturerUniversitiesResponseToEntities
import com.amos_tech_code.smartattend.data.mappers.toDomain
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.University
import com.amos_tech_code.smartattend.domain.models.request.AcademicSetupUpRequest
import com.amos_tech_code.smartattend.domain.models.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.models.response.LecturerUniversitiesResponse

class AcademicSetUpRepository(
    private val apiService: ApiService,
    private val session: SmartAttendSession,
    private val lecturerAcademicsDao: LecturerAcademicsDao
) {

    suspend fun uploadAcademicSetUp(request: AcademicSetupUpRequest) : ApiResult<AcademicSetupResponse> {

        return safeApiCall { apiService.uploadAcademicSetup(request) }

    }

    suspend fun fetchLecturerAcademicSetUp() : ApiResult<LecturerUniversitiesResponse> {

        return safeApiCall { apiService.fetchLecturerAcademicSetUp() }

    }

    suspend fun getAllAcademicsForLecturer(): List<University> {
        // Sync if not done yet
        if (!session.getAcademicSyncStatus()) {
            syncLecturerAcademics()
        }

        // Fetch all from Room
        val dbUniversities = lecturerAcademicsDao.getUniversitiesWithProgrammesAndUnits()

        // Map to domain models
        val domainUniversities = dbUniversities.map { it.toDomain() }

        return domainUniversities
    }

    suspend fun getActiveUniversityAcademics(): University? {
        val allUniversities = getAllAcademicsForLecturer()
        val activeUniversities = allUniversities.find { it.isActive }

        return if (allUniversities.size == 1) {
            allUniversities.first()
        } else if (allUniversities.isNotEmpty() && activeUniversities != null) {
            activeUniversities
        } else {
            allUniversities.firstOrNull()
        }

    }

    suspend fun setActiveUniversity(universityId: String) {
        lecturerAcademicsDao.setActiveUniversity(universityId)
    }

    suspend fun getUniversitiesForSelection(): List<University> {
        val universities = lecturerAcademicsDao.getAllUniversities()
        return universities.map { universityEntity ->
            University(
                id = universityEntity.id,
                name = universityEntity.name,
                isActive = universityEntity.isActive
            )
        }
    }


    suspend fun syncLecturerAcademics() {

        val result = fetchLecturerAcademicSetUp()

        when (result) {
            is ApiResult.Success -> {
                val (universities, programmes, units) = lecturerUniversitiesResponseToEntities(result.data)
                lecturerAcademicsDao.clearAll()
                lecturerAcademicsDao.insertFullHierarchy(universities, programmes, units)
                session.setAcademicSyncStatus(true)
            }

            is ApiResult.Failure ->  {
                session.setAcademicSyncStatus(false)
            }
        }


    }


}