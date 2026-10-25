package com.enak.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val purpose: String,
    val plannedMin: Int,
    val actualMin: Int,
    val goal: String?,
    val mood: String,
    val avgScore: Double,
    val exitLoops: Int,
    val achieved: Boolean?,
    val regret: Int?,
    val journal: String?,
    val sentiment: String?,
    val interventionsJson: String,
    val extensionsJson: String,
    val timelineJson: String
)
