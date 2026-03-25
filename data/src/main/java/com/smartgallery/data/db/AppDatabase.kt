package com.smartgallery.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartgallery.data.db.dao.CorrectionDao
import com.smartgallery.data.db.dao.EmbeddingDao
import com.smartgallery.data.db.dao.MediaMetadataDao
import com.smartgallery.data.db.dao.PersonDao
import com.smartgallery.data.db.dao.TrainingPairDao
import com.smartgallery.data.db.entity.CorrectionEntity
import com.smartgallery.data.db.entity.EmbeddingEntity
import com.smartgallery.data.db.entity.MediaMetadataEntity
import com.smartgallery.data.db.entity.PersonEntity
import com.smartgallery.data.db.entity.TrainingPairEntity

@Database(
    entities = [
        PersonEntity::class,
        EmbeddingEntity::class,
        CorrectionEntity::class,
        MediaMetadataEntity::class,
        TrainingPairEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun embeddingDao(): EmbeddingDao
    abstract fun correctionDao(): CorrectionDao
    abstract fun metadataDao(): MediaMetadataDao
    abstract fun trainingPairDao(): TrainingPairDao
}
