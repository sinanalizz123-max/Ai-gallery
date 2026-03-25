package com.smartgallery.ai.model

import com.smartgallery.data.model.FaceBox
import com.smartgallery.data.model.FaceEmbedding

/**
 * Output of the face detection + embedding pipeline.
 */
data class FaceAnalysis(
    val box: FaceBox,
    val embedding: FaceEmbedding
)
