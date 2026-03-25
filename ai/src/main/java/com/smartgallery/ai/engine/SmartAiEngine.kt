package com.smartgallery.ai.engine

import com.smartgallery.ai.model.TrainingPair
import com.smartgallery.data.model.AiStats
import com.smartgallery.data.model.FaceEmbedding
import kotlinx.coroutines.flow.StateFlow

interface SmartAiEngine {
    val trainingQueue: StateFlow<List<TrainingPair>>
    val stats: StateFlow<AiStats>

    fun shouldAskUser(confidence: Float): Boolean
    fun suggestMatch(embedding: FaceEmbedding): String?
    suspend fun submitFeedback(pair: TrainingPair, isSamePerson: Boolean)
}
