package com.zeek1910.sleeptracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zeek1910.sleeptracker.Logger
import com.zeek1910.sleeptracker.db.AppDatabase
import com.zeek1910.sleeptracker.db.SleepSegmentEventEntity
import kotlinx.coroutines.flow.Flow

class ProcessSleepDataWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val database = AppDatabase.getDatabase(appContext)

    override suspend fun doWork(): Result {
        Logger.log("Process sleep data")
        processSleepData()
        Logger.log("Sleep data processed")
        return Result.success()
    }

    private suspend fun processSleepData() {
        val rawData = database.sleepClassifyEventDao().getUnprocessedEvents()
        if (rawData.isEmpty()) return

        val sleepPeriods = mutableListOf<Pair<Long, Long>>()
        var currentSleepStart: Long? = null

        for (event in rawData) {
            if (event.confidence > SLEEP_START_THRESHOLD) {
                if (currentSleepStart == null) {
                    currentSleepStart = event.timestampMillis
                }
            } else if (event.confidence < SLEEP_END_THRESHOLD) {
                if (currentSleepStart != null) {
                    val duration = event.timestampMillis - currentSleepStart
                    if (duration > MIN_SLEEP_DURATION) {
                        sleepPeriods.add(Pair(currentSleepStart, event.timestampMillis))
                    }
                    currentSleepStart = null
                }
            }
        }

        sleepPeriods.forEach { (start, end) ->
            database.sleepSegmentEventDao().insert(SleepSegmentEventEntity(start, end))
        }

        database.sleepClassifyEventDao().markAllAsProcessed()
    }

    companion object {
        private const val SLEEP_START_THRESHOLD = 90
        private const val SLEEP_END_THRESHOLD = 60
        private const val MIN_SLEEP_DURATION = 120 * 60 * 1000 // 2 hours

        fun start(context: Context): Flow<WorkInfo?> {
            val workManager = WorkManager.getInstance(context)
            val workRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ProcessSleepDataWorker>()
                    .build()
            workManager.enqueueUniqueWork(
                "processSleepData",
                androidx.work.ExistingWorkPolicy.KEEP,
                workRequest
            )
            return workManager.getWorkInfoByIdFlow(workRequest.id)
        }
    }
}