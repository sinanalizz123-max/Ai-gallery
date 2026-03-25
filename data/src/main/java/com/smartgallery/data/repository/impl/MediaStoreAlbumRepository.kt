package com.smartgallery.data.repository.impl

import android.content.Context
import com.smartgallery.data.datasource.MediaStoreDataSource
import com.smartgallery.data.model.Album
import com.smartgallery.data.model.AlbumType
import com.smartgallery.data.repository.AlbumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MediaStoreAlbumRepository(context: Context) : AlbumRepository {
    private val dataSource = MediaStoreDataSource(context)

    override fun albums(): Flow<List<Album>> = flow {
        val buckets = dataSource.loadAllBuckets()
        val albums = buckets.map { (id, name) ->
            val mediaInBucket = dataSource.loadMediaInBucket(id)
            Album(
                id = id,
                name = name,
                coverUri = mediaInBucket.firstOrNull()?.uri,
                photoCount = mediaInBucket.size,
                type = AlbumType.SYSTEM
            )
        }.sortedByDescending { it.photoCount }
        
        // Add some "AI" albums (placeholders for now)
        val aiAlbums = listOf(
            Album(
                id = "ai_recent",
                name = "Recently Added",
                coverUri = dataSource.loadAllMedia().firstOrNull()?.uri,
                photoCount = dataSource.loadAllMedia().take(50).size,
                type = AlbumType.AI
            )
        )
        
        emit(aiAlbums + albums)
    }.flowOn(Dispatchers.IO)
}
