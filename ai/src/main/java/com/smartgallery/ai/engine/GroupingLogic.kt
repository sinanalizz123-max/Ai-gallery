package com.smartgallery.ai.engine

import com.smartgallery.data.db.dao.EmbeddingDao
import com.smartgallery.data.db.dao.PersonDao
import com.smartgallery.data.db.entity.PersonEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.math.sqrt

class GroupingLogic(
    private val embeddingDao: EmbeddingDao,
    private val personDao: PersonDao,
    private val trainingPairDao: com.smartgallery.data.db.dao.TrainingPairDao? = null,
    private val comparator: FaceComparator = FaceComparator()
) {
    suspend fun buildAndPersistClusters(): List<FaceCluster> {
        val embeddings = embeddingDao.getAll()
        if (embeddings.isEmpty()) return emptyList()

        val existingPeople = personDao.getAll()
        val existingById = existingPeople.associateBy { it.id }
        val nextIndex = nextPersonIndex(existingPeople)

        val clusters = mutableMapOf<String, MutableCluster>()

        // Seed clusters with existing person assignments
        embeddings.filter { it.personId != null }.forEach { entity ->
            val personId = entity.personId ?: return@forEach
            val vector = byteArrayToFloatArray(entity.vector)
            val cluster = clusters.getOrPut(personId) {
                MutableCluster(
                    id = personId,
                    embeddingIds = mutableListOf(),
                    centroid = vector.copyOf(),
                    count = 0
                )
            }
            cluster.embeddingIds.add(entity.id)
            cluster.count += 1
            cluster.centroid = updateCentroid(cluster.centroid, vector, cluster.count)
        }

        var nextPersonNumber = nextIndex
        // Assign unclustered embeddings
        embeddings.filter { it.personId == null }.forEach { entity ->
            val vector = byteArrayToFloatArray(entity.vector)
            val match = clusters.values.firstOrNull { cluster ->
                comparator.isSamePerson(cluster.centroid, vector)
            }
            val cluster = if (match == null) {
                val newId = UUID.randomUUID().toString()
                val created = MutableCluster(
                    id = newId,
                    embeddingIds = mutableListOf(),
                    centroid = vector.copyOf(),
                    count = 0
                )
                clusters[newId] = created
                created
            } else {
                match
            }
            cluster.embeddingIds.add(entity.id)
            cluster.count += 1
            cluster.centroid = updateCentroid(cluster.centroid, vector, cluster.count)
        }

        // Persist assignments and people
        val activePersonIds = clusters.keys
        existingPeople.filter { it.id !in activePersonIds }.forEach { person ->
            personDao.delete(person.id)
        }

        clusters.values.forEach { cluster ->
            if (cluster.embeddingIds.isNotEmpty()) {
                embeddingDao.assignPerson(cluster.id, cluster.embeddingIds)
            }
            val existing = existingById[cluster.id]
            val person = if (existing != null) {
                existing.copy(updatedAt = System.currentTimeMillis())
            } else {
                val name = "Person $nextPersonNumber"
                nextPersonNumber += 1
                PersonEntity(
                    id = cluster.id,
                    name = name,
                    isUnknown = true,
                    updatedAt = System.currentTimeMillis()
                )
            }
            personDao.upsert(person)
        }

        return clusters.values.mapIndexed { index, cluster ->
            val name = existingById[cluster.id]?.name ?: "Person ${index + 1}"
            FaceCluster(
                id = cluster.id,
                name = name,
                embeddingIds = cluster.embeddingIds.toList()
            )
        }
    }

    suspend fun generateUncertainPairs() {
        val dao = trainingPairDao ?: return
        val embeddings = embeddingDao.getAll()
        if (embeddings.size < 2) return

        // To avoid N^2, we could just look at recent ones or a subset.
        val recent = embeddings.takeLast(100)
        
        for (i in recent.indices) {
            val a = recent[i]
            for (j in i + 1 until recent.size) {
                val b = recent[j]
                if (a.mediaId == b.mediaId) continue

                val vecA = byteArrayToFloatArray(a.vector)
                val vecB = byteArrayToFloatArray(b.vector)
                val dist = comparator.squaredEuclidean(vecA, vecB)
                
                if (dist in 0.8f..1.2f) {
                    dao.insert(
                        com.smartgallery.data.db.entity.TrainingPairEntity(
                            id = UUID.randomUUID().toString(),
                            embeddingIdA = a.id,
                            embeddingIdB = b.id,
                            confidence = dist,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun updateCentroid(current: FloatArray, next: FloatArray, count: Int): FloatArray {
        val updated = FloatArray(current.size)
        val ratio = 1f / count
        for (i in current.indices) {
            updated[i] = current[i] * (1f - ratio) + next[i] * ratio
        }
        return l2Normalize(updated)
    }

    private fun l2Normalize(vector: FloatArray): FloatArray {
        var sum = 0f
        for (v in vector) {
            sum += v * v
        }
        val norm = sqrt(sum).coerceAtLeast(1e-6f)
        return FloatArray(vector.size) { index -> vector[index] / norm }
    }

    private fun byteArrayToFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder())
        val floats = FloatArray(bytes.size / 4)
        var i = 0
        while (buffer.remaining() >= 4 && i < floats.size) {
            floats[i++] = buffer.getFloat()
        }
        return floats
    }

    private fun nextPersonIndex(existing: List<PersonEntity>): Int {
        val max = existing.mapNotNull { entity ->
            val match = PERSON_NAME_REGEX.find(entity.name) ?: return@mapNotNull null
            match.groupValues.getOrNull(1)?.toIntOrNull()
        }.maxOrNull() ?: 0
        return max + 1
    }

    private data class MutableCluster(
        val id: String,
        val embeddingIds: MutableList<String>,
        var centroid: FloatArray,
        var count: Int
    )

    data class FaceCluster(
        val id: String,
        val name: String,
        val embeddingIds: List<String>
    )

    private companion object {
        private val PERSON_NAME_REGEX = Regex("Person\\s+(\\d+)")
    }
}
