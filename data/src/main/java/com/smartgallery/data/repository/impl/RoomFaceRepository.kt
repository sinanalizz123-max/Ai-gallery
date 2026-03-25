package com.smartgallery.data.repository.impl

import com.smartgallery.data.db.dao.EmbeddingDao
import com.smartgallery.data.db.dao.PersonDao
import com.smartgallery.data.model.FaceBox
import com.smartgallery.data.repository.FaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomFaceRepository(
    private val embeddingDao: EmbeddingDao,
    private val personDao: PersonDao
) : FaceRepository {
    override fun faceBoxesForMedia(mediaId: Long): Flow<List<FaceBox>> {
        return combine(
            embeddingDao.embeddingsForMedia(mediaId),
            personDao.observePeople()
        ) { embeddings, people ->
            val nameById = people.associate { it.id to it.name }
            embeddings.mapNotNull { entity ->
                val name = entity.personId?.let { nameById[it] }
                FaceBox(
                    left = entity.left ?: return@mapNotNull null,
                    top = entity.top ?: return@mapNotNull null,
                    right = entity.right ?: return@mapNotNull null,
                    bottom = entity.bottom ?: return@mapNotNull null,
                    personId = name ?: "Unknown",
                    confidence = entity.confidence
                )
            }
        }
    }
}
