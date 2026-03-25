package com.smartgallery.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_metadata")
data class MediaMetadataEntity(
    @PrimaryKey val mediaId: Long,
    val tags: String, // Comma-separated or JSON
    val notes: String,
    val updatedAt: Long
)
