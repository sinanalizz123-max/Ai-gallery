package com.smartgallery.data.repository

import androidx.paging.PagingData
import com.smartgallery.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun pagedMedia(): Flow<PagingData<MediaItem>>
    suspend fun getMediaById(id: Long): MediaItem?
    fun mediaByPerson(personId: String): Flow<PagingData<MediaItem>>
}
