package com.amos_tech_code.smartattend.data.network

import com.amos_tech_code.smartattend.domain.request.AcademicSetUpRequest
import com.amos_tech_code.smartattend.domain.request.AttendanceExportRequest
import com.amos_tech_code.smartattend.domain.request.EndSessionRequest
import com.amos_tech_code.smartattend.domain.request.GoogleSignInRequest
import com.amos_tech_code.smartattend.domain.request.MarkAttendanceRequest
import com.amos_tech_code.smartattend.domain.request.RemoveAttendanceRecordRequest
import com.amos_tech_code.smartattend.domain.request.StartSessionRequest
import com.amos_tech_code.smartattend.domain.request.StudentEnrollmentRequest
import com.amos_tech_code.smartattend.domain.request.StudentLoginRequest
import com.amos_tech_code.smartattend.domain.request.StudentRegisterRequest
import com.amos_tech_code.smartattend.domain.request.UpdateAcademicSetupRequest
import com.amos_tech_code.smartattend.domain.request.UpdateLecturerProfileRequest
import com.amos_tech_code.smartattend.domain.request.UpdateSessionRequest
import com.amos_tech_code.smartattend.domain.request.UpdateStudentProfileRequest
import com.amos_tech_code.smartattend.domain.request.UpdateYearRequest
import com.amos_tech_code.smartattend.domain.request.VerifySessionRequest
import com.amos_tech_code.smartattend.domain.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.AttendanceExportRecordDto
import com.amos_tech_code.smartattend.domain.response.AttendanceExportResponseDto
import com.amos_tech_code.smartattend.domain.response.AttendanceSessionHistoryResponse
import com.amos_tech_code.smartattend.domain.response.AttendanceStatsResponse
import com.amos_tech_code.smartattend.domain.response.DepartmentSuggestion
import com.amos_tech_code.smartattend.domain.response.ExportsListResponseDto
import com.amos_tech_code.smartattend.domain.response.GenericResponse
import com.amos_tech_code.smartattend.domain.response.LecturerAcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.LecturerAuthResponse
import com.amos_tech_code.smartattend.domain.response.MarkAttendanceResponse
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.StartAttendanceSessionResponse
import com.amos_tech_code.smartattend.domain.response.StudentAttendanceHistoryResponse
import com.amos_tech_code.smartattend.domain.response.StudentAuthResponse
import com.amos_tech_code.smartattend.domain.response.StudentEnrollmentResponse
import com.amos_tech_code.smartattend.domain.response.UnitSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion
import com.amos_tech_code.smartattend.domain.response.VerifyAttendanceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("health/status")
    suspend fun checkApiStatus() : Response<Unit>

    /**
     * Lecturer Flavor Api Service Implementation
     */
    @POST("auth/lecturers/google")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<LecturerAuthResponse>

    @PATCH("account/profile/lecturer")
    suspend fun updateLecturerProfile(
        @Body request: UpdateLecturerProfileRequest
    ) : Response<GenericResponse>

    @GET("lecturer/academic-setup/suggestions/universities")
    suspend fun fetchMatchingUniversities(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ) : Response<List<UniversitySuggestion>>

    @GET("lecturer/academic-setup/suggestions/departments")
    suspend fun fetchMatchingDepartments(
        @Query("universityId") universityId: String,
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ) : Response<List<DepartmentSuggestion>>

    @GET("lecturer/academic-setup/suggestions/programmes")
    suspend fun fetchMatchingProgrammes(
        @Query("universityId") universityId: String,
        @Query("departmentId") departmentId: String?,
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ) : Response<List<ProgrammeSuggestion>>

    @GET("lecturer/academic-setup/suggestions/units")
    suspend fun fetchMatchingUnits(
        @Query("universityId") universityId: String,
        @Query("departmentId") departmentId: String?,
        @Query("programmeId") programmeId: String?,
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ) : Response<List<UnitSuggestion>>

    @POST("lecturer/academic-setup")
    suspend fun uploadAcademicSetup(@Body request: AcademicSetUpRequest): Response<AcademicSetupResponse>

    @PUT("lecturer/academic-setup")
    suspend fun updateAcademicSetup(@Body request: UpdateAcademicSetupRequest): Response<AcademicSetupResponse>

    @GET("lecturer/academic-setup")
    suspend fun fetchLecturerAcademicSetUp(
        @Query("universityId") universityId: String?
    ): Response<LecturerAcademicSetupResponse>

    @POST("session/start")
    suspend fun startAttendanceSession(@Body request: StartSessionRequest) : Response<StartAttendanceSessionResponse>

    @PATCH("session/{sessionId}")
    suspend fun updateAttendanceSession(
        @Path("sessionId") sessionId: String,
        @Body request: UpdateSessionRequest
    ) : Response<StartAttendanceSessionResponse>

    @POST("session/end")
    suspend fun endAttendanceSession(@Body request: EndSessionRequest) : Response<Unit>

    @GET("session/active")
    suspend fun getActiveSession() : Response<StartAttendanceSessionResponse>

    @GET("session/history")
    suspend fun getSessionsHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ) : Response<AttendanceSessionHistoryResponse>

    @HTTP(method = "DELETE", path = "attendance-manage/record", hasBody = true)
    suspend fun removeFlaggedStudent(
        @Body request: RemoveAttendanceRecordRequest
    ) : Response<Unit>


    @POST("attendance-manage/record/export")
    suspend fun exportAttendanceRecords(
        @Body request: AttendanceExportRequest
    ) : Response<AttendanceExportResponseDto>

    @GET("attendance-manage/record/export/{exportId}")
    suspend fun getExportStatus(
        @Path("exportId") exportId: String
    ) : Response<AttendanceExportRecordDto>

    @GET("attendance-manage/record/export")
    suspend fun getExportRecords(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        //@Query("sort") sort: String = "desc"
    ) : Response<ExportsListResponseDto>

    /**
     *
     * Student Flavor Api Service Implementation
     *
     */
    @POST("auth/students/login")
    suspend fun studentLogin(@Body request: StudentLoginRequest): Response<StudentAuthResponse>

    @POST("auth/students/register")
    suspend fun studentRegister(@Body request: StudentRegisterRequest): Response<StudentAuthResponse>

    @PATCH("account/profile/student")
    suspend fun updateStudentProfile(
        @Body request: UpdateStudentProfileRequest
    ) : Response<GenericResponse>

    @POST("session/verify")
    suspend fun verifyAttendanceSession(@Body request: VerifySessionRequest) : Response<VerifyAttendanceResponse>

    @POST("attendance/mark")
    suspend fun markAttendanceSession(@Body request: MarkAttendanceRequest) : Response<MarkAttendanceResponse>

    @GET("attendance-manage/record")
    suspend fun getAttendanceRecords(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "desc"
    ) : Response<StudentAttendanceHistoryResponse>

    @GET("students/enrollment")
    suspend fun getStudentEnrollment() : Response<StudentEnrollmentResponse>

    @POST("students/enrollment")
    suspend fun enrollStudent(
        @Body request: StudentEnrollmentRequest
    ) : Response<StudentEnrollmentResponse>

    @PATCH("students/enrollment/{enrollmentId}/year")
    suspend fun updateStudentEnrollment(
        @Path("enrollmentId") enrollmentId: String,
        @Body request: UpdateYearRequest
    ) : Response<StudentEnrollmentResponse>

    @DELETE("students/enrollment/{enrollmentId}")
    suspend fun deleteStudentEnrollment(
        @Path("enrollmentId") enrollmentId: String
    ) : Response<Unit>

    @GET("attendance-manage/record/students/stats")
    suspend fun getStudentAttendanceStats(
    ): Response<AttendanceStatsResponse>
}
