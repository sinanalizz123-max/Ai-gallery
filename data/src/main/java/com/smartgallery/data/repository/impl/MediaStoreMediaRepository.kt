package com.smartgallery.data.repository.impl

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smartgallery.data.datasource.MediaStoreDataSource
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.paging.MediaStorePagingSource
import com.smartgallery.data.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class MediaStoreMediaRepository(
    context: Context
) : MediaRepository {
    private val dataSource = MediaStoreDataSource(context)

    override fun pagedMedia(): Flow<PagingData<MediaItem>> {
        return Pager(PagingConfig(pageSize = 30)) {
            MediaStorePagingSource(dataSource)
        }.flow
    }

    override suspend fun getMediaById(id: Long): MediaItem? {
        return dataSource.loadAllMedia().firstOrNull { it.id == id }
    }

    override fun mediaByPerson(personId: String): Flow<PagingData<MediaItem>> {
        // TODO: Filter by personId once face recognition is wired.
        return pagedMedia()
    }
}
