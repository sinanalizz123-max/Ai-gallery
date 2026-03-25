package com.smartgallery.data.repository

import com.smartgallery.data.model.FaceBox
import kotlinx.coroutines.flow.Flow

interface FaceRepository {
    fun faceBoxesForMedia(mediaId: Long): Flow<List<FaceBox>>
}
