package com.amos_tech_code.smartattend.data.local.room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amos_tech_code.smartattend.data.local.room_db.dao.AttendanceSessionHistoryDao
import com.amos_tech_code.smartattend.data.local.room_db.dao.LecturerAcademicsDao
import com.amos_tech_code.smartattend.data.local.room_db.entities.AcademicTermEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.AttendanceSessionHistoryEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.DepartmentEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.ProgrammeUnitCrossRef
import com.amos_tech_code.smartattend.data.local.room_db.entities.UnitEntity
import com.amos_tech_code.smartattend.data.local.room_db.entities.UniversityEntity

@Database(
    entities = [
        UniversityEntity::class,
        AcademicTermEntity::class,
        DepartmentEntity::class,
        ProgrammeEntity::class,
        UnitEntity::class,
        ProgrammeUnitCrossRef::class,

        AttendanceSessionHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ClassTrackProDatabase : RoomDatabase() {

    abstract fun lecturerAcademicsDao() : LecturerAcademicsDao

    abstract fun attendanceHistoryDao() : AttendanceSessionHistoryDao

}