package com.zeek1910.sleeptracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepClassifyEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SleepClassifyEventEntity)

    @Query("SELECT * FROM SleepClassifyEventEntity")
    suspend fun getAllEvents(): List<SleepClassifyEventEntity>

    @Query("SELECT * FROM SleepClassifyEventEntity ORDER BY timestampMillis DESC")
    fun getAllEventsFlow(): Flow<List<SleepClassifyEventEntity>>

    @Query("SELECT * FROM SleepClassifyEventEntity WHERE isProcessed = 0")
    suspend fun getUnprocessedEvents(): List<SleepClassifyEventEntity>

    @Query("UPDATE SleepClassifyEventEntity SET isProcessed = 1 WHERE isProcessed = 0")
    suspend fun markAllAsProcessed()

    @Query("DELETE FROM SleepClassifyEventEntity")
    suspend fun clear()
}