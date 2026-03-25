package com.smartgallery.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "embeddings")
data class EmbeddingEntity(
    @PrimaryKey val id: String,
    val mediaId: Long,
    val personId: String?,
    val vector: ByteArray,
    val confidence: Float,
    val left: Float? = null,
    val top: Float? = null,
    val right: Float? = null,
    val bottom: Float? = null,
    val createdAt: Long
)
