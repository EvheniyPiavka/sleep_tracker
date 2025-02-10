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
import com.google.android.gms.location.SleepSegmentRequest
import com.zeek1910.sleeptracker.Logger
import com.zeek1910.sleeptracker.db.AppDatabase
import com.zeek1910.sleeptracker.db.SleepClassifyEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class SleepReceiver : BroadcastReceiver(),
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {

    override fun onReceive(context: Context, intent: Intent) {
        if (SleepClassifyEvent.hasEvents(intent)) {
            val events = SleepClassifyEvent.extractEvents(intent)
            for (event in events) {
                Logger.log("Received sleep classify event: $event")
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
            Logger.log("sleep detection started")
        }
    }
}