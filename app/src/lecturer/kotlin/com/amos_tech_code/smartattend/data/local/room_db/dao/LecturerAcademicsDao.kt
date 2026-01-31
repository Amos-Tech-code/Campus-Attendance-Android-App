package com.amos_tech_code.smartattend.data.local.room_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.DepartmentEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeUnitCrossRef
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits
import com.amos_tech_code.smartattend.models.UniversityStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface LecturerAcademicsDao {

    /*------------------------
        READ OPERATIONS
    ------------------------*/
    @Transaction
    @Query("SELECT * FROM universities")
    suspend fun getUniversitiesWithProgrammesAndUnits(): List<UniversityWithProgrammesAndUnits>

    @Transaction
    @Query("SELECT * FROM universities WHERE id = :universityId")
    suspend fun getUniversityWithProgrammesAndUnits(universityId: String): UniversityWithProgrammesAndUnits?

    @Query("SELECT * FROM universities")
    suspend fun getAllUniversities(): List<UniversityEntity>

    @Query("SELECT * FROM universities WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveUniversity(): UniversityEntity?

    @Query("SELECT * FROM academic_terms WHERE universityId = :universityId")
    suspend fun getAcademicTermsForUniversity(universityId: String): List<AcademicTermEntity>

    @Query("SELECT * FROM academic_terms WHERE universityId = :universityId AND isActive = 1 LIMIT 1")
    suspend fun getActiveAcademicTermForUniversity(universityId: String): AcademicTermEntity?

    // Single query to get all statistics
    @Transaction
    suspend fun getUniversityStatistics(universityId: String): UniversityStatistics {
        return UniversityStatistics(
            totalUnits = getTotalUnitsCount(universityId),
            totalExpectedStudents = getTotalExpectedStudents(universityId),
            totalProgrammes = getTotalProgrammesCount(universityId),
            totalDepartments = getTotalDepartmentsCount(universityId),
            activeTerm = getActiveAcademicTerm(universityId)
        )
    }

    // Helper queries for statistics
    @Query("SELECT COUNT(*) FROM units WHERE universityId = :universityId")
    suspend fun getTotalUnitsCount(universityId: String): Int

    @Query("SELECT SUM(expectedStudentCount) FROM programmes WHERE universityId = :universityId")
    suspend fun getTotalExpectedStudents(universityId: String): Int

    @Query("SELECT COUNT(*) FROM programmes WHERE universityId = :universityId")
    suspend fun getTotalProgrammesCount(universityId: String): Int

    @Query("SELECT COUNT(*) FROM departments WHERE universityId = :universityId")
    suspend fun getTotalDepartmentsCount(universityId: String): Int

    @Query("SELECT * FROM academic_terms WHERE universityId = :universityId AND isActive = 1 LIMIT 1")
    suspend fun getActiveAcademicTerm(universityId: String): AcademicTermEntity?

    @Query("SELECT * FROM units WHERE universityId = :universityId")
    suspend fun getAllUnitsForUniversity(universityId: String): List<UnitEntity>
    /*------------------------
        READ OPERATIONS - Flow
    ------------------------*/
    @Query("SELECT * FROM universities")
    fun observeAllUniversities(): Flow<List<UniversityEntity>>

    @Query("SELECT * FROM universities WHERE isActive = 1 LIMIT 1")
    fun observeActiveUniversity(): Flow<UniversityEntity?>

    @Transaction
    @Query("SELECT * FROM universities WHERE id = :universityId")
    fun observeUniversityWithStats(universityId: String): Flow<UniversityWithProgrammesAndUnits?>

    @Query("SELECT * FROM units WHERE universityId = :universityId")
    fun observeUnitsForUniversity(universityId: String): Flow<List<UnitEntity>>

    // Statistics with Flow
    fun observeUniversityStatistics(universityId: String): Flow<UniversityStatistics> {
        return combine(
            observeUnitsCount(universityId),
            observeExpectedStudents(universityId),
            observeProgrammesCount(universityId),
            observeDepartmentsCount(universityId),
            observeActiveAcademicTerm(universityId)
        ) { units, students, programmes, departments, term ->
            UniversityStatistics(
                totalUnits = units,
                totalExpectedStudents = students,
                totalProgrammes = programmes,
                totalDepartments = departments,
                activeTerm = term
            )
        }
    }

    @Query("SELECT COUNT(*) FROM units WHERE universityId = :universityId")
    fun observeUnitsCount(universityId: String): Flow<Int>

    @Query("SELECT SUM(expectedStudentCount) FROM programmes WHERE universityId = :universityId")
    fun observeExpectedStudents(universityId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM programmes WHERE universityId = :universityId")
    fun observeProgrammesCount(universityId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM departments WHERE universityId = :universityId")
    fun observeDepartmentsCount(universityId: String): Flow<Int>

    @Query("SELECT * FROM academic_terms WHERE universityId = :universityId AND isActive = 1 LIMIT 1")
    fun observeActiveAcademicTerm(universityId: String): Flow<AcademicTermEntity?>

    /*------------------------
       INSERT OPERATIONS
   ------------------------*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUniversity(university: UniversityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicTerm(academicTerm: AcademicTermEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicTerms(academicTerms: List<AcademicTermEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<DepartmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramme(programme: ProgrammeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammes(programmes: List<ProgrammeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammeUnits(crossRefs: List<ProgrammeUnitCrossRef>)

    // Transaction for inserting single university setup
    @Transaction
    suspend fun insertUniversitySetup(
        university: UniversityEntity,
        academicTerms: List<AcademicTermEntity>,
        departments: List<DepartmentEntity>,
        programmes: List<ProgrammeEntity>,
        units: List<UnitEntity>,
        programmeUnits: List<ProgrammeUnitCrossRef>
    ) {
        insertUniversity(university)
        insertAcademicTerms(academicTerms)
        insertDepartments(departments)
        insertProgrammes(programmes)
        insertUnits(units)
        insertProgrammeUnits(programmeUnits)
    }

    // Transaction for full hierarchy insertion (multiple universities)
    @Transaction
    suspend fun insertFullHierarchy(
        universities: List<UniversityEntity>,
        academicTerms: List<AcademicTermEntity>,
        departments: List<DepartmentEntity>,
        programmes: List<ProgrammeEntity>,
        units: List<UnitEntity>,
        programmeUnits: List<ProgrammeUnitCrossRef>
    ) {
        universities.forEach { insertUniversity(it) }
        insertAcademicTerms(academicTerms)
        insertDepartments(departments)
        insertProgrammes(programmes)
        insertUnits(units)
        insertProgrammeUnits(programmeUnits)
    }

    /*------------------------
        UPDATE OPERATIONS
   ------------------------*/
    // Set a specific university as active
    @Query("UPDATE universities SET isActive = CASE WHEN id = :universityId THEN 1 ELSE 0 END")
    suspend fun setActiveUniversity(universityId: String)

    /*------------------------
        DELETE OPERATIONS
   ------------------------*/
    // Delete single university setup
    @Transaction
    suspend fun deleteUniversitySetup(universityId: String) {
        deleteProgrammeUnitsForUniversity(universityId)
        deleteUnitsForUniversity(universityId)
        deleteProgrammesForUniversity(universityId)
        deleteDepartmentsForUniversity(universityId)
        deleteAcademicTermsForUniversity(universityId)
        deleteUniversity(universityId)
    }

    @Query("DELETE FROM universities WHERE id = :universityId")
    suspend fun deleteUniversity(universityId: String)

    @Query("DELETE FROM academic_terms WHERE universityId = :universityId")
    suspend fun deleteAcademicTermsForUniversity(universityId: String)

    @Query("DELETE FROM departments WHERE universityId = :universityId")
    suspend fun deleteDepartmentsForUniversity(universityId: String)

    @Query("DELETE FROM programmes WHERE universityId = :universityId")
    suspend fun deleteProgrammesForUniversity(universityId: String)

    @Query("DELETE FROM units WHERE universityId = :universityId")
    suspend fun deleteUnitsForUniversity(universityId: String)

    @Query("DELETE FROM programme_units WHERE unitId IN (SELECT id FROM units WHERE universityId = :universityId)")
    suspend fun deleteProgrammeUnitsForUniversity(universityId: String)

    // Clear all data
    @Query("DELETE FROM programme_units")
    suspend fun clearProgrammeUnits()

    @Query("DELETE FROM units")
    suspend fun clearUnits()

    @Query("DELETE FROM programmes")
    suspend fun clearProgrammes()

    @Query("DELETE FROM departments")
    suspend fun clearDepartments()

    @Query("DELETE FROM academic_terms")
    suspend fun clearAcademicTerms()

    @Query("DELETE FROM universities")
    suspend fun clearUniversities()

    @Transaction
    suspend fun clearAll() {
        clearProgrammeUnits()
        clearUnits()
        clearProgrammes()
        clearDepartments()
        clearAcademicTerms()
        clearUniversities()
    }
}
