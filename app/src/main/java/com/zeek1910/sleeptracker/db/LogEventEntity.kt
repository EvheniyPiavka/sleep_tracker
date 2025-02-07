package com.zeek1910.sleeptracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LogEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val message: String,
)