package com.smartgallery.data.repository.impl

import com.smartgallery.data.demo.DemoData
import com.smartgallery.data.model.Album
import com.smartgallery.data.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoAlbumRepository : AlbumRepository {
    private val albumsState = MutableStateFlow(DemoData.albums)

    override fun albums(): Flow<List<Album>> = albumsState.asStateFlow()
}
