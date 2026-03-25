package com.smartgallery.data.repository

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    val scanProgress: Flow<Int?>
    val isScanning: Flow<Boolean>
}
