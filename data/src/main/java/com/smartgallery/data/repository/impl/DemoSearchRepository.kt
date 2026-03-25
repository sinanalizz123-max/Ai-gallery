package com.smartgallery.data.repository.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smartgallery.data.demo.DemoData
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.model.SearchQuery
import com.smartgallery.data.paging.MediaPagingSource
import com.smartgallery.data.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class DemoSearchRepository : SearchRepository {
    override fun search(query: SearchQuery): Flow<PagingData<MediaItem>> {
        val filtered = DemoData.media.filter { item ->
            val matchesText = query.text.isBlank() || item.displayName.contains(query.text, true) ||
                item.label?.contains(query.text, true) == true ||
                item.keywords.any { it.contains(query.text, true) }
            val matchesMin = query.minPeople?.let { item.peopleCount >= it } ?: true
            val matchesMax = query.maxPeople?.let { item.peopleCount <= it } ?: true
            matchesText && matchesMin && matchesMax
        }
        return Pager(PagingConfig(pageSize = 24)) {
            MediaPagingSource(filtered)
        }.flow
    }
}
