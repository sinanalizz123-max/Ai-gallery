package com.smartgallery.ai.engine

import android.net.Uri
import com.smartgallery.ai.model.TrainingPair
import com.smartgallery.data.model.AiStats
import com.smartgallery.data.model.FaceEmbedding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class LocalAiEngine : SmartAiEngine {
    private val trainingItems = MutableStateFlow(
        List(6) {
            TrainingPair(
                leftFaceUri = Uri.parse("file:///android_asset/face_${it % 3}_a.jpg"),
                rightFaceUri = Uri.parse("file:///android_asset/face_${it % 3}_b.jpg"),
                confidence = 0.45f + (it * 0.05f)
            )
        }
    )

    private val statsState = MutableStateFlow(
        AiStats(
            accuracyPercent = 86,
            learnedFaces = 124,
            recentCorrections = 7
        )
    )

    override val trainingQueue: StateFlow<List<TrainingPair>> = trainingItems
    override val stats: StateFlow<AiStats> = statsState

    override fun shouldAskUser(confidence: Float): Boolean = confidence < 0.65f

    override fun suggestMatch(embedding: FaceEmbedding): String? {
        return if (embedding.confidence > 0.8f) listOf("Ali", "Maya", "Noah").random() else null
    }

    override suspend fun submitFeedback(pair: TrainingPair, isSamePerson: Boolean) {
        trainingItems.value = trainingItems.value.drop(1)
        val delta = if (isSamePerson) 1 else 0
        statsState.value = statsState.value.copy(
            accuracyPercent = (statsState.value.accuracyPercent + delta).coerceAtMost(99),
            learnedFaces = statsState.value.learnedFaces + 1,
            recentCorrections = statsState.value.recentCorrections + 1
        )
        if (trainingItems.value.size < 3) {
            trainingItems.value = trainingItems.value + TrainingPair(
                leftFaceUri = Uri.parse("file:///android_asset/face_${Random.nextInt(3)}_a.jpg"),
                rightFaceUri = Uri.parse("file:///android_asset/face_${Random.nextInt(3)}_b.jpg"),
                confidence = 0.5f
            )
        }
    }
}
