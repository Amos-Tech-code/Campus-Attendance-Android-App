package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import com.amos_tech_code.smartattend.data.mappers.createProgrammeUnitRelationships
import com.amos_tech_code.smartattend.data.mappers.lecturerUniversitiesResponseToEntities
import com.amos_tech_code.smartattend.data.mappers.toDomain
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.University
import com.amos_tech_code.smartattend.domain.request.AcademicSetUpRequest
import com.amos_tech_code.smartattend.domain.request.DepartmentSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.ProgrammeSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UnitSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UniversitySuggestionRequest
import com.amos_tech_code.smartattend.domain.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.DepartmentSuggestion
import com.amos_tech_code.smartattend.domain.response.LecturerAcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UnitSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion

class AcademicSetUpRepository(
    private val apiService: ApiService,
    private val session: SmartAttendSession,
    private val lecturerAcademicsDao: LecturerAcademicsDao
) {

    suspend fun uploadAcademicSetUp(request: AcademicSetUpRequest) : ApiResult<AcademicSetupResponse> {

        return safeApiCall { apiService.uploadAcademicSetup(request) }

    }

    suspend fun fetchLecturerAcademicSetUp() : ApiResult<LecturerAcademicSetupResponse> {

        return safeApiCall { apiService.fetchLecturerAcademicSetUp() }

    }

    /**
     * Local Data source Operations
     */
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

    suspend fun getUniversities() : List<University> {
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
                val programmeUnits = createProgrammeUnitRelationships(result.data)

                lecturerAcademicsDao.clearAll()
                lecturerAcademicsDao.insertFullHierarchy(universities, programmes, units, programmeUnits)
                session.setAcademicSyncStatus(true)
            }

            is ApiResult.Failure -> {
                session.setAcademicSyncStatus(false)
            }
        }
    }


    /**
     * Academic set up suggestions
     */
    suspend fun fetchMatchingUniversities(request: UniversitySuggestionRequest): ApiResult<List<UniversitySuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingUniversities(
                query = request.query,
                limit = request.limit
            )
        }
    }

    suspend fun fetchMatchingDepartments(request: DepartmentSuggestionRequest): ApiResult<List<DepartmentSuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingDepartments(
                universityId = request.universityId,
                query = request.query,
                limit = request.limit
            )
        }
    }

    suspend fun fetchMatchingProgrammes(request: ProgrammeSuggestionRequest): ApiResult<List<ProgrammeSuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingProgrammes(
                universityId = request.universityId,
                departmentId = request.departmentId,
                query = request.query,
                limit = request.limit
            )
        }
    }

    suspend fun fetchMatchingUnits(request: UnitSuggestionRequest): ApiResult<List<UnitSuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingUnits(
                universityId = request.universityId,
                departmentId = request.departmentId,
                programmeId = request.programmeId,
                query = request.query,
                limit = request.limit
            )
        }
    }

}
