package com.smartgallery.data.repository

import com.smartgallery.data.model.Person
import kotlinx.coroutines.flow.Flow

interface PeopleRepository {
    fun people(): Flow<List<Person>>
    fun unknownPeople(): Flow<List<Person>>
    suspend fun renamePerson(personId: String, newName: String)
    suspend fun mergePeople(primaryId: String, secondaryId: String)
    suspend fun removeWrongPhotos(personId: String, mediaIds: List<Long>)
    suspend fun clearAiData()
    suspend fun rescanGallery()
}
