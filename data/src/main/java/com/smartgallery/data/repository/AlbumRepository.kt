package com.smartgallery.data.repository

import com.smartgallery.data.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun albums(): Flow<List<Album>>
    suspend fun getAlbumById(id: String): Album?
}
