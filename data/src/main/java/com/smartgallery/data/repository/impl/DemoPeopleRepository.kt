package com.smartgallery.data.repository.impl

import com.smartgallery.data.demo.DemoData
import com.smartgallery.data.model.Person
import com.smartgallery.data.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class DemoPeopleRepository : PeopleRepository {
    private val peopleState = MutableStateFlow(DemoData.people)

    override fun people(): Flow<List<Person>> = peopleState.asStateFlow()

    override fun unknownPeople(): Flow<List<Person>> =
        peopleState.asStateFlow().map { list -> list.filter { it.isUnknown } }

    override suspend fun renamePerson(personId: String, newName: String) {
        peopleState.value = peopleState.value.map {
            if (it.id == personId) it.copy(name = newName, isUnknown = false) else it
        }
    }

    override suspend fun mergePeople(primaryId: String, secondaryId: String) {
        peopleState.value = peopleState.value.filterNot { it.id == secondaryId }
    }

    override suspend fun removeWrongPhotos(personId: String, mediaIds: List<Long>) {
        peopleState.value = peopleState.value.map {
            if (it.id == personId) it.copy(photoCount = (it.photoCount - mediaIds.size).coerceAtLeast(0)) else it
        }
    }

    override suspend fun clearAiData() {
        peopleState.value = emptyList()
    }

    override suspend fun rescanGallery() {
        // No-op for demo
    }
}
