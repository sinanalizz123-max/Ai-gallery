package com.smartgallery.ai.engine

import com.smartgallery.data.model.FaceEmbedding
import java.nio.ByteBuffer

interface FaceRecognizer {
    suspend fun extractEmbedding(inputBuffer: ByteBuffer): FaceEmbedding
}
