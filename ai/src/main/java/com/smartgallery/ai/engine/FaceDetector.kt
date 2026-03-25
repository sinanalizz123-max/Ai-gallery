package com.smartgallery.ai.engine

import android.graphics.Bitmap
import com.smartgallery.data.model.FaceBox

interface FaceDetector {
    suspend fun detectFaces(bitmap: Bitmap): List<FaceBox>
}
