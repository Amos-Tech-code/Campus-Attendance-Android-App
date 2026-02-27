package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.local.room.dao.EnrollmentDao
import com.amos_tech_code.smartattend.data.local.room.entities.StudentEnrollmentEntity
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.mappers.toEntity
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.UniversitySuggestionsRepository
import com.amos_tech_code.smartattend.domain.request.ProgrammeSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.StudentEnrollmentRequest
import com.amos_tech_code.smartattend.domain.request.UniversitySuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UpdateYearRequest
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.StudentEnrollmentResponse
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EnrollmentRepository(
    private val apiService: ApiService,
    private val enrollmentDao: EnrollmentDao,
    private val session: ClassTrackSession,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): UniversitySuggestionsRepository {

    /**
     * Fetches a list of university suggestions from the network based on a search query.
     * @param request The request containing the search query and limit.
     * @return An [com.amos_tech_code.smartattend.data.network.utils.ApiResult] containing a list of [com.amos_tech_code.smartattend.domain.response.UniversitySuggestion] on success, or an error on failure.
     */
    override suspend fun fetchMatchingUniversities(request: UniversitySuggestionRequest): ApiResult<List<UniversitySuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingUniversities(
                query = request.query,
                limit = request.limit
            )
        }
    }

    /**
     * Fetches a list of programme suggestions from the network for a given university and department.
     * @param request The request containing university ID, optional department ID, search query, and limit.
     * @return An [ApiResult] containing a list of [com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion] on success, or an error on failure.
     */
    override suspend fun fetchMatchingProgrammes(request: ProgrammeSuggestionRequest): ApiResult<List<ProgrammeSuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingProgrammes(
                universityId = request.universityId,
                departmentId = request.departmentId,
                query = request.query,
                limit = request.limit
            )
        }
    }

    suspend fun syncActiveEnrollment() : ApiResult<StudentEnrollmentResponse> {

        val result = safeApiCall { apiService.getStudentEnrollment() }

        if (result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    // Save to local database
                    enrollmentDao.upsertEnsuringSingleActive(result.data.toEntity())
                }
                session.setEnrollmentSyncStatus(true)
            } catch (_: Exception) {
                session.setEnrollmentSyncStatus(false)
            }
        }

        return result
    }

    suspend fun enroll(request: StudentEnrollmentRequest) : ApiResult<StudentEnrollmentResponse> {

        val result = safeApiCall { apiService.enrollStudent(request) }

        if (result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    // Save to local database
                    enrollmentDao.upsertEnsuringSingleActive(result.data.toEntity())
                }
            } catch (_: Exception) {
                session.setEnrollmentSyncStatus(false)
            }
        }

        return result
    }

    suspend fun updateYear(enrollmentId: String, newYearOfStudy: Int) : ApiResult<StudentEnrollmentResponse> {

        val result = safeApiCall {
            apiService.updateStudentEnrollment(
                enrollmentId = enrollmentId,
                request = UpdateYearRequest(newYearOfStudy)
            )
        }

        if (result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    // Update local database
                    enrollmentDao.upsertEnsuringSingleActive(result.data.toEntity())
                }
            } catch (_: Exception) {
                session.setEnrollmentSyncStatus(false)
            }
        }

        return result

    }

    suspend fun deactivateEnrollment(enrollmentId: String) : ApiResult<Unit> {

        val result = safeApiCall { apiService.deleteStudentEnrollment(enrollmentId) }

        if (result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    // Update to local database
                    enrollmentDao.deleteById(enrollmentId)
                }
            } catch (_: Exception) {
                session.setEnrollmentSyncStatus(false)
            }
        }

        return result
    }

    fun getActiveEnrollmentFlow(): Flow<StudentEnrollmentEntity?> {
        return enrollmentDao.getActiveEnrollment()
    }


}