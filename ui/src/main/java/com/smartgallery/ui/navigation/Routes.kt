package com.smartgallery.ui.navigation

sealed class Routes(val route: String) {
    data object Photos : Routes("photos")
    data object People : Routes("people")
    data object Albums : Routes("albums")
    data object Ai : Routes("ai")
    data object PhotoDetail : Routes("photo/{mediaId}") {
        fun create(mediaId: Long) = "photo/$mediaId"
    }
    data object PersonDetail : Routes("person/{personId}") {
        fun create(personId: String) = "person/$personId"
    }
    data object AlbumDetail : Routes("album/{albumId}") {
        fun create(albumId: String) = "album/$albumId"
    }
    data object Settings : Routes("settings")
}
