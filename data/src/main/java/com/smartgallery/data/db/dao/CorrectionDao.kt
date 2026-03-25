package com.smartgallery.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartgallery.data.db.entity.CorrectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CorrectionDao {
    @Query("SELECT * FROM corrections ORDER BY createdAt DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<CorrectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(correction: CorrectionEntity)
}
