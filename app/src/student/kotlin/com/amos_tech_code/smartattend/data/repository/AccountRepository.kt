package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.local.room.dao.AttendanceDao
import com.amos_tech_code.smartattend.data.local.room.dao.EnrollmentDao
import com.amos_tech_code.smartattend.data.local.room.dao.StudentAttendanceStatsDao
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackSession
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.UpdateStudentProfileRequest
import com.amos_tech_code.smartattend.domain.response.GenericResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountRepository(
    private val apiService: ApiService,
    private val session: ClassTrackSession,
    private val attendanceStatsDao: StudentAttendanceStatsDao,
    private val enrollmentDao: EnrollmentDao,
    private val attendanceDao: AttendanceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun updateStudentProfile(request: UpdateStudentProfileRequest): ApiResult<GenericResponse> {

        val result = safeApiCall { apiService.updateStudentProfile(request) }

        if (result is ApiResult.Success) {
            session.saveName(request.fullName)
            session.saveRegistrationNumber(request.registrationNumber)
        }
        return result
    }


    suspend fun logOut() {
        session.clearSession()
        withContext(ioDispatcher) {
            attendanceStatsDao.clearAll()
            enrollmentDao.clear()
            attendanceDao.clearAll()
        }

    }


}