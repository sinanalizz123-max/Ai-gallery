package com.smartgallery.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_pairs")
data class TrainingPairEntity(
    @PrimaryKey val id: String,
    val embeddingIdA: String,
    val embeddingIdB: String,
    val confidence: Float,
    val isAnswered: Boolean = false,
    val createdAt: Long
)
