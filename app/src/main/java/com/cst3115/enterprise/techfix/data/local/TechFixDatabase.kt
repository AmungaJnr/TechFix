package com.cst3115.enterprise.techfix.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database for TechFix.
 * A shared instance avoids opening unnecessary database connections.
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TechFixDatabase : RoomDatabase() {

    /**
     * Provides access to Room's generated TaskDao implementation.
     */
    abstract fun taskDao(): TaskDao

    companion object {

        @Volatile
        private var INSTANCE: TechFixDatabase? = null

        /**
         * Returns the existing database or safely creates it once.
         */
        fun getDatabase(context: Context): TechFixDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TechFixDatabase::class.java,
                    "techfix_database"
                )
                    .build()
                    .also { database ->
                        INSTANCE = database
                    }
            }
        }
    }
}