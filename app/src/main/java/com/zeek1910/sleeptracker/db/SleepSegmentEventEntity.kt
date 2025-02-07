package com.zeek1910.sleeptracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SleepSegmentEventEntity(
    @PrimaryKey
    val startTime: Long,
    val endTime: Long
)
