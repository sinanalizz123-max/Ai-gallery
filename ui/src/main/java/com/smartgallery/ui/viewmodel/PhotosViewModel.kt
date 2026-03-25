package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.smartgallery.data.model.MediaItem
import com.smartgallery.data.model.SearchQuery
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class PhotosViewModel(
    private val mediaRepository: MediaRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {
    private val searchState = MutableStateFlow(SearchQuery())

    val pagedMedia: Flow<PagingData<MediaItem>> = searchState.flatMapLatest { query ->
        if (query.text.isBlank() && query.minPeople == null && query.maxPeople == null) {
            mediaRepository.pagedMedia()
        } else {
            searchRepository.search(query)
        }
    }.cachedIn(viewModelScope)

    val currentQuery = searchState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SearchQuery()
    )

    fun updateQuery(text: String) {
        searchState.value = searchState.value.copy(text = text)
    }

    fun updatePeopleFilter(min: Int?, max: Int?) {
        searchState.value = searchState.value.copy(minPeople = min, maxPeople = max)
    }
}
