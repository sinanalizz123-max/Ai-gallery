package com.smartgallery.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartgallery.data.db.entity.EmbeddingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmbeddingDao {
    @Query("SELECT * FROM embeddings WHERE personId = :personId")
    fun embeddingsForPerson(personId: String): Flow<List<EmbeddingEntity>>

    @Query("SELECT * FROM embeddings WHERE mediaId = :mediaId")
    fun embeddingsForMedia(mediaId: Long): Flow<List<EmbeddingEntity>>

    @Query("SELECT * FROM embeddings")
    fun observeAll(): Flow<List<EmbeddingEntity>>

    @Query("SELECT * FROM embeddings")
    suspend fun getAll(): List<EmbeddingEntity>

    @Query("SELECT DISTINCT mediaId FROM embeddings WHERE personId = :personId")
    suspend fun mediaIdsForPerson(personId: String): List<Long>

    @Query("SELECT * FROM embeddings ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<EmbeddingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(embedding: EmbeddingEntity)

    @Query("UPDATE embeddings SET personId = :personId WHERE id IN (:embeddingIds)")
    suspend fun assignPerson(personId: String, embeddingIds: List<String>)

    @Query("UPDATE embeddings SET personId = :newPersonId WHERE personId = :oldPersonId")
    suspend fun reassignPerson(oldPersonId: String, newPersonId: String)

    @Query("UPDATE embeddings SET personId = NULL WHERE personId = :personId AND mediaId IN (:mediaIds)")
    suspend fun clearPersonFromMedia(personId: String, mediaIds: List<Long>)

    @Query("DELETE FROM embeddings")
    suspend fun clearAll()
}
