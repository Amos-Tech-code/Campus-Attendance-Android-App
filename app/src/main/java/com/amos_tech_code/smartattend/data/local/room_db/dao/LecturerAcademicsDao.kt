package com.amos_tech_code.smartattend.data.local.room_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeUnitCrossRef
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityWithProgrammesAndUnits

@Dao
interface LecturerAcademicsDao {

    // Fetch full hierarchy: University -> Programmes -> Units
    @Transaction
    @Query("SELECT * FROM universities")
    suspend fun getUniversitiesWithProgrammesAndUnits(): List<UniversityWithProgrammesAndUnits>

    @Query("SELECT * FROM universities")
    suspend fun getAllUniversities(): List<UniversityEntity>

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUniversities(universities: List<UniversityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammes(programmes: List<ProgrammeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammeUnits(crossRefs: List<ProgrammeUnitCrossRef>)

    // Transaction for full hierarchy insertion
    @Transaction
    suspend fun insertFullHierarchy(
        universities: List<UniversityEntity>,
        programmes: List<ProgrammeEntity>,
        units: List<UnitEntity>,
        programmeUnits: List<ProgrammeUnitCrossRef>
    ) {
        insertUniversities(universities)
        insertProgrammes(programmes)
        insertUnits(units)
        insertProgrammeUnits(programmeUnits)
    }

    // Set a specific university as active
    @Query("UPDATE universities SET isActive = CASE WHEN id = :universityId THEN 1 ELSE 0 END")
    suspend fun setActiveUniversity(universityId: String)

    // Delete all when doing a full refresh
    @Query("DELETE FROM programme_units")
    suspend fun clearProgrammeUnits()

    @Query("DELETE FROM units")
    suspend fun clearUnits()

    @Query("DELETE FROM programmes")
    suspend fun clearProgrammes()

    @Query("DELETE FROM universities")
    suspend fun clearUniversities()

    @Transaction
    suspend fun clearAll() {
        clearProgrammeUnits()
        clearUnits()
        clearProgrammes()
        clearUniversities()
    }
}