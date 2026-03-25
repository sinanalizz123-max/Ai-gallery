package com.smartgallery.ai.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FaceScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // TODO: scan gallery when device is idle and power is sufficient.
        return Result.success()
    }
}
