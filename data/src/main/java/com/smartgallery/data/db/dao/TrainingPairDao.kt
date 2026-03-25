package com.smartgallery.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartgallery.data.db.entity.TrainingPairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPairDao {
    @Query("SELECT * FROM training_pairs WHERE isAnswered = 0 ORDER BY confidence ASC")
    fun observeUnanswered(): Flow<List<TrainingPairEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pair: TrainingPairEntity)

    @Query("UPDATE training_pairs SET isAnswered = 1 WHERE id = :id")
    suspend fun markAnswered(id: String)

    @Query("DELETE FROM training_pairs")
    suspend fun clearAll()
}
