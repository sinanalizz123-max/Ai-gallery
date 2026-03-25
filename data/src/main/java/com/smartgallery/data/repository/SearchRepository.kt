package com.smartgallery.data.repository

import androidx.paging.PagingData
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.model.SearchQuery
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun search(query: SearchQuery): Flow<PagingData<MediaItem>>
}
