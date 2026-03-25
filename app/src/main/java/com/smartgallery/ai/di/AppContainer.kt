package com.smartgallery.ai.di

import android.content.Context
import com.smartgallery.ai.engine.FaceEmbeddingPipeline
import com.smartgallery.ai.engine.LocalAiEngine
import com.smartgallery.ai.engine.MediaPipeFaceDetector
import com.smartgallery.ai.engine.SmartAiEngine
import com.smartgallery.ai.engine.TfliteFaceRecognizer
import com.smartgallery.data.repository.AlbumRepository
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.PeopleRepository
import com.smartgallery.data.repository.SearchRepository
import com.smartgallery.data.repository.impl.DemoAlbumRepository
import com.smartgallery.data.repository.impl.DemoPeopleRepository
import com.smartgallery.data.repository.impl.DemoSearchRepository
import com.smartgallery.data.repository.impl.MediaStoreMediaRepository

interface AppContainer {
    val mediaRepository: MediaRepository
    val peopleRepository: PeopleRepository
    val albumRepository: AlbumRepository
    val searchRepository: SearchRepository
    val aiEngine: SmartAiEngine
    val faceEmbeddingPipeline: FaceEmbeddingPipeline
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val mediaRepository: MediaRepository = MediaStoreMediaRepository(context)
    override val peopleRepository: PeopleRepository = DemoPeopleRepository()
    override val albumRepository: AlbumRepository = DemoAlbumRepository()
    override val searchRepository: SearchRepository = DemoSearchRepository()
    override val aiEngine: SmartAiEngine = LocalAiEngine()

    override val faceEmbeddingPipeline: FaceEmbeddingPipeline = FaceEmbeddingPipeline(
        faceDetector = MediaPipeFaceDetector(context),
        faceRecognizer = TfliteFaceRecognizer(context)
    )
}
