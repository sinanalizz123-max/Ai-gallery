package com.smartgallery.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartgallery.ai.engine.SmartAiEngine
import com.smartgallery.data.repository.AlbumRepository
import com.smartgallery.data.repository.FaceRepository
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.PeopleRepository
import com.smartgallery.data.repository.SearchRepository
import com.smartgallery.ui.navigation.Routes
import com.smartgallery.ui.navigation.SmartBottomBar
import com.smartgallery.ui.navigation.SmartGalleryNavHost
import com.smartgallery.ui.theme.SmartGalleryTheme

@Composable
fun SmartGalleryApp(
    mediaRepository: MediaRepository,
    peopleRepository: PeopleRepository,
    albumRepository: AlbumRepository,
    searchRepository: SearchRepository,
    aiEngine: SmartAiEngine,
    faceRepository: FaceRepository
) {
    SmartGalleryTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            bottomBar = {
                val shouldShow = currentRoute in listOf(
                    Routes.Photos.route,
                    Routes.People.route,
                    Routes.Albums.route,
                    Routes.Ai.route
                )
                if (shouldShow) {
                    SmartBottomBar(currentRoute = currentRoute) { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                        }
                    }
                }
            }
        ) { padding ->
            SmartGalleryNavHost(
                navController = navController,
                modifier = Modifier,
                paddingValues = padding,
                mediaRepository = mediaRepository,
                peopleRepository = peopleRepository,
                albumRepository = albumRepository,
                searchRepository = searchRepository,
                aiEngine = aiEngine,
                faceRepository = faceRepository
            )
        }
    }
}
