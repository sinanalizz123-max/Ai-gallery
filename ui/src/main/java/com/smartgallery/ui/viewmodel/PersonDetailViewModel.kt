package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow

class PersonDetailViewModel(
    private val mediaRepository: MediaRepository,
    private val peopleRepository: PeopleRepository
) : ViewModel() {
    fun mediaForPerson(personId: String): Flow<PagingData<MediaItem>> {
        return mediaRepository.mediaByPerson(personId).cachedIn(viewModelScope)
    }

    suspend fun rename(personId: String, newName: String) {
        peopleRepository.renamePerson(personId, newName)
    }

    suspend fun merge(primaryId: String, secondaryId: String) {
        peopleRepository.mergePeople(primaryId, secondaryId)
    }

    suspend fun removeWrong(personId: String, mediaIds: List<Long>) {
        peopleRepository.removeWrongPhotos(personId, mediaIds)
    }
}
