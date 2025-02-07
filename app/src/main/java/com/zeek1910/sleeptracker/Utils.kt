package com.zeek1910.sleeptracker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    fun getDuration(startTime: Long, endTime: Long): String {
        val duration = endTime - startTime
        val hours = duration / 3600000
        val minutes = (duration % 3600000) / 60000
        return "$hours hours $minutes minutes"
    }

    fun formatDateTime(timestampMillis: Long): String {
        val date = Date(timestampMillis)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }

    fun millisToTimeString(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}