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

    @Query("SELECT * FROM SleepClassifyEventEntity")
    fun getAllEventsFlow(): Flow<List<SleepClassifyEventEntity>>

    @Query("DELETE FROM SleepClassifyEventEntity")
    suspend fun clear()
}