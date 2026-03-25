package com.smartgallery.data.repository.impl

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smartgallery.data.demo.DemoData
import com.smartgallery.data.model.MediaDetails
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.paging.MediaPagingSource
import com.smartgallery.data.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DemoMediaRepository(
    private val context: Context
) : MediaRepository {
    override fun pagedMedia(): Flow<PagingData<MediaItem>> {
        return Pager(PagingConfig(pageSize = 24)) {
            MediaPagingSource(DemoData.media)
        }.flow
    }

    override suspend fun getMediaById(id: Long): MediaItem? {
        return DemoData.media.find { it.id == id }
    }

    override fun mediaByPerson(personId: String): Flow<PagingData<MediaItem>> {
        val label = when (personId) {
            "p1" -> "Ali"
            "p2" -> "Maya"
            "p3" -> "Unknown"
            "p4" -> "Pet"
            else -> null
        }
        val filtered = if (label == null) DemoData.media else DemoData.media.filter { it.label == label }
        return Pager(PagingConfig(pageSize = 24)) {
            MediaPagingSource(filtered)
        }.flow
    }

    override fun mediaInAlbum(albumId: String): Flow<PagingData<MediaItem>> {
        // Just return all media for demo albums
        return pagedMedia()
    }

    override suspend fun getMediaDetails(id: Long): MediaDetails? {
        val media = getMediaById(id) ?: return null
        return MediaDetails(
            id = media.id,
            uri = media.uri,
            displayName = media.displayName,
            dateTakenEpochMillis = media.dateTakenEpochMillis,
            mimeType = "image/jpeg",
            sizeBytes = 1024
        )
    }

    override suspend fun renameMedia(id: Long, newName: String): Boolean = false

    override suspend fun deleteMedia(id: Long): Boolean = false

    override suspend fun updateMetadata(mediaId: Long, tags: List<String>, notes: String) {}

    override fun observeMetadata(mediaId: Long): Flow<com.smartgallery.data.db.entity.MediaMetadataEntity?> {
        return flowOf(null)
    }
}
