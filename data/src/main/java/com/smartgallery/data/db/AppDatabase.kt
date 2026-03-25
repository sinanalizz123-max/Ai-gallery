package com.smartgallery.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartgallery.data.db.dao.CorrectionDao
import com.smartgallery.data.db.dao.EmbeddingDao
import com.smartgallery.data.db.dao.PersonDao
import com.smartgallery.data.db.entity.CorrectionEntity
import com.smartgallery.data.db.entity.EmbeddingEntity
import com.smartgallery.data.db.entity.PersonEntity

@Database(
    entities = [PersonEntity::class, EmbeddingEntity::class, CorrectionEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun embeddingDao(): EmbeddingDao
    abstract fun correctionDao(): CorrectionDao
}
