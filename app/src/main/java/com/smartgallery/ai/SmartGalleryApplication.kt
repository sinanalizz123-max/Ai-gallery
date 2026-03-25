package com.smartgallery.ai

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smartgallery.ai.workers.FaceRecognitionWorker
import java.util.concurrent.TimeUnit

class SmartGalleryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleFaceRecognition()
    }

    private fun scheduleFaceRecognition() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<FaceRecognitionWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private companion object {
        private const val WORK_NAME = "FaceRecognitionWorker"
    }
}
