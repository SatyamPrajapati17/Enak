package com.enak.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SessionEntity::class, AppUsageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EnakDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        @Volatile
        private var INSTANCE: EnakDatabase? = null

        fun getDatabase(context: Context): EnakDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EnakDatabase::class.java,
                    "enak_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
