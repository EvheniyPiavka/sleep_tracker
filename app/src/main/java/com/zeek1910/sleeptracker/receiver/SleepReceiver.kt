package com.zeek1910.sleeptracker.receiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import com.google.android.gms.location.SleepSegmentRequest
import com.zeek1910.sleeptracker.App
import com.zeek1910.sleeptracker.Logger
import com.zeek1910.sleeptracker.db.AppDatabase
import com.zeek1910.sleeptracker.db.SleepClassifyEventEntity
import com.zeek1910.sleeptracker.db.SleepSegmentEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date


class SleepReceiver : BroadcastReceiver(),
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {

    private val sleepThreshold = 80
    private val wakeThreshold = 50
    private val requiredSleepEvents = 3
    private val requiredWakeEvents = 2
    private val requiredSleepDuration = 30 * 60 * 1000L
    private val requiredWakeDuration = 20 * 60 * 1000L
    private val microWakeupDuration = 5 * 60 * 1000L


    override fun onReceive(context: Context, intent: Intent) {
        if (SleepSegmentEvent.hasEvents(intent)) {
            val events = SleepSegmentEvent.extractEvents(intent)
            for (event in events) {
                Logger.log("Received sleep segment event: $event")
            }
        }
        if (SleepClassifyEvent.hasEvents(intent)) {
            val events = SleepClassifyEvent.extractEvents(intent)
            for (event in events) {
                Logger.log("Received sleep classify event: $event")
                processSleepEvent(event)
                launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(context).sleepClassifyEventDao().insert(
                        SleepClassifyEventEntity(
                            timestampMillis = event.timestampMillis,
                            motion = event.motion,
                            light = event.light,
                            confidence = event.confidence
                        )
                    )
                }
            }
        }
    }

    private fun processSleepEvent(event: SleepClassifyEvent) {
        val settings = App.settings
        val confidence = event.confidence
        val timestamp = event.timestampMillis
        val currentTime = System.currentTimeMillis()

        if (confidence >= sleepThreshold) {
            settings.sleepConfidenceStreak++
            settings.wakeConfidenceStreak = 0 // Reset the wake counter
            if (settings.sleepConfidenceStreak >= requiredSleepEvents) {
                if (settings.sleepStartTimestamp == 0L) {
                    settings.sleepStartTimestamp = timestamp
                }
            }
        } else if (confidence < wakeThreshold) {
            settings.wakeConfidenceStreak++
            settings.sleepConfidenceStreak = 0 // Reset the sleep counter

            if (settings.wakeConfidenceStreak >= requiredWakeEvents) {
                // Check if this is a short-term awakening (< 5 minutes) - we ignore it
                if (settings.lastWakeEventTime > 0 && (timestamp - settings.lastWakeEventTime) <= microWakeupDuration) {
                    Logger.log("Ignore the short-term awakening")
                    return
                }
                settings.lastWakeEventTime = timestamp
            }
        } else {
            settings.sleepConfidenceStreak = 0
            settings.wakeConfidenceStreak = 0
        }

        // Fixing the onset of sleep
        if (settings.sleepStartTimestamp != 0L && (currentTime - settings.sleepStartTimestamp) >= requiredSleepDuration) {
            Logger.log("Sleep start: ${Date(settings.sleepStartTimestamp)}")
        }

        // Fixation of the end of sleep (awakening)
        if (settings.lastWakeEventTime > 0 && (currentTime - settings.lastWakeEventTime) >= requiredWakeDuration) {
            if (settings.sleepStartTimestamp != 0L) {
                Logger.log("Sleep end: ${Date(settings.lastWakeEventTime)}")
                launch(Dispatchers.IO) {
                    App.database.sleepSegmentEventDao().insert(
                        SleepSegmentEventEntity(
                            startTime = settings.sleepStartTimestamp,
                            endTime = settings.lastWakeEventTime
                        )
                    )
                }
                settings.sleepStartTimestamp = 0
                settings.sleepConfidenceStreak = 0
                settings.wakeConfidenceStreak = 0
                settings.lastWakeEventTime = 0
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 1910
        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, SleepReceiver::class.java)
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun checkAndValidateLastSleep() {
            val settings = App.settings
            GlobalScope.launch(Dispatchers.IO) {
                if (settings.sleepStartTimestamp != 0L) {
                    App.database.sleepSegmentEventDao().insert(
                        SleepSegmentEventEntity(
                            startTime = settings.sleepStartTimestamp,
                            endTime = System.currentTimeMillis()
                        )
                    )
                    settings.sleepStartTimestamp = 0
                    settings.lastWakeEventTime = 0
                    settings.sleepConfidenceStreak = 0
                    settings.wakeConfidenceStreak = 0
                }
            }
        }

        fun startSleepDetection(context: Context) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val pendingIntent = getPendingIntent(context)
            val client = ActivityRecognition.getClient(context)
            client.removeSleepSegmentUpdates(pendingIntent)
            client.requestSleepSegmentUpdates(
                pendingIntent,
                SleepSegmentRequest.getDefaultSleepSegmentRequest()
            )
            Logger.log("startSleepDetection")
        }

        fun stopSleepDetection(context: Context) {
            val pendingIntent = getPendingIntent(context)
            val client = ActivityRecognition.getClient(context)
            client.removeSleepSegmentUpdates(pendingIntent)
            Logger.log("stopSleepDetection")
        }
    }
}