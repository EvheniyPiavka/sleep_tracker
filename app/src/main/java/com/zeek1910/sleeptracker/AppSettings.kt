package com.zeek1910.sleeptracker

import android.content.Context
import androidx.core.content.edit

class AppSettings private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var awakeTime: Pair<Int, Int>
        get() = prefs.getInt(KEY_AWAKE_TIME_HOUR, 8) to prefs.getInt(KEY_AWAKE_TIME_MINUTES, 0)
        set(value) = prefs.edit(commit = true) {
            putInt(KEY_AWAKE_TIME_HOUR, value.first)
            putInt(KEY_AWAKE_TIME_MINUTES, value.second)
        }

    var sleepTime: Pair<Int, Int>
        get() = prefs.getInt(KEY_SLEEP_TIME_HOUR, 22) to prefs.getInt(KEY_SLEEP_TIME_MINUTES, 0)
        set(value) = prefs.edit(commit = true) {
            putInt(KEY_SLEEP_TIME_HOUR, value.first)
            putInt(KEY_SLEEP_TIME_MINUTES, value.second)
        }


    var sleepConfidenceStreak: Int
        get() = prefs.getInt(KEY_SLEEP_CONFIDENCE_STREAK, 0)
        set(value) = prefs.edit(commit = true) {
            putInt(KEY_SLEEP_CONFIDENCE_STREAK, value)
        }

    var wakeConfidenceStreak: Int
        get() = prefs.getInt(KEY_WAKE_CONFIDENCE_STREAK, 0)
        set(value) = prefs.edit(commit = true) {
            putInt(KEY_WAKE_CONFIDENCE_STREAK, value)
        }

    var sleepStartTimestamp: Long
        get() = prefs.getLong(KEY_SLEEP_START_TIME, 0)
        set(value) = prefs.edit(commit = true) {
            putLong(KEY_SLEEP_START_TIME, value)
        }

    var lastWakeEventTime: Long
        get() = prefs.getLong(KEY_LAST_WAKE_EVENT_TIME, 0)
        set(value) = prefs.edit(commit = true) {
            putLong(KEY_LAST_WAKE_EVENT_TIME, value)
        }

    companion object {
        private const val PREFS_NAME = "sleep_tracker_prefs"
        private const val KEY_AWAKE_TIME_HOUR = "awakeTime_hour"
        private const val KEY_AWAKE_TIME_MINUTES = "awakeTime_minutes"
        private const val KEY_SLEEP_TIME_HOUR = "sleepTime_hour"
        private const val KEY_SLEEP_TIME_MINUTES = "sleepTime_minutes"
        private const val KEY_SLEEP_CONFIDENCE_STREAK = "sleepConfidenceStreak"
        private const val KEY_WAKE_CONFIDENCE_STREAK = "wakeConfidenceStreak"
        private const val KEY_LAST_WAKE_EVENT_TIME = "lastWakeEventTime"
        private const val KEY_SLEEP_START_TIME = "sleepStartTimestamp"
        private var _instance: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return _instance ?: synchronized(this) {
                _instance = AppSettings(context)
                requireNotNull(_instance)
            }
        }
    }
}