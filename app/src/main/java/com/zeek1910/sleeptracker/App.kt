package com.zeek1910.sleeptracker

import android.app.Application
import com.zeek1910.sleeptracker.db.AppDatabase

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        _database = AppDatabase.getDatabase(this)
    }

    companion object {
        private var _database: AppDatabase? = null
        val database get() = requireNotNull(_database)
    }
}