package com.smartgallery.ai.model

import android.net.Uri

data class TrainingPair(
    val leftFaceUri: Uri,
    val rightFaceUri: Uri,
    val confidence: Float
)
