package com.smartgallery.data.repository

import androidx.paging.PagingData
import com.smartgallery.data.model.MediaDetails
import com.smartgallery.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun pagedMedia(): Flow<PagingData<MediaItem>>
    suspend fun getMediaById(id: Long): MediaItem?
    fun mediaByPerson(personId: String): Flow<PagingData<MediaItem>>
    suspend fun getMediaDetails(id: Long): MediaDetails?
    suspend fun renameMedia(id: Long, newName: String): Boolean
    suspend fun deleteMedia(id: Long): Boolean

    suspend fun updateMetadata(mediaId: Long, tags: List<String>, notes: String)
    fun observeMetadata(mediaId: Long): Flow<com.smartgallery.data.db.entity.MediaMetadataEntity?>
}
