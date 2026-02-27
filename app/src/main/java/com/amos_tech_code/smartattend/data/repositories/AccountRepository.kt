package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.local.SessionProvider
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.UpdateLecturerProfileRequest
import com.amos_tech_code.smartattend.domain.request.UpdateStudentProfileRequest
import com.amos_tech_code.smartattend.domain.response.GenericResponse

/*
class AccountRepository(
    private val apiService: ApiService,
    private val sessionProvider: SessionProvider
) {

    suspend fun updateLecturerProfile(request: UpdateLecturerProfileRequest): ApiResult<GenericResponse> {

        val result = safeApiCall { apiService.updateLecturerProfile(request) }

        if (result is ApiResult.Success) {
            sessionProvider.saveName(request.fullName)
        }
        return result

    }

    suspend fun updateStudentProfile(request: UpdateStudentProfileRequest): ApiResult<GenericResponse> {

        val result = safeApiCall { apiService.updateStudentProfile(request) }

        if (result is ApiResult.Success) {
            sessionProvider.saveName(request.fullName)
            sessionProvider.saveRegistrationNumber(request.registrationNumber)
        }
        return result
    }

}

 */