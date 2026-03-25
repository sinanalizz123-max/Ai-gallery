package com.smartgallery.data.repository.impl

import android.content.Context
import com.smartgallery.data.datasource.MediaStoreDataSource
import com.smartgallery.data.db.dao.EmbeddingDao
import com.smartgallery.data.db.dao.PersonDao
import com.smartgallery.data.db.entity.PersonEntity
import com.smartgallery.data.model.Person
import com.smartgallery.data.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RoomPeopleRepository(
    private val context: Context,
    private val personDao: PersonDao,
    private val embeddingDao: EmbeddingDao,
    private val correctionDao: com.smartgallery.data.db.dao.CorrectionDao,
    private val metadataDao: com.smartgallery.data.db.dao.MediaMetadataDao,
    private val trainingPairDao: com.smartgallery.data.db.dao.TrainingPairDao
) : PeopleRepository {
    private val mediaStore = MediaStoreDataSource(context)

    override fun people(): Flow<List<Person>> {
        return combine(personDao.observePeople(), embeddingDao.observeAll()) { people, embeddings ->
            val embeddingsByPerson = embeddings.filter { it.personId != null }.groupBy { it.personId!! }
            people.mapNotNull { personEntity ->
                val personEmbeddings = embeddingsByPerson[personEntity.id].orEmpty()
                if (personEmbeddings.isEmpty()) return@mapNotNull null
                val coverMediaId = personEmbeddings.maxByOrNull { it.createdAt }?.mediaId
                Person(
                    id = personEntity.id,
                    name = personEntity.name,
                    coverUri = coverMediaId?.let { mediaStore.uriFromStableId(it) },
                    photoCount = personEmbeddings.map { it.mediaId }.distinct().size,
                    isUnknown = personEntity.isUnknown
                )
            }.sortedByDescending { it.photoCount }
        }
    }

    override fun unknownPeople(): Flow<List<Person>> {
        return people().map { list -> list.filter { it.isUnknown } }
    }

    override suspend fun renamePerson(personId: String, newName: String) {
        personDao.upsert(
            PersonEntity(
                id = personId,
                name = newName,
                isUnknown = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun mergePeople(primaryId: String, secondaryId: String) {
        embeddingDao.reassignPerson(secondaryId, primaryId)
        personDao.delete(secondaryId)
    }

    override suspend fun removeWrongPhotos(personId: String, mediaIds: List<Long>) {
        if (mediaIds.isEmpty()) return
        embeddingDao.clearPersonFromMedia(personId, mediaIds)
    }

    override suspend fun clearAiData() {
        embeddingDao.clearAll()
        personDao.clearAll()
        correctionDao.clearAll()
        metadataDao.clearAll()
        trainingPairDao.clearAll()
        resetScanState()
    }

    override suspend fun rescanGallery() {
        resetScanState()
    }

    private fun resetScanState() {
        context.getSharedPreferences("face_recognition_worker", Context.MODE_PRIVATE)
            .edit()
            .remove("last_scan_epoch")
            .apply()
    }
}
