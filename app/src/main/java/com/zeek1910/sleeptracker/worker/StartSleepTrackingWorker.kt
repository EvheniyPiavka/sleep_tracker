package com.zeek1910.sleeptracker.worker

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zeek1910.sleeptracker.Logger
import com.zeek1910.sleeptracker.receiver.SleepReceiver
import java.util.concurrent.TimeUnit

class StartSleepTrackingWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Logger.log("restart sleep detection")
        SleepReceiver.startSleepDetection(context)
        return Result.success()
    }

    companion object {

        private const val WORK_TAG = "startSleepTracking"

        fun scheduleRestartSleepTracking(context: Context) {
            val workRequest: WorkRequest =
                PeriodicWorkRequestBuilder<StartSleepTrackingWorker>(1, TimeUnit.HOURS)
                    .addTag(WORK_TAG)
                    .build()
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(WORK_TAG)
            workManager.enqueue(workRequest)

            Logger.log("Schedule restart sleep tracking")
        }
    }
}