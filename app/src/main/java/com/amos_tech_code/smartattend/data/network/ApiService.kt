package com.amos_tech_code.smartattend.data.network

import com.amos_tech_code.smartattend.domain.models.request.AcademicSetupUpRequest
import com.amos_tech_code.smartattend.domain.models.request.GoogleSignInRequest
import com.amos_tech_code.smartattend.domain.models.request.StudentLoginRequest
import com.amos_tech_code.smartattend.domain.models.request.StudentRegisterRequest
import com.amos_tech_code.smartattend.domain.models.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.models.response.LecturerAuthResponse
import com.amos_tech_code.smartattend.domain.models.response.StudentAuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    /**
     * Lecturer Flavor Api Service Implementation
     */
    @POST("auth/lecturers/google")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<LecturerAuthResponse>

    @POST("/academic-setup")
    suspend fun uploadAcademicSetup(@Body request: AcademicSetupUpRequest): Response<AcademicSetupResponse>




    /**
     * Student Flavor Api Service Implementation
     *
     */
    @POST("auth/students/login")
    suspend fun studentLogin(@Body request: StudentLoginRequest): Response<StudentAuthResponse>

    @POST("auth/students/register")
    suspend fun studentRegister(@Body request: StudentRegisterRequest): Response<StudentAuthResponse>


}