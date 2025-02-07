package com.zeek1910.sleeptracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SleepSegmentEventEntity::class,
        SleepClassifyEventEntity::class,
        LogEventEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepSegmentEventDao(): SleepSegmentEventDao
    abstract fun sleepClassifyEventDao(): SleepClassifyEventDao
    abstract fun logEventDao(): LogEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sleep_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}