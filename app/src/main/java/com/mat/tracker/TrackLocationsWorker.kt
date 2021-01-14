package com.mat.tracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class TrackLocationsWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val repository: Repository by inject()

    override suspend fun doWork(): Result {
        val interval = inputData.getInt(INTERVAL, DEFAULT_INTERVAL_SECONDS)
        while (true) {
            repository.getAndSaveCurrentLocation()
            delay(interval * SECOND)
        }
    }

    companion object {
        const val INTERVAL = "interval"
        private const val SECOND = 1000L
        private const val DEFAULT_INTERVAL_SECONDS = 60
    }
}