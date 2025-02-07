package com.zeek1910.sleeptracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SleepClassifyEventEntity(
    @PrimaryKey
    val timestampMillis: Long,
    val motion: Int,
    val light: Int,
    val confidence: Int,
)
