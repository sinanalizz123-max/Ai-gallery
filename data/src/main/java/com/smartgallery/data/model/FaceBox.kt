package com.smartgallery.data.model

data class FaceBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val personId: String? = null,
    val confidence: Float = 0f,
    val leftEyeX: Float? = null,
    val leftEyeY: Float? = null,
    val rightEyeX: Float? = null,
    val rightEyeY: Float? = null
)
