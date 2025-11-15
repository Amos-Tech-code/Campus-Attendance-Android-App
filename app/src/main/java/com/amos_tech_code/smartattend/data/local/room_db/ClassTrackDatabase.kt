package com.amos_tech_code.smartattend.data.local.room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeUnitCrossRef
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity

@Database(
    entities = [
        UniversityEntity::class,
        ProgrammeEntity::class,
        UnitEntity::class,
        ProgrammeUnitCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ClassTrackDatabase : RoomDatabase() {

    abstract fun lecturerAcademicsDao() : LecturerAcademicsDao


}