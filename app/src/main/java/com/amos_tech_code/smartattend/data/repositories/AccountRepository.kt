package com.amos_tech_code.smartattend.data.repositories

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