package com.smartgallery.ai.engine

import com.smartgallery.data.db.dao.EmbeddingDao
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.math.sqrt

class GroupingLogic(
    private val embeddingDao: EmbeddingDao,
    private val comparator: FaceComparator = FaceComparator()
) {
    suspend fun buildClusters(): List<FaceCluster> {
        val embeddings = embeddingDao.getAll()
        if (embeddings.isEmpty()) return emptyList()

        val clusters = mutableListOf<MutableCluster>()
        embeddings.forEach { entity ->
            val vector = byteArrayToFloatArray(entity.vector)
            val match = clusters.firstOrNull { cluster ->
                comparator.isSamePerson(cluster.centroid, vector)
            }
            if (match == null) {
                clusters.add(
                    MutableCluster(
                        id = UUID.randomUUID().toString(),
                        embeddingIds = mutableListOf(entity.id),
                        centroid = vector.copyOf()
                    )
                )
            } else {
                match.embeddingIds.add(entity.id)
                match.centroid = updateCentroid(match.centroid, vector, match.embeddingIds.size)
            }
        }

        return clusters.mapIndexed { index, cluster ->
            FaceCluster(
                id = cluster.id,
                name = "Person ${index + 1}",
                embeddingIds = cluster.embeddingIds.toList()
            )
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

    private data class MutableCluster(
        val id: String,
        val embeddingIds: MutableList<String>,
        var centroid: FloatArray
    )

    data class FaceCluster(
        val id: String,
        val name: String,
        val embeddingIds: List<String>
    )
}
