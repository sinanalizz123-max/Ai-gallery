package com.smartgallery.ai.di

import android.content.Context
import androidx.room.Room
import com.smartgallery.ai.engine.FaceEmbeddingPipeline
import com.smartgallery.ai.engine.GroupingLogic
import com.smartgallery.ai.engine.LocalAiEngine
import com.smartgallery.ai.engine.MediaPipeFaceDetector
import com.smartgallery.ai.engine.SmartAiEngine
import com.smartgallery.ai.engine.TfliteFaceRecognizer
import com.smartgallery.data.db.AppDatabase
import com.smartgallery.data.repository.AlbumRepository
import com.smartgallery.data.repository.FaceRepository
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.PeopleRepository
import com.smartgallery.data.repository.SearchRepository
import com.smartgallery.data.repository.impl.DemoAlbumRepository
import com.smartgallery.data.repository.impl.MediaStoreMediaRepository
import com.smartgallery.data.repository.impl.MediaStoreSearchRepository
import com.smartgallery.data.repository.impl.RoomFaceRepository
import com.smartgallery.data.repository.impl.RoomPeopleRepository

interface AppContainer {
    val mediaRepository: MediaRepository
    val peopleRepository: PeopleRepository
    val albumRepository: AlbumRepository
    val searchRepository: SearchRepository
    val aiEngine: SmartAiEngine
    val faceEmbeddingPipeline: FaceEmbeddingPipeline
    val faceRepository: FaceRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
        .fallbackToDestructiveMigration()
        .build()
    private val embeddingDao = database.embeddingDao()
    private val personDao = database.personDao()

    override val mediaRepository: MediaRepository = MediaStoreMediaRepository(
        context = context,
        embeddingDao = embeddingDao,
        personDao = personDao,
        metadataDao = database.metadataDao()
    )
    override val peopleRepository: PeopleRepository = RoomPeopleRepository(
        context = context,
        personDao = personDao,
        embeddingDao = embeddingDao,
        correctionDao = database.correctionDao(),
        metadataDao = database.metadataDao(),
        trainingPairDao = database.trainingPairDao()
    )
    override val albumRepository: AlbumRepository = com.smartgallery.data.repository.impl.MediaStoreAlbumRepository(context)
    override val searchRepository: SearchRepository = MediaStoreSearchRepository(context)
    override val aiEngine: SmartAiEngine = LocalAiEngine(
        context = context,
        trainingPairDao = database.trainingPairDao(),
        embeddingDao = database.embeddingDao()
    )

    override val faceEmbeddingPipeline: FaceEmbeddingPipeline = FaceEmbeddingPipeline(
        faceDetector = MediaPipeFaceDetector(context),
        faceRecognizer = TfliteFaceRecognizer(context)
    )

    override val faceRepository: FaceRepository = RoomFaceRepository(embeddingDao, personDao)

    private companion object {
        private const val DB_NAME = "smartgallery.db"
    }
}
