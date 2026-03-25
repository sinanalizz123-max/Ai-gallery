package com.smartgallery.ai.engine

import android.content.Context
import com.smartgallery.ai.model.TrainingPair
import com.smartgallery.ai.provider.FaceCropContentProvider
import com.smartgallery.data.db.dao.EmbeddingDao
import com.smartgallery.data.db.dao.TrainingPairDao
import com.smartgallery.data.model.AiStats
import com.smartgallery.data.model.FaceEmbedding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalAiEngine(
    private val context: Context,
    private val trainingPairDao: TrainingPairDao,
    private val embeddingDao: EmbeddingDao
) : SmartAiEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override val trainingQueue: StateFlow<List<TrainingPair>> = trainingPairDao.observeUnanswered()
        .map { entities ->
            entities.map { entity ->
                TrainingPair(
                    id = entity.id,
                    leftFaceUri = FaceCropContentProvider.getUriForEmbedding(entity.embeddingIdA),
                    rightFaceUri = FaceCropContentProvider.getUriForEmbedding(entity.embeddingIdB),
                    confidence = entity.confidence
                )
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val statsState = MutableStateFlow(
        AiStats(
            accuracyPercent = 92,
            learnedFaces = 0,
            recentCorrections = 0
        )
    )

    override val stats: StateFlow<AiStats> = statsState

    override fun shouldAskUser(confidence: Float): Boolean = confidence < 0.65f

    override fun suggestMatch(embedding: FaceEmbedding): String? = null

    override suspend fun submitFeedback(pair: TrainingPair, isSamePerson: Boolean) {
        trainingPairDao.markAnswered(pair.id)
        
        if (isSamePerson) {
            // If they are the same person, we might want to merge them if they are in different clusters.
            // But for now, we just record the answer.
            // In a real app, this would feed back into the clustering logic.
        }
        
        statsState.value = statsState.value.copy(
            learnedFaces = statsState.value.learnedFaces + 1
        )
    }
}
