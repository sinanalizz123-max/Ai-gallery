package com.smartgallery.data.model

data class FaceEmbedding(
    val id: String,
    val vector: FloatArray,
    val personId: String? = null,
    val confidence: Float = 0f
)
