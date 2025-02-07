package com.zeek1910.sleeptracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: LogEventEntity)

    @Query("SELECT * FROM LogEventEntity ORDER BY timestamp DESC")
    fun getEventsFlow(): Flow<List<LogEventEntity>>
}