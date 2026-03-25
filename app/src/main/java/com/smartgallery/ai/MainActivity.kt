package com.smartgallery.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smartgallery.ai.di.DefaultAppContainer
import com.smartgallery.ui.SmartGalleryApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = DefaultAppContainer(this)
        setContent {
            SmartGalleryApp(
                mediaRepository = appContainer.mediaRepository,
                peopleRepository = appContainer.peopleRepository,
                albumRepository = appContainer.albumRepository,
                searchRepository = appContainer.searchRepository,
                aiEngine = appContainer.aiEngine
            )
        }
    }
}
