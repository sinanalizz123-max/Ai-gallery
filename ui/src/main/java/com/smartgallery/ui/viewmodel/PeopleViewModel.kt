package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgallery.data.model.Person
import com.smartgallery.data.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PeopleViewModel(
    private val peopleRepository: PeopleRepository
) : ViewModel() {
    val people: Flow<List<Person>> = peopleRepository.people()
    val unknownPeople: Flow<List<Person>> = peopleRepository.people().map { list ->
        list.filter { it.isUnknown }
    }

    fun renamePerson(personId: String, newName: String) {
        viewModelScope.launch {
            peopleRepository.renamePerson(personId, newName)
        }
    }

    fun mergePeople(primaryId: String, secondaryId: String) {
        viewModelScope.launch {
            peopleRepository.mergePeople(primaryId, secondaryId)
        }
    }

    fun removeWrongPhotos(personId: String, mediaIds: List<Long>) {
        viewModelScope.launch {
            peopleRepository.removeWrongPhotos(personId, mediaIds)
        }
    }
}
