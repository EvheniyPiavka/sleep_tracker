package com.zeek1910.sleeptracker

import android.util.Log
import com.zeek1910.sleeptracker.db.LogEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object Logger : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {
    fun log(message: String) = launch {
        Log.d("Logger", message)
        App.database.logEventDao().insertEvent(
            LogEventEntity(
                timestamp = System.currentTimeMillis(),
                message = message
            )
        )
    }
}