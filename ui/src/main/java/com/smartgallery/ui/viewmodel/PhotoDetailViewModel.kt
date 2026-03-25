package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgallery.data.model.FaceBox
import com.smartgallery.data.model.MediaDetails
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.repository.FaceRepository
import com.smartgallery.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

class PhotoDetailViewModel(
    private val mediaRepository: MediaRepository,
    private val faceRepository: FaceRepository
) : ViewModel() {
    private val mediaState = MutableStateFlow<MediaItem?>(null)
    private val detailsState = MutableStateFlow<MediaDetails?>(null)
    private val mediaIdState = MutableStateFlow<Long?>(null)

    val media: StateFlow<MediaItem?> = mediaState
    val details: StateFlow<MediaDetails?> = detailsState

    val faceBoxes: StateFlow<List<FaceBox>> = mediaIdState
        .filterNotNull()
        .flatMapLatest { faceRepository.faceBoxesForMedia(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metadata: StateFlow<com.smartgallery.data.db.entity.MediaMetadataEntity?> = mediaIdState
        .filterNotNull()
        .flatMapLatest { mediaRepository.observeMetadata(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(mediaId: Long) {
        viewModelScope.launch {
            mediaIdState.value = mediaId
            mediaState.value = mediaRepository.getMediaById(mediaId)
            detailsState.value = mediaRepository.getMediaDetails(mediaId)
        }
    }

    fun rename(newName: String, onResult: (Boolean) -> Unit) {
        val id = mediaIdState.value ?: return
        viewModelScope.launch {
            val success = mediaRepository.renameMedia(id, newName)
            detailsState.value = mediaRepository.getMediaDetails(id)
            mediaState.value = mediaRepository.getMediaById(id)
            onResult(success)
        }
    }

    fun delete(onResult: (Boolean) -> Unit) {
        val id = mediaIdState.value ?: return
        viewModelScope.launch {
            val success = mediaRepository.deleteMedia(id)
            onResult(success)
        }
    }

    fun updateMetadata(tags: List<String>, notes: String) {
        val id = mediaIdState.value ?: return
        viewModelScope.launch {
            mediaRepository.updateMetadata(id, tags, notes)
        }
    }
}
