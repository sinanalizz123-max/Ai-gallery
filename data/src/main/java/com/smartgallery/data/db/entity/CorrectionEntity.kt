package com.smartgallery.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "corrections")
data class CorrectionEntity(
    @PrimaryKey val id: String,
    val embeddingId: String,
    val correctedPersonId: String?,
    val createdAt: Long
)
