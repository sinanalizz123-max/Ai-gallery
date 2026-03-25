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

    @Query("SELECT * FROM embeddings")
    suspend fun getAll(): List<EmbeddingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(embedding: EmbeddingEntity)
}
