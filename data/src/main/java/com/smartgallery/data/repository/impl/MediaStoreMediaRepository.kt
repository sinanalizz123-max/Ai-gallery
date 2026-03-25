package com.smartgallery.data.repository.impl

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.smartgallery.data.datasource.MediaStoreDataSource
import com.smartgallery.data.db.dao.EmbeddingDao
import com.smartgallery.data.db.dao.MediaMetadataDao
import com.smartgallery.data.db.dao.PersonDao
import com.smartgallery.data.db.entity.MediaMetadataEntity
import com.smartgallery.data.model.MediaDetails
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.paging.MediaPagingSource
import com.smartgallery.data.paging.MediaStorePagingSource
import com.smartgallery.data.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class MediaStoreMediaRepository(
    context: Context,
    private val embeddingDao: EmbeddingDao? = null,
    private val personDao: PersonDao? = null,
    private val metadataDao: MediaMetadataDao? = null
) : MediaRepository {
    private val dataSource = MediaStoreDataSource(context)

    private val labelMapFlow: Flow<Map<Long, String>>? = if (embeddingDao != null && personDao != null) {
        combine(embeddingDao.observeAll(), personDao.observePeople()) { embeddings, people ->
            val nameById = people.associate { it.id to it.name }
            embeddings
                .filter { it.personId != null }
                .groupBy { it.mediaId }
                .mapValues { (_, list) ->
                    list.firstNotNullOfOrNull { entity ->
                        entity.personId?.let { nameById[it] }
                    } ?: "Unknown"
                }
        }
    } else {
        null
    }

    override fun pagedMedia(): Flow<PagingData<MediaItem>> {
        val base = Pager(PagingConfig(pageSize = 30)) {
            MediaStorePagingSource(dataSource)
        }.flow
        val labels = labelMapFlow ?: return base
        return combine(base, labels) { paging, labelMap ->
            paging.map { item -> item.copy(label = labelMap[item.id]) }
        }
    }

    override suspend fun getMediaById(id: Long): MediaItem? {
        return dataSource.loadAllMedia().firstOrNull { it.id == id }
    }

    override fun mediaByPerson(personId: String): Flow<PagingData<MediaItem>> {
        val dao = embeddingDao ?: return pagedMedia()
        val base = dao.embeddingsForPerson(personId).map { embeddings ->
            val ids = embeddings.map { it.mediaId }.toSet()
            dataSource.loadAllMedia().filter { it.id in ids }
        }.flatMapLatest { filtered ->
            Pager(PagingConfig(pageSize = 30)) {
                MediaPagingSource(filtered)
            }.flow
        }
        val labels = labelMapFlow ?: return base
        return combine(base, labels) { paging, labelMap ->
            paging.map { item -> item.copy(label = labelMap[item.id]) }
        }
    }

    override suspend fun getMediaDetails(id: Long): MediaDetails? {
        return dataSource.getMediaDetails(id)
    }

    override suspend fun renameMedia(id: Long, newName: String): Boolean {
        return dataSource.renameMedia(id, newName)
    }

    override suspend fun deleteMedia(id: Long): Boolean {
        return dataSource.deleteMedia(id)
    }

    override suspend fun updateMetadata(mediaId: Long, tags: List<String>, notes: String) {
        metadataDao?.upsert(
            MediaMetadataEntity(
                mediaId = mediaId,
                tags = tags.joinToString(","),
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override fun observeMetadata(mediaId: Long): Flow<MediaMetadataEntity?> {
        return metadataDao?.observeMetadata(mediaId) ?: flowOf(null)
    }
}
