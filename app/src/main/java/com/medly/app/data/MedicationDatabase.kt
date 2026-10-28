package com.medly.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Medication::class], version = 2, exportSchema = false)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
}
