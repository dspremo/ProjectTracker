package com.example.projecttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Projekat::class, RadniSat::class, Trosak::class, Uplata::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projekatDao(): ProjekatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "project_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        // Alias za kompatibilnost
        fun getDatabase(context: Context): AppDatabase = getInstance(context)

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
