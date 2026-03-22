package com.amos_tech_code.smartattend.data.repository

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.StudentLookupRequest
import com.amos_tech_code.smartattend.domain.response.StudentLookupResponse

class StudentLookupRepository(
    private val apiService: ApiService
) {

    suspend fun studentLookup(request: StudentLookupRequest) : ApiResult<StudentLookupResponse> {
        return safeApiCall {
            apiService.lookupStudent(request)
        }
    }
}