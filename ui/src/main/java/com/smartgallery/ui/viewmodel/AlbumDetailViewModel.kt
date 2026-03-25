package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.smartgallery.data.model.Album
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.repository.AlbumRepository
import com.smartgallery.data.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class AlbumDetailViewModel(
    private val albumRepository: AlbumRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {
    private val albumIdState = MutableStateFlow<String?>(null)
    private val albumState = MutableStateFlow<Album?>(null)

    val album: StateFlow<Album?> = albumState

    val pagedMedia: Flow<PagingData<MediaItem>> = albumIdState
        .filterNotNull()
        .flatMapLatest { mediaRepository.mediaInAlbum(it) }
        .cachedIn(viewModelScope)

    fun loadAlbum(albumId: String) {
        albumIdState.value = albumId
        viewModelScope.launch {
            albumState.value = albumRepository.getAlbumById(albumId)
        }
    }
}
