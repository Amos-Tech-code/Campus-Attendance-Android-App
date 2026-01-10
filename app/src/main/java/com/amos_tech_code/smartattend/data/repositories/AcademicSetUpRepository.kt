package com.amos_tech_code.smartattend.data.repositories

import android.util.Log
import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.DepartmentEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeUnitCrossRef
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.mappers.academicSetupResponseToEntities
import com.amos_tech_code.smartattend.data.mappers.lecturerUniversitiesResponseToEntities
import com.amos_tech_code.smartattend.data.mappers.toDomain
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.models.University
import com.amos_tech_code.smartattend.domain.request.AcademicSetUpRequest
import com.amos_tech_code.smartattend.domain.request.DepartmentSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.ProgrammeSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UnitSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UniversitySuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UpdateAcademicSetupRequest
import com.amos_tech_code.smartattend.domain.response.AcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.DepartmentSuggestion
import com.amos_tech_code.smartattend.domain.response.LecturerAcademicSetupResponse
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UnitSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion
import com.amos_tech_code.smartattend.models.TeachingStatistics
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AcademicSetUpRepository(
    private val apiService: ApiService,
    private val session: ClassTrackProSession,
    private val ioDispatcher: CoroutineDispatcher,
    private val lecturerAcademicsDao: LecturerAcademicsDao
) {
    /**
     * Fetches a list of university suggestions from the network based on a search query.
     * @param request The request containing the search query and limit.
     * @return An [ApiResult] containing a list of [UniversitySuggestion] on success, or an error on failure.
     */
    suspend fun fetchMatchingUniversities(request: UniversitySuggestionRequest): ApiResult<List<UniversitySuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingUniversities(
                query = request.query,
                limit = request.limit
            )
        }
    }

    /**
     * Fetches a list of department suggestions from the network for a given university.
     * @param request The request containing the university ID, search query, and limit.
     * @return An [ApiResult] containing a list of [DepartmentSuggestion] on success, or an error on failure.
     */
    suspend fun fetchMatchingDepartments(request: DepartmentSuggestionRequest): ApiResult<List<DepartmentSuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingDepartments(
                universityId = request.universityId,
                query = request.query,
                limit = request.limit
            )
        }
    }

    /**
     * Fetches a list of programme suggestions from the network for a given university and department.
     * @param request The request containing university ID, optional department ID, search query, and limit.
     * @return An [ApiResult] containing a list of [ProgrammeSuggestion] on success, or an error on failure.
     */
    suspend fun fetchMatchingProgrammes(request: ProgrammeSuggestionRequest): ApiResult<List<ProgrammeSuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingProgrammes(
                universityId = request.universityId,
                departmentId = request.departmentId,
                query = request.query,
                limit = request.limit
            )
        }
    }

    /**
     * Fetches a list of unit suggestions from the network for a given context (university, department, programme).
     * @param request The request containing university, optional department/programme IDs, search query, and limit.
     * @return An [ApiResult] containing a list of [UnitSuggestion] on success, or an error on failure.
     */
    suspend fun fetchMatchingUnits(request: UnitSuggestionRequest): ApiResult<List<UnitSuggestion>> {
        return safeApiCall {
            apiService.fetchMatchingUnits(
                universityId = request.universityId,
                departmentId = request.departmentId,
                programmeId = request.programmeId,
                query = request.query,
                limit = request.limit
            )
        }
    }

    /**
     * Uploads a new academic setup for the lecturer to the remote server.
     * On success, it asynchronously saves the new setup to the local database.
     * @param request The [AcademicSetUpRequest] containing the full academic hierarchy to upload.
     * @return An [ApiResult] with [AcademicSetupResponse] on success, or an error on failure.
     */
    suspend fun uploadAcademicSetUp(request: AcademicSetUpRequest): ApiResult<AcademicSetupResponse> {

        val result = safeApiCall { apiService.uploadAcademicSetup(request) }

        if (result is ApiResult.Success) {
            saveUniversitySetupAsync(result.data)
        }
        return result

    }

    /**
     * Updates an existing academic setup for a specific university on the remote server.
     * On success, it asynchronously updates the setup in the local database.
     * @param request The [UpdateAcademicSetupRequest] containing the updated academic details.
     * @return An [ApiResult] with [AcademicSetupResponse] on success, or an error on failure.
     */
    suspend fun updateAcademicSetUp(request: UpdateAcademicSetupRequest): ApiResult<AcademicSetupResponse> {

        val result = safeApiCall { apiService.updateAcademicSetup(request) }

        if (result is ApiResult.Success) {
            updateUniversitySetupAsync(request.universityId, result.data)
        }
        return result

    }

    /**
     * Fetches the complete academic setup for the lecturer from the remote server.
     * Can fetch for a specific university or all universities if [universityId] is null.
     * @param universityId The optional ID of the university to fetch. If null, fetches all setups.
     * @return An [ApiResult] with [LecturerAcademicSetupResponse] on success, or an error on failure.
     */
    suspend fun fetchLecturerAcademicSetUp(universityId: String?): ApiResult<LecturerAcademicSetupResponse> {

        return safeApiCall { apiService.fetchLecturerAcademicSetUp(universityId) }

    }

    /**
     * Local Data source Operations
     * Save a single university setup after upload/update
     */
    private fun saveUniversitySetupAsync(response: AcademicSetupResponse) {
        // Launch a coroutine in the IO dispatcher that's independent of the calling scope
        CoroutineScope(ioDispatcher + SupervisorJob()).launch {
            try {
                saveUniversitySetup(response)
            } catch (e: Exception) {
                session.setAcademicSyncStatus(false)
                Log.e("AcademicSetUpRepository", "Failed to save university setup: ${e.message}")
            }
        }
    }

    /**
     * Update a single university setup
     */
    private fun updateUniversitySetupAsync(universityId: String, response: AcademicSetupResponse) {
        CoroutineScope(ioDispatcher + SupervisorJob()).launch {
            try {
                // Get the current active university before deletion
                val currentActive = lecturerAcademicsDao.getActiveUniversity()
                val wasActive = currentActive?.id == universityId

                // First delete existing setup for this university
                lecturerAcademicsDao.deleteUniversitySetup(universityId)

                // Save the updated setup
                saveUniversitySetup(response, universityId)

                // If this was the active university, make sure it stays active
                if (wasActive) {
                    handleActiveUniversity(universityId)
                }
            } catch (e: Exception) {
                session.setAcademicSyncStatus(false)
                Log.e(
                    "AcademicSetUpRepository",
                    "Failed to save updated university setup: ${e.message}"
                )
            }
        }
    }

    /**
     * Saves a complete academic setup for a single university to the local database.
     * This includes the university, departments, programmes, units, academic terms, and their relationships.
     * It also handles setting the active university status.
     * @param response The [AcademicSetupResponse] from the API to be persisted.
     * @param universityId An optional university ID to use if not present in the response.
     */
    suspend fun saveUniversitySetup(response: AcademicSetupResponse, universityId: String? = null) {
        val (university, departmentProgrammeUnitTriple) = academicSetupResponseToEntities(
            response,
            universityId
        )
        val (departments, programmes, units) = departmentProgrammeUnitTriple

        // Create programme-unit relationships
        val programmeUnits = mutableListOf<ProgrammeUnitCrossRef>()
        response.programmes.forEach { programmeResponse ->
            programmeResponse.units.forEach { unitResponse ->
                programmeUnits.add(
                    ProgrammeUnitCrossRef(
                        programmeId = programmeResponse.programmeId,
                        unitId = unitResponse.unitId
                    )
                )
            }
        }

        // Extract academic terms from response
        val academicTerms = response.academicTerm?.let { term ->
            listOf(
                AcademicTermEntity(
                    id = term.id,
                    universityId = university.id,
                    academicYear = term.academicYear,
                    semester = term.semester,
                    isActive = term.isActive
                )
            )
        } ?: emptyList()

        // Save to database
        lecturerAcademicsDao.insertUniversitySetup(
            university = university,
            academicTerms = academicTerms,
            departments = departments,
            programmes = programmes,
            units = units,
            programmeUnits = programmeUnits
        )

        // Handle active university logic
        if (response.isActive) {
            handleActiveUniversity(university.id)
        } else {
            // Check if we need to mark any university as active
            ensureActiveUniversityExists()
        }
    }


    /**
     * Fetches the lecturer's full academic hierarchy from the server and syncs it with the local database.
     * This involves clearing the existing local data and replacing it with the fresh data from the API.
     * @param universityId The optional ID of the university to sync. If null, syncs all data for the lecturer.
     */
    suspend fun syncLecturerAcademics(universityId: String?) {
        val result = fetchLecturerAcademicSetUp(universityId)

        when (result) {
            is ApiResult.Success -> {
                val (universities, programmes, units) = lecturerUniversitiesResponseToEntities(
                    result.data
                )

                // Create additional entities from response
                val academicTerms = mutableListOf<AcademicTermEntity>()
                val departments = mutableListOf<DepartmentEntity>()
                val programmeUnits = mutableListOf<ProgrammeUnitCrossRef>()

                result.data.universities.forEach { universitySetup ->
                    // Academic Terms
                    universitySetup.academicTerms.forEach { term ->
                        academicTerms.add(
                            AcademicTermEntity(
                                id = term.id,
                                universityId = universitySetup.university.id,
                                academicYear = term.academicYear,
                                semester = term.semester,
                                isActive = term.isActive
                            )
                        )
                    }

                    // Departments and Programme-Unit relationships
                    universitySetup.programmes.forEach { programmeSetup ->
                        // Department
                        departments.add(
                            DepartmentEntity(
                                id = programmeSetup.department.id,
                                universityId = universitySetup.university.id,
                                name = programmeSetup.department.name
                            )
                        )

                        // Programme-Unit relationships
                        programmeSetup.units.forEach { unitSetup ->
                            programmeUnits.add(
                                ProgrammeUnitCrossRef(
                                    programmeId = programmeSetup.programme.id,
                                    unitId = unitSetup.unitId
                                )
                            )
                        }
                    }
                }

                // Clear and insert full hierarchy
                lecturerAcademicsDao.clearAll()
                lecturerAcademicsDao.insertFullHierarchy(
                    universities = universities,
                    academicTerms = academicTerms,
                    departments = departments,
                    programmes = programmes,
                    units = units,
                    programmeUnits = programmeUnits
                )

                // Ensure we have an active university after sync
                updateActiveUniversityAfterSync(universities)

                session.setAcademicSyncStatus(true)
            }

            is ApiResult.Failure -> {
                session.setAcademicSyncStatus(false)
                Log.e("AcademicSetUpRepository", "Sync failed: ${result.error}")
            }
        }
    }

    /**
     * Retrieves a list of all universities with their complete academic hierarchies (programmes, units, etc.) for the lecturer from the local database.
     * It triggers a network sync if the data hasn't been synced before.
     * @return A list of [University] domain models.
     */
    suspend fun getAllAcademicsForLecturer(): List<University> {
        // Sync if not done yet
        if (!session.getAcademicSyncStatus()) {
            try {
                syncLecturerAcademics(null)
            } catch (e: Exception) {
                // Handle sync failure
                Log.e("AcademicSetUpRepository", "Sync failed: ${e.message}")
            }
        }

        // Fetch all from Room
        val dbUniversities = lecturerAcademicsDao.getUniversitiesWithProgrammesAndUnits()

        // Map to domain models
        return dbUniversities.map { it.toDomain() }
    }

    /**
     * Retrieves the currently active university with its full academic hierarchy.
     * If no university is explicitly marked as active, it returns the first one in the list.
     * @return The active [University] domain model, or null if no universities are set up.
     */
    suspend fun getActiveUniversityAcademics(): University? {
        val allUniversities = getAllAcademicsForLecturer()
        val activeUniversity = allUniversities.find { it.isActive }

        return activeUniversity ?: allUniversities.firstOrNull()
    }

    /**
     * Marks a specific university as the active one in the local database.
     * All other universities will be marked as inactive.
     * @param universityId The ID of the university to set as active.
     */
    suspend fun setActiveUniversity(universityId: String) {
        lecturerAcademicsDao.setActiveUniversity(universityId)
    }

    /**
     * Retrieves a simple list of all universities stored in the local database, without their nested academic details.
     * @return A list of [University] domain models, containing only ID, name, and active status.
     */
    suspend fun getUniversities(): List<University> {
        val universities = lecturerAcademicsDao.getAllUniversities()
        return universities.map { universityEntity ->
            University(
                id = universityEntity.id,
                name = universityEntity.name,
                isActive = universityEntity.isActive
            )
        }
    }

    /**
     * Calculates and retrieves teaching statistics for the currently active university.
     * If no university is active, it returns zeroed-out or default statistics.
     * @return A [TeachingStatistics] object with totals for courses, students, and semester info.
     */
    suspend fun getTeachingStatistics(): TeachingStatistics {
        return try {
            // Get active university first
            val activeUniversity =
                lecturerAcademicsDao.getActiveUniversity() ?: return TeachingStatistics(
                    totalCourses = 0,
                    totalExpectedStudents = 0,
                    currentSemester = "No active institution",
                    activeInstitution = "",
                    isInstitutionActive = false
                )

            // Get all statistics in parallel for efficiency
            val statistics = lecturerAcademicsDao.getUniversityStatistics(activeUniversity.id)

            // Format semester display
            val semesterDisplay = statistics.activeTerm?.let { term ->
                "Year ${term.academicYear}, Semester ${term.semester}"
            } ?: "Not Set"

            TeachingStatistics(
                totalCourses = statistics.totalUnits,
                totalExpectedStudents = statistics.totalExpectedStudents,
                currentSemester = semesterDisplay,
                totalProgrammes = statistics.totalProgrammes,
                totalDepartments = statistics.totalDepartments,
                activeInstitution = activeUniversity.name,
                isInstitutionActive = activeUniversity.isActive
            )
        } catch (e: Exception) {
            //Log.e("AcademicSetUpRepository", "Error getting teaching statistics", e)
            TeachingStatistics(
                totalCourses = 0,
                totalExpectedStudents = 0,
                currentSemester = "Error loading data",
                activeInstitution = "",
                isInstitutionActive = false
            )
        }
    }

    /**
     * Ensures that at least one university is marked as active.
     * If no university is active, marks the first one as active.
     */
    private suspend fun ensureActiveUniversityExists() {
        val activeUniversity = lecturerAcademicsDao.getActiveUniversity()
        if (activeUniversity == null) {
            val allUniversities = lecturerAcademicsDao.getAllUniversities()
            allUniversities.firstOrNull()?.let {
                lecturerAcademicsDao.setActiveUniversity(it.id)
            }
        }
    }

    /**
     * Handles setting a university as active while ensuring only one is active at a time
     */
    private suspend fun handleActiveUniversity(universityId: String) {
        lecturerAcademicsDao.setActiveUniversity(universityId)
    }

    /**
     * Updates the active university status after syncing
     * This ensures we always have one university marked as active
     */
    private suspend fun updateActiveUniversityAfterSync(universities: List<UniversityEntity>) {
        // Check if we already have an active university
        val existingActive = lecturerAcademicsDao.getActiveUniversity()

        if (existingActive != null) {
            // If we have an active university, check if it still exists in the synced data
            val existsInSync = universities.any { it.id == existingActive.id }
            if (existsInSync) {
                // Keep the existing active university
                return
            }
        }

        // If no active university or it doesn't exist in sync, mark the first one as active
        universities.firstOrNull()?.let {
            lecturerAcademicsDao.setActiveUniversity(it.id)
        }
    }

}