package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgallery.data.model.FaceBox
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhotoDetailViewModel(
    private val mediaRepository: MediaRepository
) : ViewModel() {
    private val mediaState = MutableStateFlow<MediaItem?>(null)
    private val faceBoxesState = MutableStateFlow<List<FaceBox>>(emptyList())

    val media: StateFlow<MediaItem?> = mediaState
    val faceBoxes: StateFlow<List<FaceBox>> = faceBoxesState

    fun load(mediaId: Long) {
        viewModelScope.launch {
            mediaState.value = mediaRepository.getMediaById(mediaId)
            faceBoxesState.value = listOf(
                FaceBox(0.12f, 0.18f, 0.35f, 0.48f, personId = "Ali", confidence = 0.92f),
                FaceBox(0.52f, 0.2f, 0.78f, 0.5f, personId = "Unknown", confidence = 0.58f)
            )
        }
    }
}
