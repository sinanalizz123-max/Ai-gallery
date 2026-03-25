package com.smartgallery.data.repository.impl

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smartgallery.data.datasource.MediaStoreDataSource
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.model.SearchQuery
import com.smartgallery.data.paging.MediaPagingSource
import com.smartgallery.data.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class MediaStoreSearchRepository(
    context: Context
) : SearchRepository {
    private val dataSource = MediaStoreDataSource(context)

    override fun search(query: SearchQuery): Flow<PagingData<MediaItem>> {
        val filtered = dataSource.loadAllMedia().filter { item ->
            val matchesText = query.text.isBlank() ||
                item.displayName.contains(query.text, true)
            matchesText
        }
        return Pager(PagingConfig(pageSize = 30)) {
            MediaPagingSource(filtered)
        }.flow
    }
}
