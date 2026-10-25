package com.enak.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage WHERE dateKey = :dateKey")
    fun getUsageForDate(dateKey: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND dateKey = :dateKey LIMIT 1")
    suspend fun getUsage(packageName: String, dateKey: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(usage: AppUsageEntity)

    @Update
    suspend fun update(usage: AppUsageEntity)

    @Query("DELETE FROM app_usage")
    suspend fun deleteAll()
}
