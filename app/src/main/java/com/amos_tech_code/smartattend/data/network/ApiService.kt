package com.amos_tech_code.smartattend.data.network

import com.amos_tech_code.smartattend.domain.models.request.AcademicSetupUpRequest
import com.amos_tech_code.smartattend.domain.models.request.EndSessionRequest
import com.amos_tech_code.smartattend.domain.models.request.GoogleSignInRequest
import com.amos_tech_code.smartattend.domain.models.request.StartSessionRequest
import com.amos_tech_code.smartattend.domain.models.request.StudentLoginRequest
import com.amos_tech_code.smartattend.domain.models.request.StudentRegisterRequest
import com.amos_tech_code.smartattend.domain.models.request.UpdateSessionRequest
import com.amos_tech_code.smartattend.domain.models.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.models.response.LecturerAuthResponse
import com.amos_tech_code.smartattend.domain.models.response.LecturerUniversitiesResponse
import com.amos_tech_code.smartattend.domain.models.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.domain.models.response.StudentAuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    /**
     * Lecturer Flavor Api Service Implementation
     */
    @POST("auth/lecturers/google")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<LecturerAuthResponse>

    @POST("lecturer/academic-setup")
    suspend fun uploadAcademicSetup(@Body request: AcademicSetupUpRequest): Response<AcademicSetupResponse>

    @GET("lecturer/academic-setup")
    suspend fun fetchLecturerAcademicSetUp(): Response<LecturerUniversitiesResponse>

    @POST("attendance/sessions/start")
    suspend fun startAttendanceSession(@Body request: StartSessionRequest) : Response<StartAttendanceSessionResponse>

    @PATCH("attendance/sessions/{sessionId}")
    suspend fun updateAttendanceSession(
        @Path("sessionId") sessionId: String,
        @Body request: UpdateSessionRequest
    ) : Response<StartAttendanceSessionResponse>

    @POST("attendance/sessions/end")
    fun endAttendanceSession(@Body request: EndSessionRequest) : Response<Unit>

    @GET("attendance/sessions/active")
    fun getActiveSession() : Response<StartAttendanceSessionResponse>


    /**
     * Student Flavor Api Service Implementation
     *
     */
    @POST("auth/students/login")
    suspend fun studentLogin(@Body request: StudentLoginRequest): Response<StudentAuthResponse>

    @POST("auth/students/register")
    suspend fun studentRegister(@Body request: StudentRegisterRequest): Response<StudentAuthResponse>


}