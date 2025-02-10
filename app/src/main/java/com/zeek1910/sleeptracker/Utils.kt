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
}