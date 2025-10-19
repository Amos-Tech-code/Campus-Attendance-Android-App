package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.request.AcademicSetupUpRequest
import com.amos_tech_code.smartattend.domain.models.response.AcademicSetupResponse

class AcademicSetUpRepository(
    private val apiService: ApiService
) {

    suspend fun uploadAcademicSetUp(request: AcademicSetupUpRequest) : ApiResult<AcademicSetupResponse> {

        return safeApiCall { apiService.uploadAcademicSetup(request) }

    }

}