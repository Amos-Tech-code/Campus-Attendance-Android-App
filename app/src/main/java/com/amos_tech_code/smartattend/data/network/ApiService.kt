package com.amos_tech_code.smartattend.data.network

import com.amos_tech_code.smartattend.domain.request.AcademicSetupUpRequest
import com.amos_tech_code.smartattend.domain.request.EndSessionRequest
import com.amos_tech_code.smartattend.domain.request.GoogleSignInRequest
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.StartSessionRequest
import com.amos_tech_code.smartattend.domain.request.StudentLoginRequest
import com.amos_tech_code.smartattend.domain.request.StudentRegisterRequest
import com.amos_tech_code.smartattend.domain.request.UpdateSessionRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.domain.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.LecturerAuthResponse
import com.amos_tech_code.smartattend.domain.response.LecturerUniversitiesResponse
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.domain.response.StudentAuthResponse
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("health/status")
    suspend fun checkApiStatus() : Response<Unit>

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
    suspend fun endAttendanceSession(@Body request: EndSessionRequest) : Response<Unit>

    @GET("attendance/sessions/active")
    suspend fun getActiveSession() : Response<StartAttendanceSessionResponse>



    /**
     *
     * Student Flavor Api Service Implementation
     *
     */
    @POST("auth/students/login")
    suspend fun studentLogin(@Body request: StudentLoginRequest): Response<StudentAuthResponse>

    @POST("auth/students/register")
    suspend fun studentRegister(@Body request: StudentRegisterRequest): Response<StudentAuthResponse>

    @POST("attendance/verify")
    suspend fun verifyAttendanceSession(@Body request: VerifySessionRequest) : Response<VerifyAttendanceResponse>

    @POST("attendance/session/mark")
    suspend fun markAttendanceSession(@Body request: MarkAttendanceRequest) : Response<MarkAttendanceResponse>


}