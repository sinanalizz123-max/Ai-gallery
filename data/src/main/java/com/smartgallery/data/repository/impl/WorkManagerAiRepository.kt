package com.smartgallery.data.repository.impl

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.smartgallery.data.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkManagerAiRepository(context: Context) : AiRepository {
    private val workManager = WorkManager.getInstance(context)

    override val scanProgress: Flow<Int?> = workManager
        .getWorkInfosForUniqueWorkFlow("FaceRecognitionWorker")
        .map { workInfos ->
            val workInfo = workInfos.firstOrNull { 
                it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED 
            }
            workInfo?.progress?.getInt("progress", 0)
        }

    override val isScanning: Flow<Boolean> = workManager
        .getWorkInfosForUniqueWorkFlow("FaceRecognitionWorker")
        .map { workInfos ->
            workInfos.any { it.state == WorkInfo.State.RUNNING }
        }
}
