package com.zeek1910.sleeptracker.worker

import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zeek1910.sleeptracker.Logger
import com.zeek1910.sleeptracker.Utils
import com.zeek1910.sleeptracker.receiver.SleepReceiver
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StopSleepTrackerWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Logger.log("stopSleepDetection")
        SleepReceiver.stopSleepDetection(context)
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "stopSleepTracking"

        fun scheduleStopSleepTracking(context: Context, hour: Int, minute: Int) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            val delay = calendar.timeInMillis - System.currentTimeMillis()

            val workRequest: WorkRequest =
                PeriodicWorkRequestBuilder<StopSleepTrackerWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .addTag(WORK_TAG)
                    .build()

            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(WORK_TAG)
            workManager.enqueue(workRequest)
            Logger.log("Scheduled stopSleepDetection through ${Utils.millisToTimeString(delay)}")
        }
    }
}