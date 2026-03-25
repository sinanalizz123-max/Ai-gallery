package com.smartgallery.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smartgallery.ai.engine.SmartAiEngine
import com.smartgallery.data.repository.AlbumRepository
import com.smartgallery.data.repository.FaceRepository
import com.smartgallery.data.repository.MediaRepository
import com.smartgallery.data.repository.PeopleRepository
import com.smartgallery.data.repository.SearchRepository
import com.smartgallery.ui.screens.AiTrainingScreen
import com.smartgallery.ui.screens.AlbumsScreen
import com.smartgallery.ui.screens.PeopleScreen
import com.smartgallery.ui.screens.PersonDetailScreen
import com.smartgallery.ui.screens.PhotoDetailScreen
import com.smartgallery.ui.screens.PhotosScreen
import com.smartgallery.ui.screens.SettingsScreen

@Composable
fun SmartGalleryNavHost(
    navController: NavHostController,
    modifier: Modifier,
    paddingValues: PaddingValues,
    mediaRepository: MediaRepository,
    peopleRepository: PeopleRepository,
    albumRepository: AlbumRepository,
    searchRepository: SearchRepository,
    aiEngine: SmartAiEngine,
    faceRepository: FaceRepository
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Photos.route,
        modifier = modifier
    ) {
        composable(Routes.Photos.route) {
            PhotosScreen(
                paddingValues = paddingValues,
                mediaRepository = mediaRepository,
                searchRepository = searchRepository,
                onOpenPhoto = { id -> navController.navigate(Routes.PhotoDetail.create(id)) }
            )
        }
        composable(Routes.People.route) {
            PeopleScreen(
                paddingValues = paddingValues,
                peopleRepository = peopleRepository,
                onPersonClick = { personId -> navController.navigate(Routes.PersonDetail.create(personId)) }
            )
        }
        composable(Routes.Albums.route) {
            AlbumsScreen(
                paddingValues = paddingValues,
                albumRepository = albumRepository
            )
        }
        composable(Routes.Ai.route) {
            AiTrainingScreen(
                paddingValues = paddingValues,
                aiEngine = aiEngine,
                onOpenSettings = { navController.navigate(Routes.Settings.route) }
            )
        }
        composable(
            route = Routes.PhotoDetail.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: 0L
            PhotoDetailScreen(
                mediaId = mediaId,
                mediaRepository = mediaRepository,
                faceRepository = faceRepository
            )
        }
        composable(
            route = Routes.PersonDetail.route,
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId") ?: ""
            PersonDetailScreen(
                personId = personId,
                mediaRepository = mediaRepository,
                peopleRepository = peopleRepository
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(
                paddingValues = paddingValues,
                peopleRepository = peopleRepository
            )
        }
    }
}
