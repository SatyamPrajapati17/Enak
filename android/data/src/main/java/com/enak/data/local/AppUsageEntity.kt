package com.enak.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val dateKey: String,
    val usedMinutes: Int,
    val dailyLimitMinutes: Int?,
    val warningCount: Int,
    val isBlocked: Boolean,
    val lastWarningTime: Long
)
