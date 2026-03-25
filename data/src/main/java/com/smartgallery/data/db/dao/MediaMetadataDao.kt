package com.smartgallery.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartgallery.data.db.entity.MediaMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaMetadataDao {
    @Query("SELECT * FROM media_metadata WHERE mediaId = :mediaId")
    fun observeMetadata(mediaId: Long): Flow<MediaMetadataEntity?>

    @Query("SELECT * FROM media_metadata WHERE mediaId = :mediaId")
    suspend fun getMetadata(mediaId: Long): MediaMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: MediaMetadataEntity)

    @Query("DELETE FROM media_metadata WHERE mediaId = :mediaId")
    suspend fun delete(mediaId: Long)

    @Query("DELETE FROM media_metadata")
    suspend fun clearAll()
}
