package com.smartgallery.ai.model

import android.net.Uri

data class TrainingPair(
    val id: String,
    val leftFaceUri: Uri,
    val rightFaceUri: Uri,
    val confidence: Float
)
