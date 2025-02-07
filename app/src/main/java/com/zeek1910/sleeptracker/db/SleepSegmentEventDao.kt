package com.zeek1910.sleeptracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSegmentEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SleepSegmentEventEntity)

    @Query("SELECT * FROM SleepSegmentEventEntity")
    suspend fun getAllEvents(): List<SleepSegmentEventEntity>

    @Query("SELECT * FROM SleepSegmentEventEntity")
    fun getAllEventsFlow(): Flow<List<SleepSegmentEventEntity>>
}