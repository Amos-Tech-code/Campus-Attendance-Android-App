package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.GoogleSignInRequest
import com.amos_tech_code.smartattend.domain.request.StudentLoginRequest
import com.amos_tech_code.smartattend.domain.request.StudentRegisterRequest
import com.amos_tech_code.smartattend.domain.response.LecturerAuthResponse
import com.amos_tech_code.smartattend.domain.response.StudentAuthResponse

class AuthRepository(
    private val apiService: ApiService
) {

    /**
     * Lecturer Authentication with Google Impl
     */
    suspend fun googleSignIn(idToken: String): ApiResult<LecturerAuthResponse> {

        return safeApiCall { apiService.googleSignIn(
            GoogleSignInRequest(idToken)
        ) }
    }


    /**
     * Student Authentication Implementation
     */

    suspend fun studentLogin(request: StudentLoginRequest): ApiResult<StudentAuthResponse> {
        return safeApiCall { apiService.studentLogin(request) }
    }

    suspend fun studentRegister(request: StudentRegisterRequest): ApiResult<StudentAuthResponse> {
        return safeApiCall { apiService.studentRegister(request) }
    }

}