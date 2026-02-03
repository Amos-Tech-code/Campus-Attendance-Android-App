package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.local.room.dao.EnrollmentDao
import com.amos_tech_code.smartattend.data.mappers.toEntity
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.StudentEnrollmentRequest
import com.amos_tech_code.smartattend.domain.request.UpdateYearRequest
import com.amos_tech_code.smartattend.domain.response.StudentEnrollmentResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnrollmentRepository(
    private val apiService: ApiService,
    private val enrollmentDao: EnrollmentDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun syncActiveEnrollment() : ApiResult<StudentEnrollmentResponse> {

        val result = safeApiCall { apiService.getStudentEnrollment() }

        if (result is ApiResult.Success) {
            try {
                withContext(ioDispatcher) {
                    // Save to local database
                    enrollmentDao.upsert(result.data.toEntity())
                }
            } catch (e: Exception) {
                //e.printStackTrace()
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
                    enrollmentDao.upsert(result.data.toEntity())
                }
            } catch (e: Exception) {
                //e.printStackTrace()
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
                    enrollmentDao.upsert(result.data.toEntity())
                }
            } catch (e: Exception) {
                //e.printStackTrace()
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
            } catch (e: Exception) {
                //e.printStackTrace()
            }
        }

        return result
    }


}