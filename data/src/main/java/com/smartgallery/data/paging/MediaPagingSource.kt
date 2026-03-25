package com.smartgallery.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smartgallery.data.model.MediaItem

class MediaPagingSource(
    private val items: List<MediaItem>
) : PagingSource<Int, MediaItem>() {

    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        val page = params.key ?: 0
        val from = page * params.loadSize
        val to = minOf(from + params.loadSize, items.size)
        if (from >= items.size) {
            return LoadResult.Page(emptyList(), prevKey = if (page == 0) null else page - 1, nextKey = null)
        }
        val slice = items.subList(from, to)
        return LoadResult.Page(
            data = slice,
            prevKey = if (page == 0) null else page - 1,
            nextKey = if (to >= items.size) null else page + 1
        )
    }
}
