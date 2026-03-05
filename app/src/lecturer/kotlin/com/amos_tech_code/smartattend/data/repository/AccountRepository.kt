package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.local.room_db.dao.AttendanceExportDao
import com.amos_tech_code.smartattend.data.local.room_db.dao.AttendanceSessionHistoryDao
import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.UpdateLecturerProfileRequest
import com.amos_tech_code.smartattend.domain.response.GenericResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AccountRepository(
    private val apiService: ApiService,
    private val session: ClassTrackProSession,
    private val attendanceExportDao: AttendanceExportDao,
    private val lecturerAcademicsDao: LecturerAcademicsDao,
    private val attendanceSessionHistoryDao: AttendanceSessionHistoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {

    suspend fun updateLecturerProfile(request: UpdateLecturerProfileRequest): ApiResult<GenericResponse> {

        val result = safeApiCall { apiService.updateLecturerProfile(request) }

        if (result is ApiResult.Success) {
            session.saveName(request.fullName)
        }
        return result

    }

    suspend fun logOut() {
        withContext(ioDispatcher) {
            attendanceExportDao.clearAll()
            attendanceSessionHistoryDao.clearAll()
            lecturerAcademicsDao.clearAll()
            session.clearSession()
        }
    }

}