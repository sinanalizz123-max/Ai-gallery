package com.smartgallery.data.model

import android.net.Uri

enum class AlbumType {
    SYSTEM,
    AI
}

data class Album(
    val id: String,
    val name: String,
    val coverUri: Uri? = null,
    val photoCount: Int = 0,
    val type: AlbumType
)
